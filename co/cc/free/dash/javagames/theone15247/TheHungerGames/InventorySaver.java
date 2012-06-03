package co.cc.free.dash.javagames.theone15247.TheHungerGames;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventorySaver {
	private HashMap<String, List<ItemStack>> inventories;
	
	public InventorySaver() {
		inventories = new HashMap<String, List<ItemStack>>();
	}
	
	public void addInventory(Player p) {
		PlayerInventory pInv = p.getInventory();
		List<ItemStack> lstInv = new LinkedList<ItemStack>(Arrays.asList(pInv.getContents().clone()));
		
		if (pInv.getBoots() != null)
			lstInv.add(pInv.getBoots());
		
		if (pInv.getChestplate() != null)
			lstInv.add(pInv.getChestplate());
		
		if (pInv.getHelmet() != null)
			lstInv.add(pInv.getHelmet());
		
		if (pInv.getLeggings() != null)
			lstInv.add(pInv.getLeggings());
		synchronized(inventories) {
			inventories.put(p.getName(), lstInv);
		}
	}
	
	public void restoreInventory(Player p) {
		String name = p.getName();
		if (inventories.containsKey(name)) {
			synchronized(inventories) {
				Inventory pInv = p.getInventory();
				pInv.clear();
				HashMap<Integer, ItemStack> extra = new HashMap<Integer, ItemStack>();
				for (ItemStack is : inventories.get(name)) {
					if (is != null) {
						extra.putAll(pInv.addItem(is));
					}
				}
				for (ItemStack eIs : extra.values()) {
					p.getWorld().dropItem(p.getLocation(), eIs);
				}
				inventories.remove(name);
			}
		}
	}
}
