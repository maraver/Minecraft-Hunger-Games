package co.cc.free.dash.javagames.theone15247.TheHungerGames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.util.Vector;

public class CustomGen {
	final int MAX_HEIGHT;
	final int CHEST_COUNT;
	final int BELOW_DIST;
	static final int MAX_TRIB = 24;
	static int SPAWN_X_OFF = 11 + 35;
	static int SPAWN_Z_OFF = -18;
	
	int CornustartX;
	int CornustartZ;
	Random r;
	HungerGames plugin;
	CustomRandomItems randItems;
	ArrayList<ItemBlock> chestBlocks;
	
	public CustomGen(HungerGames hg, int arenaSize, int chests, int centerX, int centerZ, int depth) {
		r = new Random();
		randItems = new CustomRandomItems(hg, hg.getServer().getWorld(hg.worldName));
		chestBlocks = new ArrayList<ItemBlock>();
		plugin = hg;
		
		CornustartX = centerX;
		CornustartZ = centerZ;
		
		MAX_HEIGHT = arenaSize;
		CHEST_COUNT = chests;
		BELOW_DIST = depth;
	}

	public Location[] createArena(World w, boolean spawnInArena) {
		Location spawnLoc = null;
		
		if (!plugin.hasCreatedArena) {
			if (CornustartX == 0 && CornustartZ == 0) {
				plugin.log.warning("A starting location hasn't been set!");
				return null;
			}
			int spawnX = w.getSpawnLocation().getBlockX();
			int spawnZ = w.getSpawnLocation().getBlockZ();
			
			if (Math.sqrt((CornustartX-spawnX)*(CornustartX-spawnX) + (CornustartZ-spawnZ)*(CornustartZ-spawnZ)) <= Math.sqrt(MAX_HEIGHT*MAX_HEIGHT + (MAX_HEIGHT/2)*(MAX_HEIGHT/2)) + 20) {
				// if don't want spawning in the arena
				if (!spawnInArena) {
					plugin.log.warning("The starting location is too close to the spawn!");
					return null;
				} else {
					plugin.log.info("The spawn will be in the arena");
				}
			}
			
			plugin.log.info("Creating arena...");
			
			long start = System.currentTimeMillis();
			
			Biome bio = w.getBiome(CornustartX - 21, CornustartZ);
			
			if(bio.compareTo(Biome.EXTREME_HILLS) == 0 || bio.compareTo(Biome.OCEAN) == 0 ||
					bio.compareTo(Biome.ICE_MOUNTAINS) == 0 || bio.compareTo(Biome.MUSHROOM_ISLAND) == 0 ||
					bio.compareTo(Biome.SKY) == 0 || bio.compareTo(Biome.JUNGLE) == 0 || 
					bio.compareTo(Biome.JUNGLE_HILLS) == 0 || bio.compareTo(Biome.SMALL_MOUNTAINS) == 0 || 
					bio.compareTo(Biome.BEACH) == 0 || bio.compareTo(Biome.RIVER) == 0) {
				plugin.log.info("The arena has been created in an unrecommended biome!");
			}
			plugin.log.info("Creating at X: " + CornustartX + " Z: " + CornustartZ + " Bio: " + bio.name());
			
			// creates the spawn, because of max high needs to be before orb
			spawnLoc = createStartCornu(w);
			
			// create orb
			int startY = w.getHighestBlockYAt(CornustartX, CornustartZ);
			int height = MAX_HEIGHT;
			int offSet = 0;
			int diameter = 1;
			
			int lowest = w.getMaxHeight() - 1;
			int highest = 0;
			// Normal Tower
			int useHeight = height;
			long lastAlert = 0;
			
			for(height=MAX_HEIGHT; height>=0; height--) {
				useHeight = height;
				if (startY + useHeight > w.getMaxHeight() - 3) useHeight = w.getMaxHeight() - 3 - startY;
				
				for(int x=0;x<diameter;x++) {
					if (useHeight > 0) {
						// find highest and lowest
						int yHigh = getHeighestFreeBlockAt(w, CornustartX - offSet + x, CornustartZ - offSet);
						if (yHigh > highest) highest = yHigh;
						else if (yHigh < lowest) {
							lowest = yHigh;
						}
						yHigh = getHeighestFreeBlockAt(w, CornustartX - offSet + x, CornustartZ + offSet);
						if (yHigh > highest) highest = yHigh;
						else if (yHigh < lowest) {
							lowest = yHigh;
						}
					}
					
					turnToUnbreakable(w, Material.GLASS, CornustartX - offSet + x, startY + useHeight, CornustartZ - offSet);
					
					turnToUnbreakable(w, Material.GLASS, CornustartX - offSet + x, startY + useHeight, CornustartZ + offSet);
					
					if (useHeight <= 0) {
						for(int y=1;y<startY+useHeight;y++) {
							turnToUnbreakable(w, Material.GLASS, CornustartX - offSet + x, y, CornustartZ - offSet);
							
							turnToUnbreakable(w, Material.GLASS, CornustartX - offSet + x, y, CornustartZ + offSet);
						}
					}
				}
				
				for(int z=0;z<diameter;z++) {
					if (useHeight > 0) {
						// find highest and lowest
						int yHigh = getHeighestFreeBlockAt(w, CornustartX - offSet, CornustartZ - offSet + z);
						if (yHigh > highest) highest = yHigh;
						else if (yHigh < lowest) {
							lowest = yHigh;
						}
						yHigh = getHeighestFreeBlockAt(w, CornustartX + offSet, CornustartZ - offSet + z);
						if (yHigh > highest) highest = yHigh;
						else if (yHigh < lowest) {
							lowest = yHigh;
						}
					}
					
					turnToUnbreakable(w, Material.GLASS, CornustartX - offSet, startY + useHeight, CornustartZ - offSet + z);
					
					turnToUnbreakable(w, Material.GLASS, CornustartX + offSet, startY + useHeight, CornustartZ - offSet + z);
					
					if (useHeight <= 0) {
						for(int y=1;y<startY+useHeight;y++) {
							turnToUnbreakable(w, Material.GLASS, CornustartX - offSet, y, CornustartZ - offSet + z);
							
							turnToUnbreakable(w, Material.GLASS, CornustartX + offSet, y, CornustartZ - offSet + z);
						}
					}
				}
				
				offSet++;
				diameter += 2;
				
				float area = MAX_HEIGHT * MAX_HEIGHT;
				float doneArea = (MAX_HEIGHT-height) * (MAX_HEIGHT-height);
				if(System.currentTimeMillis() - lastAlert > 1500) {
					lastAlert = System.currentTimeMillis();
					plugin.log.info("Creating limits: " + (int)(doneArea/area*100) + "%");
				}
			}
			
			plugin.log.info("Done creating limits, limiting depth");
			
			// limit distance below
			lowest = startY - BELOW_DIST;
			
			for(int x=-(diameter/2);x<diameter/2;x++) {
				for(int z=-(diameter/2);z<diameter/2;z++) {
					turnToUnbreakable(w, Material.GLASS, CornustartX + x, lowest, CornustartZ + z);
				}
			}
			
			plugin.log.info("Arena created in " + ((System.currentTimeMillis() - start) / 1000f) + " seconds");
		}
		
		Location spectateLoc = new Location(w, CornustartX, w.getHighestBlockYAt(CornustartX, CornustartZ) + 20, CornustartZ);
		return new Location[] {spawnLoc, spectateLoc};
	}
	
	// does the opposite of createArena()
	public void removeArena(World w, Location top, Location spawnLoc) {
		long start = System.currentTimeMillis();
		
		final int startX = top.getBlockX();
		final int startZ = top.getBlockZ();
		
		// remove orb
		final int startY = top.getBlockY();
		int antiheight = 0;
		int offSet = 0;
		int diameter = 1;
		// Normal Tower
		int useHeight = startY;
		long lastAlert = 0;
		
		// "anti"height because it starts at 0 and goes up, the opposite of creating
		for(antiheight=0; antiheight<=MAX_HEIGHT; antiheight++) {
			useHeight = startY - antiheight;
			if (useHeight > w.getMaxHeight() - 3) useHeight = w.getMaxHeight() - 3;
			
			for(int x=0;x<diameter;x++) {
				// get the block from over and up one to make it fit better with the terrain
				// up one to fix corners
				// over one so doesn't clear down as air
				Material m = Material.AIR; //w.getBlockAt(startX - offSet + x, useHeight - 1, startZ - offSet - 1).getType();
				CustomGen.turnToUnbreakable(w, m, startX - offSet + x, useHeight, startZ - offSet, true);
				
				m = Material.AIR; //w.getBlockAt(startX - offSet + x, useHeight - 1, startZ + offSet - 1).getType();
				CustomGen.turnToUnbreakable(w, m, startX - offSet + x, useHeight, startZ + offSet, true);
				
				if (useHeight <= startY - MAX_HEIGHT) {
					for(int y=1;y<startY-MAX_HEIGHT;y++) {
						m = w.getBlockAt(startX - offSet + x, y, startZ - offSet - 1).getType();
						CustomGen.turnToUnbreakable(w, m, startX - offSet + x, y + 1, startZ - offSet, true);
						
						m = w.getBlockAt(startX - offSet + x, y, startZ + offSet - 1).getType();
						CustomGen.turnToUnbreakable(w, m, startX - offSet + x, y + 1, startZ + offSet, true);
					}
				}
			}
			
			for(int z=0;z<diameter;z++) {			
				Material m = Material.AIR; //w.getBlockAt(startX - offSet - 1, useHeight - 1, startZ - offSet + z).getType();
				CustomGen.turnToUnbreakable(w, m, startX - offSet, useHeight, startZ - offSet + z, true);
				
				m = Material.AIR; //w.getBlockAt(startX + offSet - 1, useHeight - 1, startZ - offSet + z).getType();
				CustomGen.turnToUnbreakable(w, m, startX + offSet, useHeight, startZ - offSet + z, true);
				
				if (useHeight <= startY - MAX_HEIGHT) {
					for(int y=1;y<startY-MAX_HEIGHT;y++) {
						m = w.getBlockAt(startX - offSet - 1, y + 1, startZ - offSet + z).getType();
						CustomGen.turnToUnbreakable(w, m, startX - offSet, y, startZ - offSet + z, true);
						
						m = w.getBlockAt(startX + offSet - 1, y + 1, startZ - offSet + z).getType();
						CustomGen.turnToUnbreakable(w, m, startX + offSet, y, startZ - offSet + z, true);
					}
				}
			}
			
			offSet++;
			diameter += 2;
			
			float area = MAX_HEIGHT * MAX_HEIGHT;
			float doneArea = antiheight * antiheight;
			if(System.currentTimeMillis() - lastAlert > 1500) {
				lastAlert = System.currentTimeMillis();
				plugin.log.info("Removing limits: " + (int)(doneArea/area*100) + "%");
			}
		}
		
		plugin.log.info("Done removing limits, removing depth limit");
		
		// remove below limit
		int lowest = (startY - MAX_HEIGHT) - BELOW_DIST;
		
		for(int x=-(diameter/2);x<diameter/2;x++) {
			for(int z=-(diameter/2);z<diameter/2;z++) {
				Material m = w.getBlockAt(startX + x, lowest + 1, startZ + z).getType();
				CustomGen.turnToUnbreakable(w, m, startX + x, lowest, startZ + z, true);
			}
		}
		
		// remove the cornucopia at an absolute location leaving none of the original
		clearArea(w, new Location(w, CornustartX, spawnLoc.getBlockY()+3, CornustartZ-27), 60, true, 0);
		
		// remove pedestals
		int spawnX = spawnLoc.getBlockX();
		int spawnZ = spawnLoc.getBlockZ();
		int spawnY = spawnLoc.getBlockY();
		for(int i=0; i<MAX_TRIB; i++) {
			short[][][] building = Statics.undergrounds[1];
			Vector vec = CustomGen.getPos(i);
			
			Location l = new Location(w, spawnX, spawnY - 1, spawnZ).add(vec);
			clearArea(w, l, building[0].length, true, 0);
		}
		
		// make sure the chests are cleared
		clearChests(w);
		
		removeItems(w);
		
		// done messages
		plugin.log.info("Removed arena in " + ((System.currentTimeMillis() - start) / 1000f) + " seconds.");
	}
	
	// w.getHeighestYAt() counts glass as air, this one doesn't
	public static int getHeighestFreeBlockAt(final World world, final int posX, final int posZ) {
		final int maxHeight = world.getMaxHeight();
		int searchedHeight = maxHeight - 1;
		Block lastBlock = null;
		while (searchedHeight > 0) {
			final Block block = world.getBlockAt(posX, searchedHeight, posZ);
			if (lastBlock != null && lastBlock.getType() == Material.AIR && 
					block.getType() != Material.AIR) {
				break; 
			}
			lastBlock = block;
			searchedHeight--;
		}
		return ++searchedHeight;
	}
	
	private void createCornucopiaChests(World w) {
		Location startCorner = new Location(w, CornustartX, w.getHighestBlockYAt(CornustartX, CornustartZ), CornustartZ);
		
		int depth = Statics.cornucopia[0].length;
		int width = Statics.cornucopia[0][0].length;
		moveLocationToAverageHeight(w, width, depth, startCorner);
		createTower(w, Statics.cornuchests, startCorner);
	}
	
	public void createChests(World w) {
		createChests(w, true);
	}
	public void createChests(World w, boolean coru) {
		for (int i=0;i<CHEST_COUNT; i++) {
			int x = CornustartX + 11 + r.nextInt(30);
			int z = CornustartZ + (r.nextInt(29) - 11);
			createChestWithRandom(w, x, z);
		}
		
		// the cornucopia has chests the need to be made
		if (coru)
			createCornucopiaChests(w);
	}
	
	public void clearChests(World w) {
		for (ItemBlock ib:chestBlocks) {
			ib.remove();
		}
		
		chestBlocks.clear();
	}
	
	public void removeItems(World w) {
		// remove all items in the arena
		for (Item i:w.getEntitiesByClass(Item.class)) {
			if (plugin.inArena(i.getLocation())) {
				i.remove();
			}
		}
	}
	
	private Location createStartCornu(World w) {
		// subtract 13 because CornustartZ isn't in the center
		clearArea(w, new Location(w, CornustartX, w.getHighestBlockYAt(CornustartX, CornustartZ-27), CornustartZ-27), 60, false);
		
		Location startCorner = new Location(w, CornustartX, w.getHighestBlockYAt(CornustartX, CornustartZ), CornustartZ);
		
		int depth = Statics.cornucopia[0].length;
		int width = Statics.cornucopia[0][0].length;
		moveLocationToAverageHeight(w, width, depth, startCorner);
		createTower(w, Statics.cornucopia, startCorner);		
		
		// Entrance area
		int spawnX = CornustartX + SPAWN_X_OFF;
		int spawnZ = CornustartZ + SPAWN_Z_OFF;
		
		// LOL not correct but the right number :P
		int spawnY = startCorner.getBlockY() - Statics.underground[0].length;
		
		for(int i=0; i<MAX_TRIB; i++) {
			short[][][] building = Statics.undergrounds[1];
			Vector vec = CustomGen.getPos(i);
			
			Location l = new Location(w, spawnX, spawnY - 1, spawnZ).add(vec);
			createTower(w, building, l);
		}
		
		Location spawnLoc = new Location(w, spawnX, spawnY, spawnZ);
		
		return spawnLoc;
	}
	
	public static Vector getPos(int i) {
		int pos = i%8;
		Vector v = new Vector(0,0,0);
		if(i < 8) {
			v.setX(-(pos * 5));
			v.setZ(0);
		} else if (i >= 8 && i < 16) {
			v.setX(0);
			v.setZ(5 + pos * 5);
		} else {
			v.setX(-(pos * 5));
			v.setZ(9 * 5);
		}
		return v;
	}
	
	public static void turnToUnbreakable(World w, Material material, int x, int y, int z) {
		// default
		turnToUnbreakable(w, material, x, y, z, false);
	}
	public static void turnToUnbreakable(Block b, Material material) {
		// default
		turnToUnbreakable(b, material, false);
	}
	public static void turnToUnbreakable(World w, Material material, int x, int y, int z, boolean ignore) {
		Block b = w.getBlockAt(x, y, z);
		turnToUnbreakable(b, material, ignore);
	}
	public static void turnToUnbreakable(Block b, Material material, boolean ignore) {
		if (b.getData() != 6 || ignore) {
			b.setType(material);
			b.setData((byte) 6);
		}
	}
	
	private int clearArea(World w, Location startCorner, int diameter, boolean absoluteY) {
		return clearArea(w, startCorner, diameter, absoluteY, 1);
	}
	private int clearArea(World w, Location startCorner, int diameter, boolean absoluteY, int leaveAmount) {		
		int lowestY;
		if (absoluteY) {
			lowestY = startCorner.getBlockY();
		} else {
			int[] locationResults = moveLocationToAverageHeight(w, diameter, diameter, startCorner);
			int averageY = locationResults[0];
			//highestY = locationResults[1];
			lowestY = averageY; //locationResults[2];
		}
		
		for(int x=startCorner.getBlockX(); x<startCorner.getBlockX()+diameter; x++) {
			for(int z=startCorner.getBlockZ(); z<startCorner.getBlockZ()+diameter; z++) {
				// the highest block for that area (time saver)
				int highestY = CustomGen.getHeighestFreeBlockAt(w, x, z) + 2;
				// cut back stuff, but leave some of the original
				for(int y=lowestY + leaveAmount; y<highestY; y++) {
					Block b =  w.getBlockAt(x, y, z);
					b.setType(Material.AIR);
				}
				// one below lowest cleared is made sure a floor
				Block b = w.getBlockAt(x, lowestY, z);
				if (b.getType() == Material.WATER || b.getType() == Material.LAVA || b.getType() == Material.AIR ||
						b.getType() == Material.SAND) {
					b.setType(Material.GRASS);
				}
			}
		}
		
		return lowestY;
	}
	
	private int[] moveLocationToAverageHeight(World w, int width, int depth, Location startCorner) {
		int lowestY = 256;
		int highestY = 0;
		Map<Integer, Integer> averageYMap = new HashMap<Integer, Integer>();
		// Find the lowest block in the area
		for(int iX=0; iX<width; iX++) {
			Location tempFindYLoc = new Location(w, startCorner.getX(), startCorner.getY(), startCorner.getZ());
			tempFindYLoc.setX(tempFindYLoc.getX() + iX);
			for(int iZ=0; iZ<depth; iZ++) {
				tempFindYLoc.setZ(startCorner.getZ() + iZ);
				Block b = w.getHighestBlockAt(tempFindYLoc);
				int newY = b.getY(); // get the highest y value at this (x, z)
				// if not a "real mine-able block" don't count toward elevation
				if (isFakeBlock(b))  {
					newY -= 1;
				}
				if (newY < lowestY) {
					lowestY = newY;
				} else if (newY > highestY) {
					highestY = newY;
				}
				
				if (averageYMap.containsKey(newY)) {
					averageYMap.put(newY, averageYMap.get(newY) + 1);
				} else {
					averageYMap.put(newY, 1);
				}
			}
		}
		
		startCorner.setY(lowestY);
		
		// get the averageY
		int averageY = 0;
		int averageYCount = 0;
		for(int i:averageYMap.keySet()) {
			int count = averageYMap.get(i);
			if (count > averageYCount) {
				averageY = i;
				averageYCount = count;
			}
		}
		
		startCorner.setY(averageY);
		
		return new int[] {averageY, highestY, lowestY};
	}
	
	private void createTower(World w, short[][][] tower, Location startCorner) {		
		// , int averageY, int highestY) {
		int height = tower.length;
		int depth = tower[0].length;
		int width = tower[0][0].length;
		
		// create the tower
		for(int iX=0; iX<width; iX++) {
			Location tempLoc = new Location(w, startCorner.getX(), startCorner.getY(), startCorner.getZ());
			tempLoc.setX(tempLoc.getX() + iX);
			for(int iZ=0; iZ<depth; iZ++) {
				tempLoc.setZ(startCorner.getZ() + iZ);
				for(int iY=0; iY<height; iY++) {					
					tempLoc.setY(startCorner.getY() + iY);
					int id = tower[iY][iZ][iX];
					Block b = w.getBlockAt(tempLoc);
					if (id >= 0){
						// create block, if unbreakable block make it unbreakable
						turnToUnbreakable(b, Material.getMaterial(id));
					}
					if (id == 54) {
						createChestWithRandom(b);
					} 
				}
			}
		}
	}
	
	private boolean isFakeBlock(Block b) {
		return (b.getType() == Material.LONG_GRASS || b.getType() == Material.CACTUS 
		|| b.getType() == Material.RED_MUSHROOM || b.getType() == Material.RED_ROSE || 
		b.getType() == Material.DEAD_BUSH || b.getType() == Material.YELLOW_FLOWER || 
		b.getType() == Material.WATER_LILY || b.getType() == Material.VINE  || 
		b.getType() == Material.LEAVES || b.getType() == Material.WATER);
	}
	
	private void createChestWithRandom(World w, int x, int z) {
		Block belowBlock = w.getBlockAt(x, w.getHighestBlockYAt(x, z), z);
		int goUp = 0;
		
		if(isFakeBlock(belowBlock)) {
			goUp = -1;
		}
		
		createChestWithRandom(w.getBlockAt(x, w.getHighestBlockYAt(x, z) + goUp, z));
	}
	private void createChestWithRandom(Block b) {
		chestBlocks.add(new ItemBlock(this, b.getWorld(), b.getLocation(), randItems.getRandomItem()));
	}
}
