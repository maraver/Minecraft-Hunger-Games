package co.cc.free.dash.javagames.theone15247.TheHungerGames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class HungerGames extends JavaPlugin implements Listener {
	final static int MIN_RADIUS = 70;
	final static int UP_AMOUNT = 1;
	// TODO 2
	public final static int MIN_TRIB = 1;
	
	Logger log;
	Autorun auto;
	CustomGen cGen;
	Random rand;
	BlockSaver blockSaver;
	InventorySaver invSaver;
	
	// Configuration
	boolean hasCreatedArena = false;
	boolean protectGamesTNT = false;
	boolean unOP = false;
	boolean removeArena = false;
	boolean spawnInArena = false;
	boolean compassPoint = true;
	boolean updating = false;
	boolean dayAtStart = false;
	int respawnItemsTime = 300;
	// autorun stuff
	Autorun autorun;
	boolean doAutorun = false;
	int betweenGamesSec = 10;
	int startingTribs = 2;
	int randomPickTribs = 10;
	int tribWaitSec = 10;
	
	int radius = 150;
	int depth = 15;
	int items = 10;
	int tribHearDist = 25;
	World world;
	String worldName = "world";
	// Saved
	Location arenaSpawnLoc, arenaSpectateLoc;
	int centerX = 0, centerZ = 0;
	boolean gameHasStarted, gamePrepare, gameAccept;
	ConcurrentHashMap<String, Byte> tributes = new ConcurrentHashMap<String, Byte>();
	int tributeCount = 0;
	int lastTributeCheck = 0;
	ConcurrentHashMap<String, Byte> spectators = new ConcurrentHashMap<String, Byte>();
	ConcurrentHashMap<String, Byte> gamemakers = new ConcurrentHashMap<String, Byte>();
	// NOT saved
	ConcurrentHashMap<String, Byte> unTributes = new ConcurrentHashMap<String, Byte>();
	ConcurrentHashMap<String, Byte> unSpectators = new ConcurrentHashMap<String, Byte>();
	Set<String> dontChoose = new HashSet<String>();
	Map<String, Long> playerInBed;
	Map<String, Long> lastGiveItem;
	
	@Override
	public void onEnable() { 
		rand = new Random();
		log = this.getLogger();	
		getServer().getPluginManager().registerEvents(this, this);
		blockSaver = new BlockSaver(this);
		getServer().getPluginManager().registerEvents(blockSaver, this);
		invSaver = new InventorySaver();
		
		loadWorldName();
		loadConfig();
		
		addRecipe();
		cGen = new CustomGen(this, radius, items, centerX, centerZ, depth);
		
		world = this.getServer().getWorld(worldName);
		
		boolean loaded = loadWorld();
		// failed to load, shutdown
		if (!loaded) {
			setEnabled(false);
			return;
		}
		
		playerInBed = new HashMap<String, Long>();
		lastGiveItem = new HashMap<String, Long>();
		
		// not all the tributes and spectators will be back online, so remove them all
		// we have to do it this way incase the server unexpectidly crashes
		restartGame();
		
		log.info("The Hunger Games Plugin Enabled");
		
		// enable after done loading so doesn't call timer too soon
		if (doAutorun && autorun != null)
			autorun.startAutorun();
	}
	
	@Override
	public void onDisable() {		
		// only save if has started
		if (hasCreatedArena) {
			saveBinFile();
			blockSaver.saveBlockFile();
			cGen.randItems.saveItems();
		}
		
		log.warning("The Hunger Games Plugin Disabled");
	}
	
	private void addRecipe() {
		ShapedRecipe compassRecipe = new ShapedRecipe(new ItemStack(Material.COMPASS, 1));
		compassRecipe.shape(new String[] { " # ", "IWI", " I " });
		compassRecipe.setIngredient('W', Material.WATER_BUCKET);
		compassRecipe.setIngredient('#', Material.LEAVES);
		compassRecipe.setIngredient('I', Material.IRON_INGOT);
		this.getServer().addRecipe(compassRecipe);
	}
	
	// some things in this method may need to run events
	private boolean refreshIn(ConfigurationSection cs) {
		boolean hasFound = true;
		
		if (cs.contains("items")) {
			items = cs.getInt("items");
			if (items < 0) {
				items = 0;
				log.info("There must be a positive number of items");
			}
		} else hasFound = false;
		
		if (cs.contains("tribHearDist")) {
			tribHearDist = cs.getInt("tribHearDist");
		} else hasFound = false;
		
		if (cs.contains("protectGamesTNT")) {
			protectGamesTNT = cs.getBoolean("protectGamesTNT");
			if (protectGamesTNT)
				log.info("Protecting arena blocks from TNT");
		} else hasFound = false;
		
		if (cs.contains("unOP")) {
			unOP = cs.getBoolean("unOP");
		} else hasFound = false;
		
		if (cs.contains("compassPoint")) {
			compassPoint = cs.getBoolean("compassPoint");
		} else hasFound = false;
		
		if (cs.contains("dayAtStart")) {
			dayAtStart = cs.getBoolean("dayAtStart");
		} else hasFound = false;
		
		
		
		// new stuff
		loadAutoRun(cs);
		
		return hasFound;
	}

	// load the autorun section
	private void loadAutoRun(ConfigurationSection cs) {
		ConfigurationSection interiorCs = cs.getConfigurationSection("autorun");
		
		betweenGamesSec = interiorCs.getInt("betweenGamesSec");
		
		startingTribs = interiorCs.getInt("startingTribs");
		
		randomPickTribs = interiorCs.getInt("randomPickTribs");
		
		tribWaitSec = interiorCs.getInt("tribWaitSec");
		
		respawnItemsTime = interiorCs.getInt("respawnItemsTime");
		
		doAutorun = interiorCs.getBoolean("doAutorun");
		// if true start autorunning
		if (doAutorun) {
			// setup autorun
			if (autorun == null)
				autorun = new Autorun(this, betweenGamesSec, startingTribs, randomPickTribs, tribWaitSec, respawnItemsTime);
			// TODO start autorun
		}
	}
	
	private void refreshConfig() {
		// if this section exists
		if (getConfig().isConfigurationSection("refresh")) {		
			ConfigurationSection cs = getConfig().getConfigurationSection("refresh");
			refreshIn(cs);
		} else {
			// get from old setup, without sections
			ConfigurationSection cs = getConfig().getDefaultSection();
			log.info("Using 1.2.1s location for config");
			boolean result = refreshIn(cs);	
			if (!result) log.warning("Couldn't find the refresh section, defaulting...");
		}
	}
	
	private boolean loadOnceIn(ConfigurationSection cs) {
		boolean hasFound = true;
		
		if (cs.contains("radius")) {
			radius = cs.getInt("radius");
			if (radius < MIN_RADIUS) {
				radius = MIN_RADIUS;
				log.info("The entered size is too small. Defaulting to " + MIN_RADIUS + "!");
			}
		} else hasFound = false;
		
		if (cs.contains("worldName")) {
			String temp_worldName = cs.getString("worldName");
			if (!temp_worldName.equals("default")) { // if not default use this value
				worldName = temp_worldName;
			} // else use the name found in default minecraft file
		} else hasFound = false;
		
		if (cs.contains("centerX")) {
			centerX = cs.getInt("centerX");
		} else hasFound = false;
			
		if (cs.contains("centerZ")) {
			centerZ = cs.getInt("centerZ");
		} else hasFound = false;
		
		if (cs.contains("depth")) {
			depth = cs.getInt("depth");
		} else hasFound = false;
		
		if (cs.contains("remove")) {
			removeArena = cs.getBoolean("remove");
		} else hasFound = false;
		
		if (cs.contains("spawnInArena")) {
			spawnInArena = cs.getBoolean("spawnInArena");
		} else hasFound = false;
		
		if (cs.contains("updating")) {
			updating = cs.getBoolean("updating");
		} else hasFound = false;
		
		return hasFound;
	}
	
	private void loadConfig() {
		// load once section
		if (getConfig().isConfigurationSection("loadOnce")) {
			ConfigurationSection cs = getConfig().getConfigurationSection("loadOnce");
			loadOnceIn(cs);
		} else {
			// get from old setup, without sections
			ConfigurationSection cs = getConfig().getDefaultSection();
			log.info("Using 1.2.1s location for config");
			boolean result = loadOnceIn(cs);
			if (!result) log.warning("Couldn't find the loadOnce section, defaulting...");
		}
		
		// The refreshed section
		refreshConfig();
		
		// recreate the config file
		try {
			File f = new File(getDataFolder().getCanonicalPath() + File.separator + "config.yml");
			if (f.exists()) {
				f.delete();
				this.saveDefaultConfig();
			}
		} catch (Exception e) {
			log.info("Error creating config file");
		}
		
		// load once section
		Map<String, Object> loadOnce = new LinkedHashMap<String, Object>(new HashMap<String, Object>());
		loadOnce.put("worldName", worldName);
		loadOnce.put("centerX", centerX);
		loadOnce.put("centerZ", centerZ);
		loadOnce.put("radius", radius);
		loadOnce.put("depth", depth);
		loadOnce.put("spawnInArena", spawnInArena);
		loadOnce.put("remove", removeArena);
		loadOnce.put("updating", updating);
		getConfig().createSection("loadOnce", loadOnce);
		
		// refresh section
		Map<String, Object> refresh = new LinkedHashMap<String, Object>(new HashMap<String, Object>());
		refresh.put("items", items);
		refresh.put("tribHearDist", tribHearDist);
		refresh.put("protectGamesTNT", protectGamesTNT);
		refresh.put("unOP", unOP);
		refresh.put("compassPoint", compassPoint);
		refresh.put("dayAtStart", dayAtStart);
		ConfigurationSection refreshSec = getConfig().createSection("refresh", refresh);
		
			// auto load within the refresh section
			Map<String, Object> autorun = new LinkedHashMap<String, Object>(new HashMap<String, Object>());
			autorun.put("doAutorun", doAutorun);
			autorun.put("betweenGamesSec", betweenGamesSec);
			autorun.put("startingTribs", startingTribs);
			autorun.put("randomPickTribs", randomPickTribs);
			autorun.put("tribWaitSec", tribWaitSec);
			autorun.put("respawnItemsTime", respawnItemsTime);
			refreshSec.createSection("autorun", autorun);
		
		this.saveConfig();
	}
	
	private void loadWorldName() {
		try {
			BufferedReader is = new BufferedReader(new FileReader(new File("./server.properties")));
			String line = is.readLine();
			
			while(line != null) {
				String[] splitLine = line.trim().split("=");
				if (splitLine.length == 2) {
					if (splitLine[0].equals("level-name")) {
						worldName = splitLine[1].trim();
						break;
					}			
				}
				
				line = is.readLine();
			}
			
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean loadWorld() {		
		// Load world and create blocks
		if (world != null) {
			loadBinFile();
			blockSaver.loadBlockFile();
			cGen.randItems.loadItems();
			
			// so can use the spawn location
			world.setKeepSpawnInMemory(true);
			
			if (updating) {
				ConfigurationSection cs = getConfig().getConfigurationSection("loadOnce");
				cs.set("updating", false);
				saveConfig();
				
				// set the locations
				arenaSpawnLoc = new Location(world, cGen.CornustartX + CustomGen.SPAWN_X_OFF, 
						world.getHighestBlockYAt(cGen.CornustartX, cGen.CornustartZ), 
						cGen.CornustartZ + CustomGen.SPAWN_Z_OFF);
				arenaSpectateLoc = new Location(world, cGen.CornustartX, 
						world.getHighestBlockYAt(cGen.CornustartX, cGen.CornustartZ) + 20, 
						cGen.CornustartZ);;
				
				// delete the bin files
				deleteBinFiles();
						
				log.info("Registered updating The Hunger Games");
			}
			
			if ((hasCreatedArena && removeArena) || (removeArena && updating)) {
				removeArena();
				log.warning("The Hunger Games plugin will now be stopped, reconfigure 'config.yml' and restart");
				return false;
			} else if (updating) {
				log.warning("Updating but the arena will not be removed.");
			} else if (!hasCreatedArena && removeArena) {
				log.warning("The arena hasn't been made yet so it couldn't be removed");
			}
			
			if (!hasCreatedArena && !updating) {
				Location[] lA = cGen.createArena(world, spawnInArena);
				if (lA != null) {
					if (lA.length == 2) {
						arenaSpawnLoc = lA[0];
						arenaSpectateLoc = lA[1];
						hasCreatedArena = true;
					}
				} else {
					// failed to create arena, shutdown plugin
					log.warning("Canceled arena creation");
					return false;
				}
			} else if (gameReady() && !updating) {
				// TODO for now just clear the chests to prevent errors
				cGen.clearChests(world);
				recreateChests(world);
			// if updating don't create the arena
			} else if (updating) {
				// TODO change if updating changes
				log.warning("Updating failed to work correctly!");
				return false;
			}
		} else {
			// failed to load world, shutdown plugin
			log.warning("Could not find the world named <" + worldName + ">");
			return false;
		}
		
		// something screwed up
		if (arenaSpawnLoc == null || arenaSpectateLoc == null || !hasCreatedArena) {
			log.warning("Failed to load The Hunger Games plugin!");
			return false;
		}
		
		return true;
	}
	
	private void recreateChests(World w) {
		// TODO recreateChests is the server has been restarted while the games are ready
	}
	
	private void deleteBinFiles() {
		try {
			if (this.getDataFolder().exists()) {
				File f = new File(getDataFolder().getCanonicalPath() + File.separator + "data_" + worldName + ".bin");
				if(f.exists()) f.delete();
				
				f = new File(getDataFolder().getCanonicalPath() + File.separator + "blocks_" + worldName + ".bin");
				if(f.exists()) f.delete();
			}
			log.info("The old bin files have been removed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void removeArena() {
		boolean result = removePhysicalArena();
		if (result) {
			// reset variables
			hasCreatedArena = false;
			// reset configuration
			ConfigurationSection cs = getConfig().getConfigurationSection("loadOnce");
			cs.set("remove", false);
			cs.set("centerX", 0);
			cs.set("centerZ", 0);
			saveConfig();
			
			// delete bin files
			deleteBinFiles();
			
			// if the games are going restart them
			restartGame();
			
			log.info("Done removing the arena");
		}
	}
	
	private boolean removePhysicalArena() {
		Location temp = 
			new Location(world, arenaSpectateLoc.getX(),
					CustomGen.getHeighestFreeBlockAt(
							world, arenaSpectateLoc.getBlockX(), arenaSpectateLoc.getBlockZ())+UP_AMOUNT, 
					arenaSpectateLoc.getZ());
		
		Block b = world.getBlockAt(temp);
		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() - time < 35) {
			if (b.getType() == Material.GLASS && b.getData() == (byte) 6) {
				log.info("Removing the arena");
				cGen.removeArena(world, temp, arenaSpawnLoc);
				return true;
			}
			Vector v = new Vector(0,-1,0);
			b = world.getBlockAt(temp.add(v));
		}
		log.warning("Failed to find the physical arena! Have the values in 'config.yml' been changed?");
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private void loadBinFile() {
		try {
			if (this.getDataFolder().exists()) {
				File f = new File(getDataFolder().getCanonicalPath() + File.separator + "data_" + worldName + ".bin");
				if(f.exists()) {
					try {	
						ObjectInputStream is = new ObjectInputStream(new FileInputStream(f));
						
						hasCreatedArena = is.readBoolean();
						int[] arenaAry = (int[]) is.readObject();
						arenaSpawnLoc = new Location(world, arenaAry[0], arenaAry[1], arenaAry[2]);
						
						int[] spectArr = (int[]) is.readObject();
						arenaSpectateLoc = new Location(world, spectArr[0], spectArr[1], spectArr[2]);
						
						Object trib = is.readObject();
						if (trib instanceof ConcurrentHashMap<?, ?>) {
							tributes = (ConcurrentHashMap<String, Byte>) trib;
							tributeCount = tributes.size();
						} else {
							log.warning("Invalid type for tribute's HashMap in bin (ignore if updating)");
						}
						
						Object spec = is.readObject();
						if (spec instanceof ConcurrentHashMap<?, ?>) {
							spectators = (ConcurrentHashMap<String, Byte>) spec;
						} else {
							log.warning("Invalid type for spectator's HashMap in bin (ignore if updating)");
						}
						
						gameHasStarted = is.readBoolean();
						
						gamePrepare = is.readBoolean();
						
						gameAccept = is.readBoolean();
						
						Object gms = is.readObject();
						if (gms instanceof ConcurrentHashMap<?, ?>) {
							gamemakers = (ConcurrentHashMap<String, Byte>) gms;
						} else {
								log.warning("Invalid type for gamemaker's HashMap in bin (ignore if updating)");
						}
						
						radius = is.readInt();
						
						is.close();
					} catch (Exception e) {
						// error loading
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void saveBinFile() {
		try {
			long start = System.currentTimeMillis();
			if(!getDataFolder().getCanonicalFile().exists()) getDataFolder().getCanonicalFile().mkdir();
			
			File f = new File(getDataFolder().getCanonicalPath() + File.separator + "data_" + worldName + ".bin");
			if (f.exists()) f.delete();
			f.createNewFile();
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
			os.writeBoolean(hasCreatedArena);
			os.writeObject(new int[] {arenaSpawnLoc.getBlockX(), arenaSpawnLoc.getBlockY(), arenaSpawnLoc.getBlockZ()});
			os.writeObject(new int[] {arenaSpectateLoc.getBlockX(), arenaSpectateLoc.getBlockY(), arenaSpectateLoc.getBlockZ()});
			os.writeObject(tributes);
			os.writeObject(spectators);
			os.writeBoolean(gameHasStarted);
			os.writeBoolean(gamePrepare);
			os.writeBoolean(gameAccept);
			os.writeObject(gamemakers);
			// in case configuration file is messed up after first run save radius because we need it
			os.writeInt(radius);
			os.flush();
			os.close();
			log.info("Saved bin in " + (System.currentTimeMillis() - start) + "ms.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void tpToSpawn(Player p) {
		Location l = p.getWorld().getSpawnLocation();
		p.setNoDamageTicks(60);
		p.setVelocity(new Vector(0,0,0));
		tpTo(p, new Location(p.getWorld(),
				l.getBlockX() + 0.5, 
				p.getWorld().getHighestBlockYAt(l.getBlockX(), l.getBlockZ()) + UP_AMOUNT, 
				l.getBlockZ() + 0.5));
	}
	
	/**
	 *  TODO use in place of p.teleport(l);
	 **/
	private void tpTo(Player p, Location l) {
		tpTo(p, l, 0);
	}
	private void tpTo(Player p, Location l, int yOffset) {
		int x = l.getBlockX();
		int z = l.getBlockZ();
		for (int y=l.getBlockY()+2; y<world.getMaxHeight(); y++) {
			if (world.getBlockAt(x, y, z).getType() == Material.AIR &&
					world.getBlockAt(x, y+1, z).getType() == Material.AIR) {
				p.teleport(new Location(world, x, y + yOffset, z));
				return;
			}
		}
		
		// if fails just do this
		p.teleport(new Location(world, x, world.getHighestBlockYAt(x, z), z));
	}
		
	protected boolean makeTribute(Player player) {
		if(!tributes.containsKey(player.getName()) && !gamemakers.containsKey(player.getName()) &&
				gameAccept && !gameReady() && tributeCount < CustomGen.MAX_TRIB) {
			
			tributeCount++;
			int i = tributeCount;
			Vector vec = CustomGen.getPos(i);
			Vector uV = new Vector(2, 0, 3);
			vec.add(uV);
			Location tpLoc = new Location(world, 
					arenaSpawnLoc.getX(), 
					arenaSpawnLoc.getY() + 4,
					arenaSpawnLoc.getZ()).add(vec);
			
			// hide gamemakers
			// don't need to hide spectators because the can't spectate till all tribute are in
			// so they will be hidden when they spectate
			hide(gamemakers, null, player);
			
			if (unOP) {
				player.setOp(false);
			}
			
			player.setGameMode(GameMode.SURVIVAL);
			player.setDisplayName(ChatColor.BLUE + "[Trib]" + ChatColor.WHITE + player.getName());
			invSaver.addInventory(player);
			player.getInventory().clear();
			player.getInventory().setHelmet(new ItemStack(Material.AIR, 1));
			player.getInventory().setChestplate(new ItemStack(Material.AIR, 1));
			player.getInventory().setLeggings(new ItemStack(Material.AIR, 1));
			player.getInventory().setBoots(new ItemStack(Material.AIR, 1));
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(20);
			player.setVelocity(new Vector(0,0,0));
			tpTo(player, tpLoc);
			tributes.put(player.getName(), (byte) 0);
			saveBinFile();
			player.setVelocity(new Vector(0,-1,0));
			return true;
		}
		return false;
	}
	
	private void spectate(Player p) {
		if(gameReady()) {
			
			// tribute can't see
			hideFrom(tributes, null, p);
			// can't see gamemakers
			hide(gamemakers, null, p);
	
			spectators.put(p.getName(), (byte) 0);
			p.setGameMode(GameMode.SURVIVAL);
			p.eject();
			p.setAllowFlight(true);
			p.setFlying(true);
			this.getServer().broadcastMessage(ChatColor.BLUE + p.getName() + ChatColor.WHITE + " has become a spectator.");
			p.setDisplayName(ChatColor.BLUE + "[Spec]" + ChatColor.WHITE + p.getName());
			p.setVelocity(new Vector(0,0,0));
			tpTo(p, arenaSpectateLoc);
			saveBinFile();
		} else {
			p.sendMessage(ChatColor.RED + "The Games haven't started!");
		}
	}
	
	// pass remove as false to prevent concurrent modification
	private void unspectateNoMessage(Player p) {
		// tribute can see
		showTo(tributes, null, p);
		// can see gamemakers
		show(gamemakers, null, p);
	
		
		spectators.remove(p.getName());
		unSpectators.remove(p.getName());
		
		p.setAllowFlight(false);
		tpToSpawn(p);
		p.setDisplayName(p.getName());
		saveBinFile();
		p.setDisplayName(ChatColor.WHITE + p.getName());
	}
	private void unspectate(Player p) {
		if (spectators.containsKey(p.getName())) {
			unspectateNoMessage(p);
			this.getServer().broadcastMessage(ChatColor.BLUE + p.getName() + ChatColor.WHITE + " is no longer a spectators.");
		}
	}
	
	private void offlineUnspectate(String name) {
		if (spectators.containsKey(name)) {
			spectators.remove(name);
		}
		unSpectators.put(name, (byte) 0);
		
		saveBinFile();
		this.getServer().broadcastMessage(ChatColor.BLUE + name + ChatColor.WHITE + " is no longer a spectators.");
	}
	
	private boolean makeGameMaster(Player p) {
		if(!gamemakers.containsKey(p.getName()) && !tributes.containsKey(p.getName())) {
			if (spectators.containsKey(p.getName())) {
				unspectate(p);
			}
			
			hideFrom(tributes, spectators, p);
			
			gamemakers.put(p.getName(), (byte) 0);			
			p.setGameMode(GameMode.CREATIVE);
			p.setVelocity(new Vector(0,0,0));
			tpTo(p, arenaSpectateLoc);
			this.getServer().broadcastMessage(
					ChatColor.BLUE + p.getName() + 
					ChatColor.WHITE + " has become a Gamemaker");
			p.setDisplayName(ChatColor.BLUE + "[GM]" + ChatColor.WHITE + p.getName());
			
			return true;
		}
		return false;
	}
	
	private void unGameMaster(Player p) {
		if(gamemakers.containsKey(p.getName())) {
			gamemakers.remove(p.getName());
			
			showTo(tributes, spectators, p);
			
			p.setGameMode(GameMode.SURVIVAL);
			tpToSpawn(p);
			
			this.getServer().broadcastMessage(
					ChatColor.BLUE + p.getName() + 
					ChatColor.WHITE + " is no longer a Gamemaker");
			p.setDisplayName(ChatColor.WHITE + p.getName());
		}
	}
	
	public boolean gameReady() {
		return gameHasStarted || gamePrepare;
	}
	
	public boolean doProtectGame() {
		return !gameHasStarted;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}
		
		if (player != null) {
			if (cmd.getName().equalsIgnoreCase("games-listTrib") && args.length == 0) {
				String tribString = "";
				Iterator<String> it = tributes.keySet().iterator();
				while (it.hasNext()) {
					tribString += it.next();
					if (it.hasNext())
						tribString += ", ";
				}
				
				sender.sendMessage(ChatColor.GRAY + "Tributes: " + ChatColor.AQUA + tribString);
				return true;
			} else if (cmd.getName().equalsIgnoreCase("games-listSpec") && args.length == 0) {
				String specString = "";
				Iterator<String> it = spectators.keySet().iterator();
				while (it.hasNext()) {
					specString += it.next();
					if (it.hasNext())
						specString += ", ";
				}
				
				sender.sendMessage(ChatColor.GRAY + "spectators: " + ChatColor.AQUA + specString);
				return true;
			} else if (cmd.getName().equalsIgnoreCase("games-listGm") && args.length == 0) {
				String gmString = "";
				Iterator<String> it = gamemakers.keySet().iterator();
				while (it.hasNext()) {
					gmString += it.next();
					if (it.hasNext())
						gmString += ", ";
				}
				
				sender.sendMessage(ChatColor.GRAY + "Gamemasters: " + ChatColor.AQUA + gmString);
				return true;
			}
			
			// spectators commands
			if (spectators.containsKey(player.getName())) {
				if (cmd.getName().equalsIgnoreCase("games-unspec")) {
					unspectate(player);
					return true;
				}
				
				sender.sendMessage(ChatColor.RED + "Spectators can only use the unSpectate and List commands");
				return true;
			}
		
			// tribute commands
			if (tributes.containsKey(player.getName())) {
				if (cmd.getName().equalsIgnoreCase("games-unTrib")) {
					onlineTributeKill(player);
					return true;
				}
				
				sender.sendMessage(ChatColor.RED + "Tributes can only use unTribute and List commands");
				return true;
			}
		}
		
		// Player commands
		if (cmd.getName().equalsIgnoreCase("games-volunteer") && args.length == 0) {
			if (player != null) {
				if (tributeCount < CustomGen.MAX_TRIB && !gameReady() && gameAccept) {
					boolean result = makeTribute(player);
					
					if (result) {
						this.getServer().broadcastMessage(ChatColor.BLUE + player.getDisplayName() + 
								ChatColor.WHITE + " has volunteered as tribute!");
					} else {
						sender.sendMessage(ChatColor.RED + "You are already involved in The Games.");
					}
				} else if (!gameAccept) {
					sender.sendMessage(ChatColor.RED + "The Games are not accepting tributes at this time");
				} else if (tributeCount > CustomGen.MAX_TRIB) {
					sender.sendMessage(ChatColor.RED + "There are already " + CustomGen.MAX_TRIB + " tributes!");
				} else if (gameReady()) {
					sender.sendMessage(ChatColor.RED + "The Games have already started!");
				} else {
					sender.sendMessage(ChatColor.RED + "You could not be added as a tribute");
				}
				return true;
			} else {
				log.info("Only players can use that command");
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("games-spec") && args.length == 0) {
			if (player != null) {
				spectate(player);
				return true;
			} else {
				log.info("Only players can use that command");
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("games-start") && args.length == 0) {
			if (sender.isOp()) {
				if(gamePrepare && !gameHasStarted && tributeCount >= MIN_TRIB) {
					startGame();
					saveBinFile();
				} else if (gameHasStarted) {
					sender.sendMessage(ChatColor.RED + "The Game has already started.");
				} else if (!gamePrepare){
					sender.sendMessage(ChatColor.RED + "The Game hasn't been prepared. (/games-prepare)");
				} else {
					sender.sendMessage(ChatColor.RED + "The Game has already started or there are < 2 tributes");
				}
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("games-end") && args.length == 0) {
			if (player != null) {
				if (player.isOp()) {
					if (gameReady() || gameAccept) {
						if(gamemakers.containsKey(player.getName())) {
							restartGame();
							this.getServer().broadcastMessage(ChatColor.BLUE + "The Games have ended early!");
						} else {
							sender.sendMessage(ChatColor.RED + "You don't have GM permissions");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "The Games haven't started!");
					}
					return true;
				}
			} else {
				restartGame();
				this.getServer().broadcastMessage(ChatColor.BLUE + "The Games have ended early!");
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("games-prepare") && args.length == 0) {
			if (sender.isOp()) {
				if(!gameReady() && gameAccept && tributeCount >= MIN_TRIB) {
					prepareGame();
					sender.sendMessage(ChatColor.AQUA + "The Game has been prepared");
					saveBinFile();
				} else if (!gameAccept) {
					sender.sendMessage(ChatColor.RED + "The Game must be accepting tributes first! Use /games-accept");
				} else if (gamePrepare) {
					sender.sendMessage(ChatColor.RED + "The Game is already prepared. Use /games-start to start.");
				} else if (gameHasStarted) {
					sender.sendMessage(ChatColor.RED + "The Game has already started");
				} else {
					sender.sendMessage(ChatColor.RED + "The Game can't prepare there are < 2 tributes");
				}
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("games-accept") && args.length == 0) {
			if (sender.isOp()) {
				if (!gameReady() && !gameAccept) {
					acceptTributes();
				} else if(gameReady()) {
					sender.sendMessage(ChatColor.RED + "The Games has already passed this phase!");
				} else if(gameAccept) {
					sender.sendMessage(ChatColor.RED + "The Games are already accepting tributes!");
				}
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("games-tribute") && args.length >= 1) {
			if (sender.isOp()) {
				Player p = this.getServer().getPlayer(args[0]);
				if (p != null) {
					if (p.isOnline()) {
						boolean success = makeTribute(p);
						if (success) {
							this.getServer().broadcastMessage(ChatColor.BLUE + p.getName() + ChatColor.WHITE + " has been chosen as a tribute!");
						} else {
							sender.sendMessage(ChatColor.RED + p.getName() + " couldn't be made a tribute!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + args[0] + " isn't online!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
				}
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("games-tpspawn") && args.length >= 1) {
			if (sender.isOp()) {
				Player p = this.getServer().getPlayer(args[0]);
				if (p != null) {
					if (p.isOnline()) {
						// can't tp tributes
						if (!tributes.containsKey(p.getName())) {
							tpToSpawn(p);
							// unspectate if they radius a spectators
							if (spectators.containsKey(p.getName())) {
								unspectate(p);
							}
							p.sendMessage(ChatColor.AQUA + sender.getName() + " sent you to the spawn.");
						} else {
							sender.sendMessage(p.getName() + " could not be teleported, they are a tribute");
						}
					} else {
						sender.sendMessage(ChatColor.RED + args[0] + " isn't online!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
				}
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("games-gamemaker") && (args.length == 0 || args.length == 1)) {
			if (!gameReady()) {
				if (args.length == 0) {
					if (player != null && sender.isOp()) {
						boolean result = makeGameMaster(player);
						
						if (!result) {
							sender.sendMessage(ChatColor.RED + "You are already involved in The Games!");
						}
						return true;
					} else {
						sender.sendMessage(ChatColor.RED + "You must specify a player");
						return true;
					}
				} else if (args.length == 1){
					if (sender.isOp()) {
						Player otherP = this.getServer().getPlayer(args[0]);
						if (otherP != null) {
							if (otherP.isOnline()) {
								boolean result = makeGameMaster(otherP);
								
								if (!result) {
									sender.sendMessage(ChatColor.RED + args[0] + " is already involved in The Games!");
								}
							} else {
								sender.sendMessage(ChatColor.RED + args[0] + " isn't online!");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
						}
					}
					return true;
				}
			} else {
				if (sender.isOp()) {
					sender.sendMessage(ChatColor.RED + "The Games have already started!");
					return true;
				}
			}
		} else if(cmd.getName().equalsIgnoreCase("games-ungamemaker") && (args.length == 0 || args.length == 1)) {
			if (sender.isOp()) {
				if (args.length == 0) {
					if (player != null) {
						unGameMaster(player);
					} else {
						sender.sendMessage(ChatColor.RED + "You must specify a player");
					}
					return true;
				} else {
					Player otherP = this.getServer().getPlayer(args[0]);
					if (otherP != null) {
						if (otherP.isOnline()) {
							unGameMaster(otherP);
						} else {
							sender.sendMessage(ChatColor.RED + args[0] + " isn't online!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "That player doesn't exist!");
					}
					return true;
				}
			}
		} else if (cmd.getName().equalsIgnoreCase("games-unTrib") && args.length == 1) {
			if (sender.isOp()) {
				if (tributes.containsKey(args[0])) {
					Player tribP = this.getServer().getPlayer(args[0]);
					// if they are online kill, else mark
					if (tribP != null) {
						onlineTributeKill(tribP);
					} else {
						tributeMarkForRemoval(args[0]);
					}
				} else {
					sender.sendMessage(ChatColor.RED + args[0] + " is not a tribute");
				}
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("games-unSpec") && args.length == 1) {
			if (sender.isOp()) {
				if (spectators.containsKey(args[0])) {
					Player specP = this.getServer().getPlayer(args[0]);
					// if they are online kill, else mark
					if (specP != null) {
						unspectate(specP);
					} else {
						spectatorMarkForRemoval(args[0]);
					}
				} else {
					sender.sendMessage(ChatColor.RED + args[0] + " is not a spectator");
				}
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("games-refreshConfig") && args.length == 0) {
			if (sender.isOp()) {
				refreshConfig();
				sender.sendMessage(ChatColor.AQUA + "The config has been reloaded");
				return true;
			}
		} else if(cmd.getName().equalsIgnoreCase("games-notribute") && args.length == 0) {
			if (player != null) {
				if (autorun != null) {
					dontChoose.add(player.getName());
					player.sendMessage(ChatColor.AQUA + "You will not be auto-chosen");
				} else {
					player.sendMessage(ChatColor.RED + "The Games are not auto-running");
				}
			} else {
				sender.sendMessage("You must be a player to use this command");
			}
			return true;
		} else if(cmd.getName().equalsIgnoreCase("games-dotribute") && args.length == 0) {
			if (player != null) {
				if (autorun != null) {
					dontChoose.remove(player.getName());
					player.sendMessage(ChatColor.AQUA + "You can be auto-chosen");
					autorun.autoChoosePlayerAsTribute(player, false);
				} else {
					player.sendMessage(ChatColor.RED + "The Games are not auto-running");
				}
			} else {
				sender.sendMessage("You must be a player to use this command");
			}
			return true;
		} else if(cmd.getName().equalsIgnoreCase("games-state") && args.length == 0) {
			if (gameAccept)
				sender.sendMessage(ChatColor.AQUA + "The Games are accepting");
			else if (gamePrepare)
				sender.sendMessage(ChatColor.AQUA + "The Games are prepared");
			else if (gameHasStarted)
				sender.sendMessage(ChatColor.AQUA + "The Games have started");
			else
				sender.sendMessage(ChatColor.AQUA + "The Games are dorment");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("games-storm") && args.length == 0) {
			if (sender.isOp()) {
				world.setStorm(!world.hasStorm());
				if (world.hasStorm()) {
					world.setWeatherDuration(Integer.MAX_VALUE);
					sender.sendMessage(ChatColor.AQUA + "A storm has stared");
				} else {
					sender.sendMessage(ChatColor.AQUA + "The storm has ended");
				}
			}
			return true;
		}
		
		return false;
	}
	
	private void resetGameState() {
		setGameState(false, false, false, true);
	}
	private void setGameState(boolean accept, boolean prepare, boolean started) {
		setGameState(accept, prepare, started, false);
	}
	private void setGameState(boolean accept, boolean prepare, boolean started, boolean reset) {
		gameAccept = accept;
		gamePrepare = prepare;
		gameHasStarted = started;
		
		if (autorun != null) {
			// do even if not auto-running, will just ignore. Helps prevent errors if reload
			if (gameHasStarted)
				autorun.gamesStarted();
			if (gamePrepare)
				autorun.gamesPrepated();
			if (gameAccept)
				autorun.gamesAccepting();
			if (reset)
				autorun.gamesReset();
		}
		
		saveBinFile();
	}
	
	protected void acceptTributes() {
		setGameState(true, false, false);
		
		// if want to be day at start
		this.getServer().broadcastMessage(ChatColor.BLUE + "The Hunger Games is now accepting tributes!");
		
	}
	
	protected void prepareGame() {				
		// if want to be day at start
		if (dayAtStart)
			world.setTime(0);
		
		// clear old items
		cGen.removeItems(world);
		
		// create the item chests
		cGen.createChests(world);
		
		String[] tributeArray = tributes.keySet().toArray(new String[0]);
		for (int i=0; i<CustomGen.MAX_TRIB; i++) {
			Vector vec = CustomGen.getPos(i);
			
			Vector uV = new Vector(2, 0, 3);
			vec.add(uV);
			
			Location tpLoc = new Location(world, 
					arenaSpawnLoc.getBlockX() - 1, 
					world.getHighestBlockYAt((int) (arenaSpawnLoc.getBlockX() - 1 + vec.getX()), (int) (arenaSpawnLoc.getBlockZ() + vec.getZ())) + 3,
					arenaSpawnLoc.getBlockZ()).add(vec);
			
			// create floor			
			CustomGen.turnToUnbreakable(
					world, Material.GLASS, tpLoc.getBlockX(), tpLoc.getBlockY(), tpLoc.getBlockZ(), true);
			CustomGen.turnToUnbreakable(
					world, Material.GLASS, tpLoc.getBlockX() + 1, tpLoc.getBlockY(), tpLoc.getBlockZ(), true);
			CustomGen.turnToUnbreakable(
					world, Material.GLASS, tpLoc.getBlockX(), tpLoc.getBlockY(), tpLoc.getBlockZ() - 1, true);
			CustomGen.turnToUnbreakable(
					world, Material.GLASS, tpLoc.getBlockX() + 1, tpLoc.getBlockY(), tpLoc.getBlockZ() - 1, true);
			
			if (i < tributeArray.length) {
				String name = tributeArray[i];
				Player pTrib = this.getServer().getPlayer(name);
				if (pTrib != null) {
					// tp the player
					tpLoc.setY(tpLoc.getBlockY() + UP_AMOUNT);
					// move back to center, it needed to be moved to make the block
					tpLoc.setX(tpLoc.getBlockX() + 1);
					pTrib.setHealth(pTrib.getMaxHealth());
					pTrib.setVelocity(new Vector(0,0,0));
					tpTo(pTrib, tpLoc, UP_AMOUNT);
				} else {
					tributeMarkForRemoval(name);
				}
			}
		}
		
		setGameState(false, true, false);
	}
	
	protected void startGame() {	
		if (dayAtStart)
			world.setTime(0);
		
		for (int i=0; i<CustomGen.MAX_TRIB; i++) {
			Iterator<String> it = tributes.keySet().iterator();
			while (it.hasNext()) {
				String name = it.next();
				Player pTrib = this.getServer().getPlayer(name);
				if (pTrib != null) {
					pTrib.setHealth(pTrib.getMaxHealth());
				} else {
					tributeMarkForRemoval(name);
				}
			}
			
			Vector vec = CustomGen.getPos(i);
			
			Vector uV = new Vector(2, 0, 3);
			vec.add(uV);
			
			Location tpLoc = new Location(world, 
					arenaSpawnLoc.getBlockX() - 1, 
					world.getHighestBlockYAt((int) (arenaSpawnLoc.getBlockX() - 1 + vec.getX()), (int) (arenaSpawnLoc.getBlockZ() + vec.getZ())) + 3,
					arenaSpawnLoc.getBlockZ()).add(vec);
			
			// clear plates
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX() - 1, tpLoc.getBlockY()+2, tpLoc.getBlockZ(), true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX() - 1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 1, true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX(), tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 2, true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX() + 1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 2, true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX() + 2, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 1, true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX() + 2, tpLoc.getBlockY()+2, tpLoc.getBlockZ(), true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX() + 1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() + 1, true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX(), tpLoc.getBlockY()+2, tpLoc.getBlockZ() + 1, true);
			
			// new corners
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX()-1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 2, true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX()+2, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 2, true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX()+2, tpLoc.getBlockY()+2, tpLoc.getBlockZ() + 1, true);
			CustomGen.turnToUnbreakable(
					world, Material.AIR, tpLoc.getBlockX()-1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() + 1, true);
		}
		
		// started game
		setGameState(false, false, true);
		this.getServer().broadcastMessage(ChatColor.BLUE + "The Games have started!");
	}
	
	private synchronized void restartGame() {
		// Always reset players so can use to help clear everyone
		
		// concurrent mod
		String[] specCopy = spectators.keySet().toArray(new String[0]).clone();
		for (String spec:specCopy) {
			Player pSpec = this.getServer().getPlayer(spec);
			if (pSpec != null) {
				unspectate(pSpec);
			} else {
				spectatorMarkForRemoval(spec);
			}
		}
		
		// concurrent mod
		String[] tribCopy = tributes.keySet().toArray(new String[0]).clone();
		for (String trib:tribCopy) {
			Player pTrib = this.getServer().getPlayer(trib);
			if (pTrib != null) {
				onlineTributeKill(pTrib, false);
			} else {
				tributeMarkForRemoval(trib);
			}
		}
		tributeCount = 0;
		
		// only do if game is currently running so isn't called twice in one game
		if (gameReady() || gameAccept) {
			resetGameState();
			
			
			blockSaver.restore(world);
			// reset everything
			cGen.clearChests(world);
			
			// clear items from previous games
			cGen.removeItems(world);
			
			for(int i=0; i<CustomGen.MAX_TRIB; i++) {
				Vector vec = CustomGen.getPos(i);
				
				Vector uV = new Vector(2, 0, 3);
				vec.add(uV);
				
				Location tpLoc = new Location(world, 
						arenaSpawnLoc.getBlockX() - 1, 
						world.getHighestBlockYAt((int) (arenaSpawnLoc.getBlockX() - 1 + vec.getX()), (int) (arenaSpawnLoc.getBlockZ() + vec.getZ())) + 3,
						arenaSpawnLoc.getBlockZ()).add(vec);
				
				// clear floor			
				CustomGen.turnToUnbreakable(
						world, Material.AIR, tpLoc.getBlockX(), tpLoc.getBlockY(), tpLoc.getBlockZ(), true);
				CustomGen.turnToUnbreakable(
						world, Material.AIR, tpLoc.getBlockX() + 1, tpLoc.getBlockY(), tpLoc.getBlockZ(), true);
				CustomGen.turnToUnbreakable(
						world, Material.AIR, tpLoc.getBlockX(), tpLoc.getBlockY(), tpLoc.getBlockZ() - 1, true);
				CustomGen.turnToUnbreakable(
						world, Material.AIR, tpLoc.getBlockX() + 1, tpLoc.getBlockY(), tpLoc.getBlockZ() - 1, true);
				// clear plates
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX() - 1, tpLoc.getBlockY()+2, tpLoc.getBlockZ(), true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX() - 1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 1, true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX(), tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 2, true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX() + 1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 2, true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX() + 2, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 1, true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX() + 2, tpLoc.getBlockY()+2, tpLoc.getBlockZ(), true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX() + 1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() + 1, true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX(), tpLoc.getBlockY()+2, tpLoc.getBlockZ() + 1, true);
				
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX()-1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 2, true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX()+2, tpLoc.getBlockY()+2, tpLoc.getBlockZ() - 2, true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX()+2, tpLoc.getBlockY()+2, tpLoc.getBlockZ() + 1, true);
				CustomGen.turnToUnbreakable(
						world, Material.STONE_PLATE, tpLoc.getBlockX()-1, tpLoc.getBlockY()+2, tpLoc.getBlockZ() + 1, true);
		
			}
		}
	}
	
	private boolean noGMaker(Player p) {
		return !gamemakers.containsKey(p.getName());
	}
	
	public boolean inArena(Location l) {
		if (!l.getWorld().equals(world))
			return false;
		
		return Math.abs(l.getX() - arenaSpectateLoc.getX()) <= radius && Math.abs(l.getZ() - arenaSpectateLoc.getZ()) <= radius;
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (spectators.containsKey(p.getName())) {
			if (lastGiveItem.containsKey(p.getName())) {
				if (System.currentTimeMillis() - lastGiveItem.get(p.getName()) < 60000) {
					e.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You have given an item within the last minute!");
				} else {
					lastGiveItem.put(p.getName(), System.currentTimeMillis());
				}
			} else {
				lastGiveItem.put(p.getName(), System.currentTimeMillis());
			}
		}
		
		if (doProtectGame() && inArena(p.getLocation()) && noGMaker(p)) {
			p.sendMessage(ChatColor.RED + "The Games haven't started yet!");
			e.setCancelled(true);
			return;
		}
	}
	
	@EventHandler
	public void onEntityDamaged(EntityDamageByEntityEvent e) {
		if (e.getEntityType() == EntityType.PLAYER) {
			Player p = (Player) e.getEntity();
			if(spectators.containsKey(p.getName())) {
				e.setCancelled(true);
				return;
			}
		}
		if (e.getDamager() instanceof Player) {
			Player killer = (Player) e.getDamager();
			if(spectators.containsKey(killer.getName())) {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onEntityDamagedByBlock(EntityDamageByBlockEvent e) {
		if(e.getEntityType() == EntityType.PLAYER) {
			Player p = (Player) e.getEntity();
			if (spectators.containsKey(p.getName())) {
				e.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onBlockDestroyed(BlockBreakEvent e) {
		String name = e.getPlayer().getName();
		if (spectators.containsKey(name)) {
			e.setCancelled(true);
			return;
		} else if (gamemakers.containsKey(name)) {
			return;
		}
		
		if (doProtectGame() && inArena(e.getBlock().getLocation()) && noGMaker(e.getPlayer())) {
			e.getPlayer().sendMessage(ChatColor.RED + "The Games haven't started yet!");
			e.setCancelled(true);
			return;
		}
		
		Block b = e.getBlock();
		if (b.getType() == Material.GLASS || b.getType() == Material.SMOOTH_BRICK ||
				b.getType() == Material.WOOD_PLATE || b.getType() == Material.STONE_PLATE ||
				b.getType() == Material.GOLD_BLOCK || b.getType() == Material.PISTON_STICKY_BASE) {
			if (b.getData() == (byte) 6) {
				e.setCancelled(true);
				// retain the data
				b.setData((byte) 6);
			}
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent e) {		
		ArrayList<Block> remove = new ArrayList<Block>();
		for(Block b:e.blockList()) {
			// if in arena
			if (inArena(b.getLocation())) {
				// If not protect all, protect just invincible
				if ((b.getType() == Material.GLASS || b.getType() == Material.SMOOTH_BRICK ||
						b.getType() == Material.WOOD_PLATE || b.getType() == Material.STONE_PLATE ||
						b.getType() == Material.GOLD_BLOCK || b.getType() == Material.PISTON_STICKY_BASE) && 
						b.getData() == (byte) 6) {
					remove.add(b);
					// retain the data
					b.setData((byte) 6);
				}
				
				// Protect all blocks in the arena
				if (protectGamesTNT || doProtectGame()) {
					remove.add(b);
				}
			}
		}
		e.blockList().removeAll(remove);
	}
	
	@EventHandler
	public void onPlayerEnterBed(PlayerBedEnterEvent e) {
		playerInBed.put(e.getPlayer().getName(), System.currentTimeMillis());
	}
	
	@EventHandler
	public void onPlayerExitBed(PlayerBedLeaveEvent e) {
		// time in seconds
		float time = (System.currentTimeMillis() - playerInBed.get(e.getPlayer().getName())) / 1000f;
		if (time > 2) {
			Player p = e.getPlayer();
			float reduceAmount = 0.075f * (time / 1f);
			p.setExhaustion((float) (p.getExhaustion() - reduceAmount));
			if (p.getExhaustion() < 0) {
				p.setExhaustion(0);
			}
		}
		playerInBed.remove(e.getPlayer().getName());
	}
	
	// stop ItemBlock items from despawning
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent e) {
		Item i = e.getEntity();
		
		if (i != null) {
			for (ItemBlock ib:cGen.chestBlocks) {
				if (ib != null) {
					if (ib.isThisBlock(i)) {
						e.setCancelled(true);
						return;
					}
				} else {
					log.warning("Null itemblock still in list!");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent e) {
		if (spectators.containsKey(e.getPlayer().getName())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (spectators.containsKey(e.getPlayer().getName())) {
			e.setCancelled(true);
			return;
		}
		
		// When opening a chest created by server add to watch list
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			if (e.getClickedBlock().getType() == Material.BEDROCK && e.getClickedBlock().getData() == (byte) 6) {
				ItemBlock clickedItemBlock = null;
				for (ItemBlock ib:cGen.chestBlocks) {
					if (ib.isThisBlock(e.getClickedBlock())) {
						clickedItemBlock = ib;
						break;
					}
				}
				if (clickedItemBlock == null) {
					e.getClickedBlock().setType(Material.AIR);
					e.getPlayer().getInventory().addItem(new ItemStack(Material.STONE, 8));
					return; // could not find the block
				} else {
					cGen.chestBlocks.remove(clickedItemBlock.getBlock());
					clickedItemBlock.click(e.getPlayer());
				}
			}
		} else if(e.getAction() == Action.PHYSICAL) {
			Block b = e.getClickedBlock();
			if (b.getType() == Material.STONE_PLATE && b.getData() == (byte) 6) {
				// doesn't need to really activate, will screw up data
				e.setCancelled(true);
				b.setData((byte) 6);
				b.getWorld().createExplosion(b.getLocation(), 2.5f);
			} else if (b.getType() == Material.WOOD_PLATE && b.getData() == (byte) 6) {
				e.setCancelled(true);
				b.setData((byte)6);
			}
		} else if(e.getAction() == Action.RIGHT_CLICK_AIR) {
			testPoison(e);
			
			// point compass at other tributes
			if (compassPoint) {
				ItemStack i = e.getItem();
				Player p = e.getPlayer();
				if (i.getType() == Material.COMPASS && 
						tributes.containsKey(p.getName()) || spectators.containsKey(p.getName())) {
					Player otherP = findNearestTributeLoc(p);
					p.setCompassTarget(otherP.getLocation());
					p.sendMessage("Compass pointing at " + otherP.getName());
				}
			}
		}
	}
	
	private void testPoison(PlayerInteractEvent e) {
		ItemStack i = e.getItem();
		Player p = e.getPlayer();
		
		if (i.getType() == Material.APPLE && rand.nextDouble() < 0.025) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 1));
		} else if (i.getType() == Material.MELON && rand.nextDouble() < 0.025) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 1));
		} else if (i.getType() == Material.PORK && rand.nextDouble() < 0.075) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1));
		} else if (i.getType() == Material.PUMPKIN && rand.nextDouble() < 0.025) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 600, 1));
		} else if (i.getType() == Material.RAW_BEEF && rand.nextDouble() < 0.1) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1));
		} else if (i.getType() == Material.RAW_FISH && rand.nextDouble() < 0.15) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1));
		} else if (i.getType() == Material.BREAD && rand.nextDouble() < 0.015) {
			p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 500, 1));
		}
	}
	
	private Player findNearestTributeLoc(Player p) {
		double dist = Integer.MAX_VALUE;
		Player target = p;
		for (String otherName:tributes.keySet()) {
			Player otherPlayer = this.getServer().getPlayer(otherName);
			
			if (otherPlayer != null) {
				if (!otherPlayer.equals(p)) {
					double newDist = otherPlayer.getLocation().distance(p.getLocation());
					if (newDist < dist) {
						dist = newDist;
						target = otherPlayer;
					}
				}
			} else {
				// This tribute isn't online and wasn't removed
				// mark them for removal
				tributeMarkForRemoval(otherName);
			}
		}
		
		return target;
	}
	
	private void tributeMarkForRemoval(String name) {
		tributes.remove(name);
		unTributes.put(name, (byte) 0);
		offlineTributeKill(name);
	}
	private void spectatorMarkForRemoval(String name) {
		spectators.remove(name);
		unSpectators.put(name, (byte)0);
		offlineUnspectate(name);
	}
	
	private void showTo(ConcurrentHashMap<String, Byte> group1, ConcurrentHashMap<String, Byte> group2, Player p) {
		hideFrom(group1, group2, p, true);
	}
	private void hideFrom(ConcurrentHashMap<String, Byte> group1, ConcurrentHashMap<String, Byte> group2, Player p) {
		hideFrom(group1, group2, p, false);
	}
	// hide player from all in group1 and group2
	private void hideFrom(ConcurrentHashMap<String, Byte> group1, ConcurrentHashMap<String, Byte> group2, Player p, boolean showTo) {
		if (group1 != null) {
			for (String g1Name:group1.keySet()) {
				Player g1Player = this.getServer().getPlayer(g1Name);
				if (g1Player != null) {
					if (showTo) {
						g1Player.showPlayer(p);
					} else {
						g1Player.hidePlayer(p);
					}
				} else {
					tributeMarkForRemoval(g1Name);
				}
			}
		}
		
		if (group2 != null) {
			for (String g2Name:group2.keySet()) {
				Player g2Player = this.getServer().getPlayer(g2Name);
				if (g2Player != null) {
					if (showTo) {
						g2Player.showPlayer(p);
					} else {
						g2Player.hidePlayer(p);
					}
				} else {
					spectatorMarkForRemoval(g2Name);
				}
			}
		}
	}
	
	private void show(ConcurrentHashMap<String, Byte> group1, ConcurrentHashMap<String, Byte> group2, Player p) {
		hide(group1, group1, p, true);
	}
	private void hide(ConcurrentHashMap<String, Byte> group1, ConcurrentHashMap<String, Byte> group2, Player p) {
		hide(group1, group2, p, false);
	}
	// hide all in group1 and group2 from player
	private void hide(ConcurrentHashMap<String, Byte> group1, ConcurrentHashMap<String, Byte> group2, Player p, boolean show) {
		if (group1 != null) {
			for (String g1Name:group1.keySet()) {
				Player g1Player = this.getServer().getPlayer(g1Name);
				if (g1Player != null) {
					if (show) {
						p.showPlayer(g1Player);
					} else {
						p.hidePlayer(g1Player);
					}
				} else {
					tributeMarkForRemoval(g1Name);
				}
			}
		}
		
		if (group2 != null) {
			for (String g2Name:group2.keySet()) {
				Player g2Player = this.getServer().getPlayer(g2Name);
				if (g2Player != null) {
					if (show) {
						p.showPlayer(g2Player);
					} else {
						p.hidePlayer(g2Player);
					}
				} else {
					spectatorMarkForRemoval(g2Name);
				}
			}
		}
	}
	
	// at dust show broadcast who is left
	boolean hasShownTributes = false;
	@EventHandler
	public void onPlayerMoved(PlayerMoveEvent e) {
		if (gameHasStarted) {
			// 12000 dusk, 18000 midnight
			long time = this.getServer().getWorld(this.worldName).getTime();
			if(((time > 12000 && time < 17500) || (time > 500 && time < 6000)) && !hasShownTributes) {
				hasShownTributes = true;
				
				String tribString = "";
				Iterator<String> it = tributes.keySet().iterator();
				while (it.hasNext()) {
					tribString += it.next();
					if (it.hasNext())
						tribString += ", ";
				}
				
				this.getServer().broadcastMessage(ChatColor.GRAY + "The tributes remaining are: " + ChatColor.AQUA + tribString);
			} else if (hasShownTributes && ((time > 6000 && time < 12000) || time > 17500 || time < 500)) {
				hasShownTributes = false;
			}
			
			testInBounds(e.getPlayer());
		} else if (this.gamePrepare || this.gameAccept) {
			testInBounds(e.getPlayer());
		}
	}
	
	private void testInBounds(Player p) {
		if (System.currentTimeMillis() - lastTributeCheck > 50) {
			if (tributes.containsKey(p.getName())) {
				if (!inArena(p.getLocation())) {
					onlineTributeKill(p, true);
					p.sendMessage(ChatColor.RED + "You have gone out of bounds!");
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerChat(PlayerChatEvent e) {
		Set<Player> recip = e.getRecipients();
		Player sender = e.getPlayer();
		
		Set<Player> remove = new HashSet<Player>();
		
		if (tributes.containsKey(sender.getName()) || spectators.containsKey(sender.getName())) {
			// tributes can only hear near by other tributes and spectators
			for (Player rP:recip) {
				if (tributes.containsKey(rP.getName())) {
					if (sender.getLocation().distance(rP.getLocation()) > tribHearDist) {
						remove.add(rP);
					}
				}
			}
		} else {
			// tributes can not hear normal chat
			for (Player rP:recip) {
				if (tributes.containsKey(rP.getName())) {
					remove.add(rP);
				}
			}
		}
		
		recip.removeAll(remove);
	}
	
	@EventHandler
	public void onPlayerConnect(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		String name = p.getName();
		
		if (gamemakers.containsKey(name)) {
			hideFrom(tributes, spectators, p);
		} else if (unSpectators.containsKey(name)) {
			unspectateNoMessage(p);
			unSpectators.remove(name);
		} else if (unTributes.containsKey(name)) {
			unTributes.remove(name);
		}
		
		// if don't want them spawning in the arena move
		if (!spawnInArena) {
			// non tributes and spectators in arena are sent to spawn
			if (inArena(p.getLocation())) {
				if(!tributes.containsKey(name) && !spectators.containsKey(name) && noGMaker(p)) {
					this.tpToSpawn(p);
					p.sendMessage(ChatColor.RED + "Non tributes or spectators can't be in the arena");
				}
			}
		}
		
		// randomly pick tributes if are auto-running and in accept stage
		if (doAutorun && autorun != null && gameAccept) {
			autorun.autoChoosePlayerAsTribute(e.getPlayer(), true);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		invSaver.restoreInventory(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerDisconnect(PlayerQuitEvent e) {
		// if a tribute kill them
		onlineTributeKill(e.getPlayer());
		// if spectators unspectate
		unspectate(e.getPlayer());
	}
	
	@EventHandler
	public void onPlayerKilled(PlayerDeathEvent e) {
		Player p = e.getEntity();
		// If spectators somehow dies, unspectate
		if (spectators.containsKey(p.getName())) {
			unspectate(p);
		}
		
		onlineTributeKill(p);
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent e) {
		if (e.getTarget() != null)
			if (e.getTarget().getType() == EntityType.PLAYER) {
				Player targetP = (Player) e.getTarget();
				if (spectators.containsKey(targetP.getName()) || gamemakers.containsKey(targetP.getName())) {
					e.setCancelled(true);
				}
			}
	}
	
	private void onlineTributeKill(Player p) {
		onlineTributeKill(p, false);
	}
	// Call this if they were really killed
	//  pass explosion as false if calling for all tributes (concurrent mod)
	private void onlineTributeKill(Player p, boolean explosion) {
		if (tributes.containsKey(p.getName())) {
			if (explosion) {
				for (String name:tributes.keySet()) {
					if (!name.equals(p.getName())) {
						Player pTrib = this.getServer().getPlayer(name);
						if (pTrib != null) {
							int x = pTrib.getLocation().getBlockX();
							int y = pTrib.getLocation().getBlockY() + 5;
							int z = pTrib.getLocation().getBlockZ();
							world.createExplosion(new Location(world, x, y, z), 0f);
						} else {
							tributeMarkForRemoval(name);
						}
					}
				}
			}
			
			untributeNoMessage(p);
			this.getServer().broadcastMessage(ChatColor.RED + "Tribute " + p.getName() + " has been killed!");
			testForWinner();
		}
	}
	
	// Call this if they need to be removed but are offline
	private void offlineTributeKill(String name) {
		// make sure they are removed from the list
		if (tributes.containsKey(name)) {
			tributes.remove(name);
		}
		if (unTributes.containsKey(name)) {
			unTributes.put(name, (byte) 0);
		}
	}
	
	private void testForWinner() {
		if (tributes.size() == 1 && gameReady()) {
			
			// get first the knowing there is only one left
			// the converting to array and getting first element
			String winner = tributes.keySet().toArray(new String[0])[0];
			Player pWinner = this.getServer().getPlayer(winner);
			tributes.remove(winner);
			if (pWinner != null) {
				pWinner.setHealth(pWinner.getMaxHealth());
				
				// Reward
				invSaver.restoreInventory(pWinner);
				pWinner.giveExp(500);
				Inventory pWinInv = pWinner.getInventory();
				pWinInv.addItem(new ItemStack(Material.DIAMOND_BLOCK, 1));
				pWinInv.addItem(new ItemStack(Material.IRON_BLOCK, 5));
				pWinInv.addItem(new ItemStack(Material.GOLD_BLOCK, 3));
				
				tpToSpawn(pWinner);
				
				// restarts stuff and resets the booleans
				restartGame();
				
				// so message is after reset message
				this.getServer().broadcastMessage(ChatColor.GOLD + "The Hunger Games has a winner!");
				this.getServer().broadcastMessage(ChatColor.GOLD + winner + " is the winner of this Years Hunger Games!");
			} else {
				this.getServer().broadcastMessage(ChatColor.RED + "ERROR declaring a winner! Notify a server GM");
				log.warning("Failed to untribute the winner of the games!");
				restartGame();
			}
		} else if(tributes.size() <= 0 && gameReady()) {
			// restarts stuff and resets the booleans
			restartGame();
			
			// so message is after reset message
			this.getServer().broadcastMessage(ChatColor.GOLD + "The Hunger Games is over!");
			this.getServer().broadcastMessage(ChatColor.GOLD + "All tributes are dead, there is NO WINNER!");
		}
	}
	
	
	private void untributeNoMessage(Player p) {
		untributeNoMessage(p, true);
	}
	private void untributeNoMessage(Player p, boolean kill) {
		if (p != null) {
			String name = p.getName();
			
			tributes.remove(name);
			unTributes.remove(name);
			p.setDisplayName(name);
			
			if (kill) {
				p.setHealth(0);
			} else {
				tpToSpawn(p);
			}
			
			show(spectators, gamemakers, p);
			saveBinFile();
		}
	}
}