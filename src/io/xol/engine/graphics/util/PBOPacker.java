package io.xol.engine.graphics.util;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.ARBSync.*;
import org.lwjgl.opengl.GLSync;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import io.xol.chunkstories.api.rendering.RenderTargetManager;
import io.xol.engine.base.GameWindowOpenGL;
import io.xol.engine.concurrency.Fence;
import io.xol.engine.graphics.fbo.FrameBufferObject;
import io.xol.engine.graphics.textures.Texture2D;

public class PBOPacker
{
	FrameBufferObject fbo = new FrameBufferObject(null);
	
	int bufferId;
	boolean alreadyReading = false;
	
	public PBOPacker()
	{
		bufferId = glGenBuffers();
	}
	
	public void copyTexure(Texture2D texture)
	{
		copyTexure(texture, 0);
	}
	
	public PBOPackerResult copyTexure(Texture2D texture, int level)
	{
		if(alreadyReading)
			throw new RuntimeException("You asked this PBO downloader to download a texture but you did not finish the last read.");
		
		alreadyReading = true;
		
		long startT = System.nanoTime();
		
		glBindBuffer(GL_PIXEL_PACK_BUFFER, bufferId);
		//glBindTexture(GL_TEXTURE_2D, texture.getId());
		
		int width = texture.getWidth();
		int height = texture.getHeight();
		
		double pow = Math.pow(2, level);
		width =  (int)Math.ceil(width / pow);
		height = (int)Math.ceil(height / pow);
		
		//Allocates space for the read
		glBufferData(GL_PIXEL_PACK_BUFFER, width * height * 4 * 3 , GL_STREAM_COPY);

		//Obtains ref to RTM
		RenderTargetManager rtm = GameWindowOpenGL.getInstance().renderingContext.getRenderTargetManager();
		
		FrameBufferObject previousFB = rtm.getFramebufferWritingTo();
		rtm.setCurrentRenderTarget(fbo);
		fbo.setColorAttachements(texture.getMipLevelAsRenderTarget(level));
		fbo.setEnabledRenderTargets(true);
		
		//rtm.clearBoundRenderTargetAll();
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		
		//Reads the pixels of the texture to the PBO.
		glReadPixels(0, 0, width, height, GL_RGB, GL_FLOAT, 0);
		
		//slower method: 
		/*TextureFormat format = texture.getType();
		System.out.println(format.getBytesPerTexel());
		glGetTexImage(GL_TEXTURE_2D, level, format.getFormat(), format.getType(), 0);*/

		GLSync fence = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0x00);
		
		//Puts everything back into place
		glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
		rtm.setCurrentRenderTarget(previousFB);
		
		long endT = System.nanoTime();
		
		//System.out.println((endT-startT)/1000+"�s");
		
		return new PBOPackerResult(fence);
	}
	
	public class PBOPackerResult implements Fence {
		GLSync fence;
		boolean isTraversable = false;
		boolean readAlready = false;
		
		PBOPackerResult(GLSync fence)
		{
			this.fence = fence;
		}

		@Override
		public void traverse()
		{
			while(!isTraversable)
			{
				//Asks for wether the sync completed and timeouts in 1000ns or 1�s
				int waitReturnValue = glClientWaitSync(fence, GL_SYNC_FLUSH_COMMANDS_BIT, 1);
				
				//System.out.println("Waiting on GL fence");
				
				//Errors are considered ok
				if(waitReturnValue == GL_ALREADY_SIGNALED || waitReturnValue == GL_CONDITION_SATISFIED || waitReturnValue == GL_WAIT_FAILED)
					break;
			}
		}
		
		public boolean isTraversable()
		{
			//Don't do these calls for nothing
			if(isTraversable)
				return true;
			
			int syncStatus = glGetSynci(fence, GL_SYNC_STATUS);
			isTraversable = syncStatus == GL_SIGNALED;
			
			return isTraversable;
		}
		
		public ByteBuffer readPBO()
		{
			if(readAlready)
				throw new RuntimeException("Tried to read a PBOPackerResult twice !");
			
			//Traverses the sync object first
			traverse();
			
			glBindBuffer(GL_PIXEL_PACK_BUFFER, bufferId);
			
			//Map the buffer and read it
			long startT = System.nanoTime();
			ByteBuffer gpuBuffer = glMapBuffer(GL_PIXEL_PACK_BUFFER, GL_READ_ONLY, null);
			long endT = System.nanoTime();
			
			ByteBuffer freeBuffer = BufferUtils.createByteBuffer(gpuBuffer.capacity());
			int free = freeBuffer.remaining();
			freeBuffer.put(gpuBuffer);
			int freeNow = freeBuffer.remaining();
			//System.out.println("Read "+(free - freeNow)+" bytes from the PBO in "+(endT-startT)/1000+" �s");
			
			//Unmpapps the buffer 
		    glUnmapBuffer(GL_PIXEL_PACK_BUFFER);
			glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
			
			//Destroys the useless fence
			glDeleteSync(fence);
			
			PBOPacker.this.alreadyReading = false;
			this.readAlready = true;
			
			return freeBuffer;
		}
	}
	
	public void destroy()
	{
		glDeleteBuffers(bufferId);
	}
}
