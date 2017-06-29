package io.xol.chunkstories.input.lwjgl3;

import static org.lwjgl.glfw.GLFW.*;

import io.xol.chunkstories.api.input.KeyboardKeyInput;
import io.xol.chunkstories.client.Client;

//(c) 2015-2017 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

/**
 * Describes a key assignated to some action
 */
public class Lwjgl3KeyBind extends Lwjgl3Input implements KeyboardKeyInput, LWJGLPollable
{
	int GLFW_key;
	
	boolean isDown;
	boolean editable = true;
	
	public Lwjgl3KeyBind(Lwjgl3ClientInputsManager im, String name, String defaultKeyName)
	{
		super(im, name);
		this.GLFW_key = Client.getInstance().getConfig().getInteger("bind.glfw."+name, GLFWKeyIndexHelper.getGlfwKeyByName(defaultKeyName));
	}
	
	/**
	 * Internal to the engine, should not be interfered with by external mods
	 * @return
	 */
	public int getLWJGL2xKey()
	{
		return GLFW_key;
	}
	
	@Override
	public boolean isPressed()
	{
		return isDown;
	}

	/**
	 * When reloading from the config file (options changed)
	 */
	public void reload()
	{
		this.GLFW_key = Client.getInstance().getConfig().getInteger("bind.glfw."+name, -1);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null)
			return false;
		else if(o instanceof KeyboardKeyInput) {
			return ((KeyboardKeyInput)o).getName().equals(getName());
		}
		else if(o instanceof String) {
			return ((String)o).equals(this.getName());
		}
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return getName().hashCode();
	}
	
	@Override
	public void updateStatus()
	{
		isDown = glfwGetKey(im.gameWindow.glfwWindowHandle, GLFW_key) == GLFW_PRESS;//Keyboard.isKeyDown(LWJGL2_key);
	}
	
	/**
	 * Is this key bind editable in the controls
	 */
	public boolean isEditable()
	{
		return editable;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}
}
