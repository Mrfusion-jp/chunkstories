package io.xol.chunkstories.net.packets;

import io.xol.chunkstories.client.Client;
import io.xol.chunkstories.world.io.IOTasksMultiplayerClient;
import io.xol.chunkstories.world.summary.ChunkSummary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

//(c) 2015-2016 XolioWare Interactive
// http://chunkstories.xyz
// http://xol.io

public class Packet03ChunkSummary extends Packet
{
	// Server-side
	public ChunkSummary summary;
	
	// Client-side
	public int rx, rz;
	public byte[] compressedData;
	
	public Packet03ChunkSummary(boolean client)
	{
		super(client);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void send(DataOutputStream out) throws IOException
	{
		out.writeByte(0x03);
		out.writeInt(summary.rx);
		out.writeInt(summary.rz);
		ByteBuffer compressMe = ByteBuffer.allocateDirect(256 * 256 * 4 * 2);
			for(int i : summary.heights)
				compressMe.putInt(i);
			for(int i : summary.ids)
				compressMe.putInt(i);
			
		compressMe.flip();
		byte[] unCompressed = new byte[compressMe.remaining()];
		compressMe.get(unCompressed);
		byte[] compressedData = ChunkSummary.compressor.compress(unCompressed);
		out.writeInt(compressedData.length);
		out.write(compressedData);
	}

	@Override
	public void read(DataInputStream in) throws IOException
	{
		rx = in.readInt();
		rz = in.readInt();
		//System.out.println("read "+rx+":"+rz);
		int dataLength = in.readInt();
		compressedData = new byte[dataLength];
		in.readFully(compressedData);
	}

	@Override
	public void process(PacketsProcessor processor)
	{
		if(processor.isClient)
			((IOTasksMultiplayerClient) Client.world.ioHandler).requestChunkSummaryProcess(this);
	}

}
