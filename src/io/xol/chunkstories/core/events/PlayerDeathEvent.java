package io.xol.chunkstories.core.events;

import io.xol.chunkstories.api.events.Event;
import io.xol.chunkstories.api.events.EventListeners;
import io.xol.chunkstories.api.server.Player;

//(c) 2015-2016 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class PlayerDeathEvent extends Event
{
	// Every event class has to have this
	
	static EventListeners listeners = new EventListeners();
	
	@Override
	public EventListeners getListeners()
	{
		return listeners;
	}
	
	public static EventListeners getListenersStatic()
	{
		return listeners;
	}
	
	// Specific event code
	
	final Player player;
	String deathMessage;

	public PlayerDeathEvent(Player player)
	{
		this.player = player;
		this.deathMessage = player.getDisplayName()+" died.";
	}
	
	public String getDeathMessage()
	{
		return deathMessage;
	}

	public void setDeathMessage(String deathMessage)
	{
		this.deathMessage = deathMessage;
	}

	public Player getPlayer()
	{
		return player;
	}
}
