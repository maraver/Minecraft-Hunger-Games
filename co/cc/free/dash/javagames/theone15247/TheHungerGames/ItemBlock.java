package co.cc.free.dash.javagames.theone15247.TheHungerGames;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ItemBlock implements Serializable {
	private static final long serialVersionUID = -8465418635984738312L;
	
	double[] location;
	int[] itemstack;
	boolean hasMade;
	transient Block block;
	transient ArrayList<Item> items;
	static final int DIST = 2;
	
	public ItemBlock(CustomGen g, World w, Location bl, ItemStack i) {
		items = new ArrayList<Item>();
		
		Block belowBlock = w.getBlockAt(bl.getBlockX(), bl.getBlockY() - 1, bl.getBlockZ());
		if (belowBlock.getType() == Material.BEDROCK && belowBlock.getData() == (byte) 6) {
			for (ItemBlock ib:g.chestBlocks) {
				if (ib.isThisBlock(belowBlock)) {
					ib.addItem(w, bl, i);
					break;
				}
			}
			block = null;
			
			// there is already a block at this point
			hasMade = false;
			return;
		} else {
			Location blockLoc = new Location(w, bl.getBlockX(), bl.getBlockY(), bl.getBlockZ());
			
			// stuff to save
			location = new double[] {blockLoc.getX(), blockLoc.getY(), blockLoc.getZ()};
			itemstack = new int[] {i.getTypeId(), i.getAmount()};
			
			recreateInWorld(w);
			hasMade = true;
		}
	}
	
	private void recreateInWorld(World w) {
		Location blockLoc = new Location(w, location[0], location[1], location[2]);
		block = w.getBlockAt(blockLoc);
		block.setType(Material.BEDROCK);
		block.setData((byte) 6);
		
		items = new ArrayList<Item>();
		// move for item location
		blockLoc.add(new Vector(0.5, 1, 0.5));
		Item item = w.dropItem(blockLoc, new ItemStack(itemstack[0], itemstack[1]));
		item.setVelocity(new Vector(0, 0, 0));
		item.setPickupDelay(Integer.MAX_VALUE);
		items.add(item);
	}
	
	public void click(Player p) {
		// can only get if this is a valid block
		if (hasMade && isCloseEnough(p)) {
			for (Item item:items) {
				p.getInventory().addItem(item.getItemStack());
				item.remove();
			}
			block.setType(Material.AIR);
			block.setData((byte) 0);
		}
	}
	
	private boolean isCloseEnough(Player p) {
		if (hasMade) {
			Location l = p.getLocation();
			if (Math.abs(l.getX() - block.getX()) <= DIST || 
					Math.abs(l.getZ() - block.getZ()) <= DIST) {
				return true;
			}
		}
		return false;
	}
	
	// remove but no one gets stuff
	public void remove() {
		if (hasMade) {
			for (Item item:items)
				item.remove();
			block.setType(Material.AIR);
			block.setData((byte) 0);
		}
	}
	
	public void addItem(World w, Location dropLoc, ItemStack i) {
		if (hasMade) {
			Item item = w.dropItem(dropLoc, i);
			// so none move
			for (Item eItem:items) {
				eItem.setVelocity(new Vector(0,0,0));
				/// delay till can be picked up by players
				eItem.setPickupDelay(Integer.MAX_VALUE);
			}
			items.add(item);
		}
	}
	
	public boolean isThisBlock(Block b) {
		return b.equals(block);
	}
	
	public boolean isThisBlock(Item i) {
		return items.contains(i);
/*		for (Item item:items) {
			if (i.equals(item))
				return true;
		}
		return false; */
	}

	public Block getBlock() {
		return block;
	}
	
	// only out if was made
	private void writeObject(ObjectOutputStream out) throws IOException {
		if (hasMade) {
			out.defaultWriteObject();
		}
	}
}
