package io.xol.chunkstories.item.renderer;

import org.lwjgl.input.Mouse;
import io.xol.engine.math.lalgb.Vector4f;
import io.xol.chunkstories.api.entity.Inventory;
import io.xol.chunkstories.api.entity.interfaces.EntityWithSelectedItem;
import io.xol.chunkstories.gui.overlays.ingame.InventoryOverlay;
import io.xol.chunkstories.item.ItemPile;
import io.xol.engine.graphics.RenderingContext;
import io.xol.engine.graphics.fonts.TrueTypeFont;
import io.xol.engine.graphics.fonts.TrueTypeFontRenderer;
import io.xol.engine.graphics.textures.Texture2D;
import io.xol.engine.graphics.textures.TexturesHandler;

//(c) 2015-2016 XolioWare Interactive
// http://chunkstories.xyz
// http://xol.io

public class InventoryDrawer
{
	private Inventory inventory;
	private EntityWithSelectedItem entity;
	
	public InventoryDrawer(Inventory entityInventories)
	{
		this.inventory = entityInventories;
	}

	public InventoryDrawer(EntityWithSelectedItem entity)
	{
		this.entity = entity;
	}
	
	public void drawInventoryCentered(RenderingContext context, int x, int y, int scale, boolean summary, int blankLines)
	{
		drawInventory(context, x - slotsWidth(getInventory().getWidth(), scale) / 2, y - slotsHeight(getInventory().getHeight(), scale, summary, blankLines) / 2, scale, summary, blankLines, -1);
	}
	
	int[] selectedSlot;
	boolean closedButton = false;
	
	public int[] getSelectedSlot()
	{
		return selectedSlot;
	}

	public boolean isOverCloseButton()
	{
		return closedButton;
	}
	
	public void drawPlayerInventorySummary(RenderingContext context, int x, int y)
	{
		//Don't draw inventory only
		if(entity == null)
			return;
		drawInventory(context, x - slotsWidth(getInventory().getWidth(), 2) / 2, y - slotsHeight(getInventory().getHeight(), 2, true, 0) / 2, 2, true, 0, entity.getSelectedItemComponent().getSelectedSlot());
	}
	
	public void drawInventory(RenderingContext context, int x, int y, int scale, boolean summary, int blankLines, int highlightSlot)
	{
		if(getInventory() == null)
			return;
		
		int cornerSize = 8 * scale;
		int internalWidth = getInventory().getWidth() * 24 * scale;
		
		int height = summary ? 1 : getInventory().getHeight();
		
		int internalHeight = (height + (summary ? 0 : 1) + blankLines) * 24 * scale;
		int slotSize = 24 * scale;

		Texture2D inventoryTexture = TexturesHandler.getTexture("./textures/gui/inventory/inventory.png");
		inventoryTexture.setLinearFiltering(false);
		
		Vector4f color = new Vector4f(1f, 1f, 1f, summary ? 0.5f : 1f);
		//All 8 corners
		context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x, y + internalHeight + cornerSize, cornerSize, cornerSize, 0, 0.03125f, 0.03125f, 0, inventoryTexture, true, true, color);
		context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize, y + internalHeight + cornerSize, internalWidth, cornerSize, 0.03125f, 0.03125f, 0.96875f, 0, inventoryTexture, true, true, color);
		context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + internalWidth, y + internalHeight + cornerSize, cornerSize, cornerSize, 0.96875f, 0.03125f, 1f, 0, inventoryTexture, true, true, color);
		context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x, y, cornerSize, cornerSize, 0, 1f, 0.03125f, 248/256f, inventoryTexture, true, true, color);
		context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize, y, internalWidth, cornerSize, 0.03125f, 1f, 0.96875f, 248/256f, inventoryTexture, true, true, color);
		context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + internalWidth, y, cornerSize, cornerSize, 0.96875f, 1f, 1f, 248/256f, inventoryTexture, true, true, color);
		context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x, y+cornerSize, cornerSize, internalHeight, 0, 248f/256f, 0.03125f, 8f/256f, inventoryTexture, true, true, color);
		context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + internalWidth, y+cornerSize, cornerSize, internalHeight, 248/256f, 248f/256f, 1f, 8f/256f, inventoryTexture, true, true, color);
		//Actual inventory slots
		int sumSlots2HL = 0;
		boolean foundTheVegan = false;
		for (int i = 0; i < getInventory().getWidth(); i++)
		{
			for (int j = 0; j < height; j++)
			{
				boolean mouseOver = Mouse.getX() > x + cornerSize + i * slotSize && Mouse.getX() <= x + cornerSize + i * slotSize + slotSize
						&& Mouse.getY() > y + cornerSize + j * slotSize && Mouse.getY() <= y + cornerSize + j * slotSize + slotSize;
				//Just a dirt hack to always keep selecte slot values where we want them
				if(mouseOver)
				{
					selectedSlot = new int[] { i, j };
					foundTheVegan = true;
				}
				
				ItemPile selectedPile = null;
				if(selectedSlot != null)
					selectedPile = getInventory().getItemPileAt(selectedSlot[0], selectedSlot[1]);
				ItemPile thisPile = getInventory().getItemPileAt(i, j);
				
				if(summary)
				{
					ItemPile summaryBarSelected = getInventory().getItemPileAt(highlightSlot, 0);
					if(summaryBarSelected != null && i == summaryBarSelected.x)
					{
						sumSlots2HL = summaryBarSelected.item.getSlotsWidth();
					}
					if(sumSlots2HL > 0 || (summaryBarSelected == null && highlightSlot == i))
					{
						sumSlots2HL--;
						context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + j * slotSize, slotSize, slotSize, 32f / 256f, 176 / 256f, 56 / 256f, 152 / 256f, inventoryTexture, true, true, color);
					}
					else
						context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + j * slotSize, slotSize, slotSize, 8f / 256f, 176 / 256f, 32f / 256f, 152 / 256f, inventoryTexture, true, true, color);
					
				}
				else
				{
					if(mouseOver || (selectedPile != null && thisPile != null && selectedPile.x == thisPile.x && selectedPile.y == thisPile.y))
					{
						context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + j * slotSize, slotSize, slotSize, 32f / 256f, 176 / 256f, 56 / 256f, 152 / 256f, inventoryTexture, true, true, color);
					}
					else
						context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + j * slotSize, slotSize, slotSize, 8f / 256f, 176 / 256f, 32f / 256f, 152 / 256f, inventoryTexture, true, true, color);
				
				}
			}
		}
		if(!foundTheVegan)
			selectedSlot = null;
		//Blank part ( usefull for special inventories, ie player )
		for (int j = getInventory().getHeight(); j < getInventory().getHeight()+blankLines; j++)
		{
			for (int i = 0; i < getInventory().getWidth(); i++)
			{
				if(j == getInventory().getHeight())
				{
					if(i == getInventory().getWidth()-1)
						context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + j * slotSize, slotSize, slotSize, 224f / 256f, 152 / 256f, 248 / 256f, 128 / 256f, inventoryTexture, true, true, color);
					else
						context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + j * slotSize, slotSize, slotSize, 8f / 256f, 152 / 256f, 32f / 256f, 128 / 256f, inventoryTexture, true, true, color);
				}
				else
				{
					if(i == getInventory().getWidth()-1)
						context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + j * slotSize, slotSize, slotSize, 224f / 256f, 56 / 256f, 248 / 256f, 32 / 256f, inventoryTexture, true, true, color);
					else
						context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + j * slotSize, slotSize, slotSize, 8f / 256f, 56 / 256f, 32f / 256f, 32 / 256f, inventoryTexture, true, true, color);
				}
			}
		}
		//Top part
		if(!summary)
		{
			context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize, y + cornerSize + internalHeight - slotSize, slotSize, slotSize, 8f / 256f, 32f / 256f, 32f / 256f, 8f / 256f, inventoryTexture, true, true, color);
			for (int i = 1; i < getInventory().getWidth() - 2; i++)
			{
				context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + i * slotSize, y + cornerSize + internalHeight - slotSize, slotSize, slotSize, 32f / 256f, 32f / 256f, 56f / 256f, 8f / 256f, inventoryTexture, true, true, color);
			}
			context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + (getInventory().getWidth() - 2) * slotSize, y + cornerSize + internalHeight - slotSize, slotSize, slotSize, 200f / 256f, 32f / 256f, 224 / 256f, 8f / 256f, inventoryTexture, true, true, color);
			closedButton = Mouse.getX() > x + cornerSize + (getInventory().getWidth() - 1) * slotSize && Mouse.getX() <= x + cornerSize + (getInventory().getWidth() - 1) * slotSize + slotSize
					&& Mouse.getY() > y + cornerSize + internalHeight - slotSize && Mouse.getY() <= y + cornerSize + internalHeight;
			
			context.getGuiRenderer().drawBoxWindowsSpaceWithSize(x + cornerSize + (getInventory().getWidth() - 1) * slotSize, y + cornerSize + internalHeight - slotSize, slotSize, slotSize, 224f / 256f, 32f / 256f, 248f / 256f, 8f / 256f, inventoryTexture, true, true, color);
			TrueTypeFontRenderer.get().drawStringWithShadow(TrueTypeFont.haettenschweiler, x + cornerSize + 6 , y + cornerSize + internalHeight - slotSize + 2 * scale, getInventory().getHolderName(), scale, scale, new Vector4f(1,1,1,1));
		}

		//Get rid of any remaining GUI elements or else they will draw on top of the items
		context.getGuiRenderer().drawBuffer();
		
		//Draw the actual items
		for (int i = 0; i < getInventory().getWidth(); i++)
		{
			for (int j = 0; j < height; j++)
			{
				ItemPile pile = getInventory().getContents()[i][j];
				if(pile != null)
				{
					int amountToDisplay = pile.getAmount();
					//If we selected this item
					if((InventoryOverlay.selectedItem != null 
							&& InventoryOverlay.selectedItem.inventory != null 
							&& getInventory().equals(InventoryOverlay.selectedItem.inventory) 
							&& InventoryOverlay.selectedItem.x == i 
							&& InventoryOverlay.selectedItem.y == j ))
					{
						amountToDisplay -= InventoryOverlay.selectedItemAmount;
					}
					if(amountToDisplay <= 0)
						continue;
					
					int center = summary ? slotSize * (pile.item.getSlotsHeight()-1) / 2 : 0;
					pile.getItem().getItemRenderer().renderItemInInventory(context, pile, x + cornerSize + i * slotSize, y - center + cornerSize + j * slotSize, scale);
					
					//System.out.println(""+(x + cornerSize + ((pile.getItem().getSlotsWidth() - 0.5f) + i) * slotSize));

					if(amountToDisplay > 1)
						TrueTypeFontRenderer.get().drawStringWithShadow(TrueTypeFont.arial11px, x + cornerSize + ((pile.getItem().getSlotsWidth() - 1.0f) + i) * slotSize , y + cornerSize + j * slotSize, amountToDisplay+"", scale, scale, new Vector4f(1,1,1,1));
				}
			}
		}
	}

	public int slotsWidth(int slots, int scale)
	{
		return (8 + slots * 24) * scale;
	}

	public int slotsHeight(int slots, int scale, boolean summary, int blankLines)
	{
		return (8 + (slots+(summary ? 0 : 1)+blankLines) * 24) * scale;
	}

	public Inventory getInventory()
	{
		if(entity == null)
			return inventory;
		return entity.getInventory();
	}
}
