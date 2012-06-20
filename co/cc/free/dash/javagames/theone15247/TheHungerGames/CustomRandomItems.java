package co.cc.free.dash.javagames.theone15247.TheHungerGames;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
	LinkedHashMap<String, int[]> randomItems;
	LinkedHashMap<String, int[]> rewardItems;
	
	public CustomRandomItems(HungerGames hg, World w) {
		plugin = hg;
		this.w = w;
		r = new Random();
		randomItems = new LinkedHashMap<String, int[]>();
		rewardItems = new LinkedHashMap<String, int[]>();
	}
	
	public void saveItems() {
		try {
			if (plugin.getDataFolder().exists()) {
				File f = new File(plugin.getDataFolder().getCanonicalPath() + File.separator + "items_" + w.getName() + ".txt");

				if (!f.exists() || randomItems.size() <= 0) {
					// init with default
					addToRandomItems(Material.APPLE, new int[] {5, 8});
					addToRandomItems(Material.ARROW, new int[] {15, 28});
					addToRandomItems(Material.BED, new int[] {1});
					addToRandomItems(Material.BLAZE_ROD, new int[] {1});
					addToRandomItems(Material.BOW, new int[] {1});
					addToRandomItems(Material.BREAD, new int[] {10, 20});
					addToRandomItems(Material.CAKE, new int[] {1, 3});
					addToRandomItems(Material.PISTON_BASE, new int[] {4, 6});
					addToRandomItems(Material.COOKIE, new int[] {3, 6});
					addToRandomItems(Material.DIAMOND, new int[] {3, 6});
					addToRandomItems(Material.DIAMOND_HOE, new int[] {1});
					addToRandomItems(Material.DIAMOND_HELMET, new int[] {1});
					addToRandomItems(Material.BONE, new int[] {5, 10});
					addToRandomItems(Material.EGG, new int[] {15, 30});
					addToRandomItems(Material.FLINT_AND_STEEL, new int[] {1});
					addToRandomItems(Material.GLOWSTONE_DUST, new int[] {6, 16});
					addToRandomItems(Material.GOLD_CHESTPLATE, new int[] {1});
					addToRandomItems(Material.GOLDEN_APPLE, new int[] {1});
					addToRandomItems(Material.GOLD_INGOT, new int[] {5, 10});
					addToRandomItems(Material.IRON_AXE, new int[] {1});
					addToRandomItems(Material.IRON_CHESTPLATE, new int[] {1});
					addToRandomItems(Material.IRON_HELMET, new int[] {1});
					addToRandomItems(Material.IRON_LEGGINGS, new int[] {1});
					addToRandomItems(Material.DIAMOND_BLOCK, new int[] {1});
					addToRandomItems(Material.IRON_PICKAXE, new int[] {1});
					addToRandomItems(Material.IRON_SWORD, new int[] {1});
					addToRandomItems(Material.CAULDRON_ITEM, new int[] {1});
					addToRandomItems(Material.MAP, new int[] {1});
					addToRandomItems(Material.MUSHROOM_SOUP, new int[] {2, 5});
					addToRandomItems(Material.MOSSY_COBBLESTONE, new int[] {15, 40});
					addToRandomItems(Material.NETHERRACK, new int[] {10, 30});
					addToRandomItems(Material.OBSIDIAN, new int[] {2, 7});
					addToRandomItems(Material.DIAMOND_BOOTS, new int[] {1});
					addToRandomItems(createPotion().getType(), new int[] {1, 3});
					addToRandomItems(createPotion().getType(), new int[] {1, 3});
					addToRandomItems(Material.TORCH, new int[] {25, 60});
					addToRandomItems(Material.SLIME_BALL, new int[] {2, 7});
					addToRandomItems(Material.WEB, new int[] {2, 5});
					addToRandomItems(Material.WHEAT, new int[] {10, 20});
					addToRandomItems(Material.FEATHER, new int[] {6, 20});
					addToRandomItems(Material.FLINT, new int[] {6, 20});
					addToRandomItems(Material.BEDROCK, new int[] {1});
					addToRandomItems(Material.LAVA_BUCKET, new int[] {1});
					addToRandomItems(Material.BUCKET, new int[] {1});
					addToRandomItems(Material.IRON_ORE, new int[] {4, 15});
					addToRandomItems(Material.NETHER_STALK, new int[] {5, 10});
					addToRandomItems(Material.FISHING_ROD, new int[] {1});
					addToRandomItems(Material.IRON_BOOTS, new int[] {1});
					addToRandomItems(Material.DIAMOND_LEGGINGS, new int[] {1});
					addToRandomItems(Material.DIAMOND_PICKAXE, new int[] {1});
					addToRandomItems(Material.IRON_INGOT, new int[] {5, 15});
					addToRandomItems(Material.STONE_AXE, new int[] {1});
					addToRandomItems(Material.STONE_PICKAXE, new int[] {1});
					addToRandomItems(Material.STONE_SWORD, new int[] {1});
					addToRandomItems(Material.BREWING_STAND_ITEM, new int[] {1});
					addToRandomItems(Material.CHAINMAIL_BOOTS, new int[] {1});
					addToRandomItems(Material.CHAINMAIL_CHESTPLATE, new int[] {1});
					addToRandomItems(Material.CHAINMAIL_HELMET, new int[] {1});
					addToRandomItems(Material.CHAINMAIL_LEGGINGS, new int[] {1});
					addToRandomItems(Material.IRON_INGOT, new int[] {5, 11});
					addToRandomItems(createPotion().getType(), new int[] {1, 3});
					addToRandomItems(Material.GOLD_BOOTS, new int[] {1});
					addToRandomItems(Material.GOLD_HELMET, new int[] {1});
					addToRandomItems(Material.GOLD_LEGGINGS, new int[] {1});
					addToRandomItems(Material.GOLD_PICKAXE, new int[] {1});
					addToRandomItems(Material.COBBLESTONE, new int[] {32, 64});
					addToRandomItems(Material.COAL, new int[] {12, 48});
					addToRandomItems(Material.COOKED_BEEF, new int[] {3, 8});
					addToRandomItems(Material.COOKED_CHICKEN, new int[] {3, 8});
					addToRandomItems(Material.COOKED_FISH, new int[] {3, 8});
					addToRandomItems(Material.DISPENSER, new int[] {1, 4});
					addToRandomItems(Material.ENCHANTMENT_TABLE, new int[] {1});
					addToRandomItems(Material.EYE_OF_ENDER, new int[] {1, 5});
					addToRandomItems(Material.FERMENTED_SPIDER_EYE, new int[] {2, 5});
					addToRandomItems(Material.FIREBALL, new int[] {2, 8});
					addToRandomItems(Material.WOOD_SWORD, new int[] {1});
					addToRandomItems(Material.WOOD_PICKAXE, new int[] {1});
					addToRandomItems(Material.WOOD_AXE, new int[] {1});
					addToRandomItems(Material.WATER_BUCKET, new int[] {1});
					addToRandomItems(Material.SULPHUR, new int[] {3, 7});
					addToRandomItems(Material.WORKBENCH, new int[] {1});
					addToRandomItems(Material.STRING, new int[] {1, 5});
					addToRandomItems(Material.STICK, new int[] {20, 50});
					addToRandomItems(Material.SAND, new int[] {10, 30});
					addToRandomItems(createEgg(2).getType(), new int[] {1, 4});
					addToRandomItems(createEgg(3).getType(), new int[] {1, 4});
					addToRandomItems(Material.WOOD, new int[] {10, 48});
					addToRandomItems(Material.NETHER_WARTS, new int[] {3, 13});
					addToRandomItems(Material.SPIDER_EYE, new int[] {2, 10});
				}
				if (f.exists()) {
					f.delete();
				}
				f.createNewFile();
				BufferedWriter os = new BufferedWriter(new FileWriter(f));
				
				os.write("// material:min-max:isreward");
				os.newLine();
				
				// write each entry
				for (Entry<String, int[]> data:randomItems.entrySet()) {
					if (data.getValue().length == 1)
						os.write(data.getKey() + ":" + data.getValue()[0]);
					else if (data.getValue().length == 2)
						os.write(data.getKey() + ":" + data.getValue()[0] + "-" + data.getValue()[1]);
					else
						plugin.log.info("Error saving random item. Too many min-max values");
					
					// save rewards
					if (rewardItems.containsKey(data.getKey()) && 
							Arrays.equals(rewardItems.get(data.getKey()), data.getValue())) {
						os.write(":true");
					}
					
					os.newLine();
				}
				
				os.flush();
				os.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addToRandomItems(Material m, int [] amnt) {
		randomItems.put(m.name(), amnt);	
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
						if (splitString.length == 2 || splitString.length == 3) {
							boolean error = false;
							
							String[] splitRange = splitString[1].split("-");
							
							Material material = Material.getMaterial(splitString[0].trim().toUpperCase());
							if (material == null) {
								plugin.log.warning("Error parsing line in items_" + w.getName() + ".txt! Unknown material " + splitString[0].trim());
								error = true;
							}
							
							boolean reward = false;
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
								
								if (splitString.length == 3 && !error) {
									reward = Boolean.valueOf(splitString[2].trim());
								}
							} catch (NumberFormatException e) {
								plugin.log.warning("Error parsing line in items_" + w.getName() + ".txt! Number Format Exception");
								error = true;
							}
							
							if (!error) {
								randomItems.put(material.name(), new int[] {min, max});
								if (reward)
									rewardItems.put(material.name(), new int[] {min, max});
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
	
	public ArrayList<ItemStack> getRewards() {
		ArrayList<ItemStack> reward = new ArrayList<ItemStack>();
		if (rewardItems.size() <= 0) {
			// DEFAULT REWARD
			reward.add(new ItemStack(Material.DIAMOND_BLOCK, 1));
			reward.add(new ItemStack(Material.IRON_BLOCK, 5));
			reward.add(new ItemStack(Material.GOLD_BLOCK, 3));
		} else {
			for (int index=0; index<rewardItems.size(); index++) {
				String mName = rewardItems.keySet().toArray(new String[0])[index];
				Material m = Material.valueOf(mName);
				
				boolean error = false;
				Integer min = 0, max = 0;
				int dataLength = rewardItems.get(mName).length;
				if (dataLength == 1) {
					min = max = rewardItems.get(mName)[0];
				} else if (dataLength == 2) {
					min = rewardItems.get(mName)[0];
					max = rewardItems.get(mName)[1];
				} else {
					plugin.log.warning("Error getting min and max for custom random rewards!");
					if (m != null) {
						plugin.log.warning("Replaced with one " + mName + "!");
						reward.add(new ItemStack(m, 1));
					} else {
						plugin.log.warning("Replaced with 64 COBBLESTONE!");
						reward.add(new ItemStack(Material.COBBLESTONE, 64));
					}
					error = true;
				}
				
				if (!error)
					reward.add(new ItemStack(m, r.nextInt(max-min+1) + min));
			}
		}
		
		return reward;
	}
	
	public ItemStack getRandomItem() {
		if (randomItems.size() <= 0) {
			plugin.log.warning("No random items configured! Chests may not be created!");
		}
		
		int index = r.nextInt(randomItems.size());
		String mName = randomItems.keySet().toArray(new String[0])[index];
		Material m = Material.valueOf(mName);
		
		Integer min, max;
		int dataLength = randomItems.get(mName).length;
		if (dataLength == 1) {
			min = max = randomItems.get(mName)[0];
		} else if (dataLength == 2) {
			min = randomItems.get(mName)[0];
			max = randomItems.get(mName)[1];
		} else {
			plugin.log.warning("Error getting min and max for custom random items!");
			if (m != null) {
				plugin.log.warning("Replaced with one " + mName + "!");
				return new ItemStack(m, 1);
			} else {
				plugin.log.warning("Replaced with 16 COBBLESTONE!");
				return new ItemStack(Material.COBBLESTONE, 16);
			}
		}
		
		// because nextFloat is exclusive will never get 1 so 4.999 becomes 4
		return new ItemStack(m, r.nextInt(max-min+1) + min);
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
