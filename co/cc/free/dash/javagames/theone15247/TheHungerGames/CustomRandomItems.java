package co.cc.free.dash.javagames.theone15247.TheHungerGames;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CustomRandomItems {
	HungerGames plugin;
	World w;
	Random r;
	LinkedHashMap<Material, int[]> randomItems;
	
	public CustomRandomItems(HungerGames hg, World w) {
		plugin = hg;
		this.w = w;
		r = new Random();
		randomItems = new LinkedHashMap<Material, int[]>();
	}
	
	public void saveItems() {
		try {
			if (plugin.getDataFolder().exists()) {
				File f = new File(plugin.getDataFolder().getCanonicalPath() + File.separator + "items_" + w.getName() + ".txt");

				if (!f.exists() || randomItems.size() <= 0) {
					// init with default
					randomItems.put(Material.APPLE, new int[] {5, 8});
					randomItems.put(Material.ARROW, new int[] {15, 28});
					randomItems.put(Material.BED, new int[] {1});
					randomItems.put(Material.BLAZE_ROD, new int[] {1});
					randomItems.put(Material.BOW, new int[] {1});
					randomItems.put(Material.BREAD, new int[] {10, 20});
					randomItems.put(Material.CAKE, new int[] {1, 3});
					randomItems.put(Material.PISTON_BASE, new int[] {4, 6});
					randomItems.put(Material.COOKIE, new int[] {3, 6});
					randomItems.put(Material.DIAMOND, new int[] {3, 6});
					randomItems.put(Material.DIAMOND_HOE, new int[] {1});
					randomItems.put(Material.DIAMOND_HELMET, new int[] {1});
					randomItems.put(Material.BONE, new int[] {5, 10});
					randomItems.put(Material.EGG, new int[] {15, 30});
					randomItems.put(Material.FLINT_AND_STEEL, new int[] {1});
					randomItems.put(Material.GLOWSTONE_DUST, new int[] {6, 16});
					randomItems.put(Material.GOLD_CHESTPLATE, new int[] {1});
					randomItems.put(Material.GOLDEN_APPLE, new int[] {1});
					randomItems.put(Material.GOLD_INGOT, new int[] {5, 10});
					randomItems.put(Material.IRON_AXE, new int[] {1});
					randomItems.put(Material.IRON_CHESTPLATE, new int[] {1});
					randomItems.put(Material.IRON_HELMET, new int[] {1});
					randomItems.put(Material.IRON_LEGGINGS, new int[] {1});
					randomItems.put(Material.DIAMOND_BLOCK, new int[] {1});
					randomItems.put(Material.IRON_PICKAXE, new int[] {1});
					randomItems.put(Material.IRON_SWORD, new int[] {1});
					randomItems.put(Material.CAULDRON_ITEM, new int[] {1});
					randomItems.put(Material.MAP, new int[] {1});
					randomItems.put(Material.MUSHROOM_SOUP, new int[] {2, 5});
					randomItems.put(Material.MOSSY_COBBLESTONE, new int[] {15, 40});
					randomItems.put(Material.NETHERRACK, new int[] {10, 30});
					randomItems.put(Material.OBSIDIAN, new int[] {2, 7});
					randomItems.put(Material.DIAMOND_BOOTS, new int[] {1});
					randomItems.put(createPotion().getType(), new int[] {1, 3});
					randomItems.put(createPotion().getType(), new int[] {1, 3});
					randomItems.put(Material.TORCH, new int[] {25, 60});
					randomItems.put(Material.SLIME_BALL, new int[] {2, 7});
					randomItems.put(Material.WEB, new int[] {2, 5});
					randomItems.put(Material.WHEAT, new int[] {10, 20});
					randomItems.put(Material.FEATHER, new int[] {6, 20});
					randomItems.put(Material.FLINT, new int[] {6, 20});
					randomItems.put(Material.BEDROCK, new int[] {1});
					randomItems.put(Material.LAVA_BUCKET, new int[] {1});
					randomItems.put(Material.BUCKET, new int[] {1});
					randomItems.put(Material.IRON_ORE, new int[] {4, 15});
					randomItems.put(Material.NETHER_STALK, new int[] {5, 10});
					randomItems.put(Material.FISHING_ROD, new int[] {1});
					randomItems.put(Material.IRON_BOOTS, new int[] {1});
					randomItems.put(Material.DIAMOND_LEGGINGS, new int[] {1});
					randomItems.put(Material.DIAMOND_PICKAXE, new int[] {1});
					randomItems.put(Material.IRON_INGOT, new int[] {5, 15});
					randomItems.put(Material.STONE_AXE, new int[] {1});
					randomItems.put(Material.STONE_PICKAXE, new int[] {1});
					randomItems.put(Material.STONE_SWORD, new int[] {1});
					randomItems.put(Material.BREWING_STAND_ITEM, new int[] {1});
					randomItems.put(Material.CHAINMAIL_BOOTS, new int[] {1});
					randomItems.put(Material.CHAINMAIL_CHESTPLATE, new int[] {1});
					randomItems.put(Material.CHAINMAIL_HELMET, new int[] {1});
					randomItems.put(Material.CHAINMAIL_LEGGINGS, new int[] {1});
					randomItems.put(Material.IRON_INGOT, new int[] {5, 11});
					randomItems.put(createPotion().getType(), new int[] {1, 3});
					randomItems.put(Material.GOLD_BOOTS, new int[] {1});
					randomItems.put(Material.GOLD_HELMET, new int[] {1});
					randomItems.put(Material.GOLD_LEGGINGS, new int[] {1});
					randomItems.put(Material.GOLD_PICKAXE, new int[] {1});
					randomItems.put(Material.COBBLESTONE, new int[] {32, 64});
					randomItems.put(Material.COAL, new int[] {12, 48});
					randomItems.put(Material.COOKED_BEEF, new int[] {3, 8});
					randomItems.put(Material.COOKED_CHICKEN, new int[] {3, 8});
					randomItems.put(Material.COOKED_FISH, new int[] {3, 8});
					randomItems.put(Material.DISPENSER, new int[] {1, 4});
					randomItems.put(Material.ENCHANTMENT_TABLE, new int[] {1});
					randomItems.put(Material.EYE_OF_ENDER, new int[] {1, 5});
					randomItems.put(Material.FERMENTED_SPIDER_EYE, new int[] {2, 5});
					randomItems.put(Material.FIREBALL, new int[] {2, 8});
					randomItems.put(Material.WOOD_SWORD, new int[] {1});
					randomItems.put(Material.WOOD_PICKAXE, new int[] {1});
					randomItems.put(Material.WOOD_AXE, new int[] {1});
					randomItems.put(Material.WATER_BUCKET, new int[] {1});
					randomItems.put(Material.SULPHUR, new int[] {3, 7});
					randomItems.put(Material.WORKBENCH, new int[] {1});
					randomItems.put(Material.STRING, new int[] {1, 5});
					randomItems.put(Material.STICK, new int[] {20, 50});
					randomItems.put(Material.SAND, new int[] {10, 30});
					randomItems.put(createEgg(2).getType(), new int[] {1, 4});
					randomItems.put(createEgg(3).getType(), new int[] {1, 4});
					randomItems.put(Material.WOOD, new int[] {10, 48});
					randomItems.put(Material.NETHER_WARTS, new int[] {3, 13});
					randomItems.put(Material.SPIDER_EYE, new int[] {2, 10});
				}
				if (f.exists()) {
					f.delete();
				}
				f.createNewFile();
				BufferedWriter os = new BufferedWriter(new FileWriter(f));
				
				os.write("// material:min-max");
				os.newLine();
				
				// write each entry
				for (Entry<Material, int[]> data:randomItems.entrySet()) {
					if (data.getValue().length == 1)
						os.write(data.getKey().name() + ":" + data.getValue()[0]);
					else if (data.getValue().length == 2)
						os.write(data.getKey().name() + ":" + data.getValue()[0] + "-" + data.getValue()[1]);
					else
						plugin.log.info("Error saving random item. Too many min-max values");
					os.newLine();
				}
				
				os.flush();
				os.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadItems() {
		randomItems.clear();
		
		ArrayList<String> itemsAsString = new ArrayList<String>();
		try {
			if (plugin.getDataFolder().exists()) {
				File f = new File(plugin.getDataFolder().getCanonicalPath() + File.separator + "items_" + w.getName() + ".txt");
				if(f.exists() && f.length() > 0) {
					BufferedReader is = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
					
					String line;
					while ((line = is.readLine()) != null) {
						itemsAsString.add(line);
					}
					
					is.close();
				} else {
					// if it doesn't exit create it
					saveItems();
					plugin.log.info("Custom Items not defined, saving default");
					// using the predefines so doesn't have to do other stuff
					return;
				}
			} else {
				plugin.log.info("The Data Folder doesn't exist. Couldn't load items_" + w.getName() + ".txt");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (itemsAsString.size() > 0) {
			synchronized (this) {
				for (String s:itemsAsString) {
					if (!s.trim().startsWith("//")) {
						String[] splitString = s.split(":");
						if (splitString.length == 2) {
							boolean error = false;
							
							String[] splitRange = splitString[1].split("-");
							
							Material material = Material.getMaterial(splitString[0].trim().toUpperCase());
							if (material == null) {
								plugin.log.warning("Error parsing line in items_" + w.getName() + ".txt! Unknown material " + splitString[0].trim());
								error = true;
							}
							
							int min = 0, max = 0;
							try {
								if (splitRange.length == 1 && !error) {
									min = Integer.valueOf(splitRange[0].trim());
									max = min;
								} else if (splitRange.length == 2 && !error) {
									min = Integer.valueOf(splitRange[0].trim());
									max = Integer.valueOf(splitRange[1].trim());
								} else {
									plugin.log.warning("Error parsing line in items_" + w.getName() + ".txt! Invalid dash count");
									error = true;
								}
							} catch (NumberFormatException e) {
								plugin.log.warning("Error parsing line in items_" + w.getName() + ".txt! Number Format Exception");
								error = true;
							}
							
							if (!error) {
								randomItems.put(material, new int[] {min, max});
							}
						} else {
							plugin.log.warning("Error parsing line in items_" + w.getName() + ".txt! Invalid colon count");
						}
					}
				}
			}
		} else {
			plugin.log.warning("Failed to load custom items");
		}
	}
	
	public ItemStack getRandomItem() {
		if (randomItems.size() <= 0) {
			plugin.log.warning("No random items configured! Chests may not be created!");
		}
		
		int index = r.nextInt(randomItems.size());
		Material m = randomItems.keySet().toArray(new Material[2])[index];
		
		Integer min, max;
		int dataLength = randomItems.get(m).length;
		if (dataLength == 1) {
			min = max = randomItems.get(m)[0];
		} else if (dataLength == 2) {
			min = randomItems.get(m)[0];
			max = randomItems.get(m)[1];
		} else {
			plugin.log.warning("Error getting min and max for custom random items!");
			if (m != null) {
				plugin.log.warning("Replaced with one " + m.name() + "!");
				return new ItemStack(m, 1);
			} else {
				plugin.log.warning("Replaced with 16 COBBLESTONE!");
				return new ItemStack(Material.COBBLESTONE, 16);
			}
		}
		
		// because nextFloat is exclusive will never get 1 so 4.999 becomes 4
		return new ItemStack(m, (int) ((max - min + 1) * r.nextFloat() + min));
	}
	
	private ItemStack createPotion() {
		int amount = r.nextInt(3)+1;
		return new ItemStack(373, amount, (byte) (r.nextInt(63)+1));
	}
	
	private ItemStack createEgg(int amount) {
		int num = r.nextInt(10);
		int egg;
		
		switch (num) {
		case 0: 
			egg = 50;
			break;
		case 1: 
			egg = 51;
			break;
		case 2:
			egg = 52;
			break;
		case 3:
			egg = 54;
			break;
		case 4:
			egg = 55;
			break;
		case 5:
			egg = 57;
			break;
		case 6:
			egg = 59;
			break;
		case 7:
			egg = 58;
			break;
		case 8:
			egg = 90;
			break;
		default:
			egg = 92;
		}
		return new ItemStack(383, amount, (byte) egg);
	}
}
