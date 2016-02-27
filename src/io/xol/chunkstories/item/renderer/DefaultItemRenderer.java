package io.xol.chunkstories.item.renderer;

import io.xol.chunkstories.item.Item;
import io.xol.chunkstories.item.ItemPile;
import io.xol.engine.gui.GuiDrawer;
import io.xol.engine.model.RenderingContext;
import io.xol.engine.textures.TexturesHandler;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;

//(c) 2015-2016 XolioWare Interactive
//http://chunkstories.xyz
//http://xol.io

public class DefaultItemRenderer implements ItemRenderer
{
	Item item;
	
	public DefaultItemRenderer(Item item)
	{
		this.item = item;
	}

	@Override
	public void renderItemInInventory(RenderingContext context, ItemPile pile, int screenPositionX, int screenPositionY, int scaling)
	{
		int slotSize = 24 * 2;
		TexturesHandler.getTexture(pile.getTextureName()).setLinearFiltering(false);
		int textureId = TexturesHandler.getTextureID(pile.getTextureName());
		if(textureId == -1)
			textureId = TexturesHandler.getTexture("res/items/icons/notex.png").getID();
		int width = slotSize * pile.item.getSlotsWidth();
		int height = slotSize * pile.item.getSlotsHeight();
		GuiDrawer.drawBoxWindowsSpaceWithSize(screenPositionX, screenPositionY, width, height, 0, 1, 1, 0, textureId, true, true, null);
	}

	@Override
	public void renderItemInWorld(RenderingContext context, ItemPile pile, Matrix4f handTransformation)
	{
		// TODO Auto-generated method stub
		
	}

}
