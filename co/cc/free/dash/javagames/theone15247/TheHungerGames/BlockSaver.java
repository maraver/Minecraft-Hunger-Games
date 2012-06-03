package co.cc.free.dash.javagames.theone15247.TheHungerGames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;
import java.util.Stack;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class BlockSaver implements Listener {
	HungerGames plugin;
	
	// SAVE
	private Stack<int[][]> resortBlocks; // <[[x,y,z], [material,data]]>
	
	public BlockSaver(HungerGames p) {
		plugin = p;
		
		synchronized (plugin) {
			resortBlocks = new Stack<int[][]>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void loadBlockFile() {
		try {			
			if (plugin.getDataFolder().exists()) {
				File f = new File(plugin.getDataFolder().getCanonicalPath() + 
						File.separator + "blocks_" + plugin.worldName + ".bin");
	
				if(f.exists()) {
					ObjectInputStream is = new ObjectInputStream(new FileInputStream(f));
					
					Object blocks = is.readObject();
					if (blocks instanceof Stack<?>) {
						resortBlocks = (Stack<int[][]>) blocks;
					} else {
						plugin.log.warning("Invalid type for resortBlocks Stack in blocks bin (ignore if updating)");
					}
					
					is.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveBlockFile() {
		try {
			if(!plugin.getDataFolder().getCanonicalFile().exists()) 
				plugin.getDataFolder().getCanonicalFile().mkdir();
			
			File f = new File(plugin.getDataFolder().getCanonicalPath() + 
					File.separator + "blocks_" + plugin.worldName + ".bin");
			if (f.exists()) f.delete();
			f.createNewFile();
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
			
			os.writeObject(resortBlocks);
			
			os.flush();
			os.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDestroyed(BlockBreakEvent e) {
		if (plugin.gamemakers.contains(e.getPlayer().getName())) {	
				return;
		}
		
		doAddBlock(e.getBlock());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityExplode(EntityExplodeEvent e) {
		// if not protecting need to restore
		if (!plugin.protectGamesTNT) {
			for(Block b:e.blockList()) {
				doAddBlock(b);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent e) {
		if (plugin.gamemakers.contains(e.getPlayer().getName())) {	
			return;
		}
		
		doAddBlock(e.getBlock(), Material.AIR, (byte)0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockDissapear(BlockFadeEvent e) {
		doAddBlock(e.getBlock());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockSpread(BlockSpreadEvent e) {
		doAddBlock(e.getBlock(), Material.AIR, (byte) 0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockForm(BlockFormEvent e) {
		doAddBlock(e.getBlock(), Material.AIR, (byte) 0);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeavesDespawn(LeavesDecayEvent e) {
		doAddBlock(e.getBlock());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockIgnite(BlockIgniteEvent e) {
		// if the games havent started and in arena don't ignite
		if (!plugin.gameHasStarted && plugin.inArena(e.getBlock().getLocation())) {
			e.setCancelled(true);
			e.getBlock().setType(Material.AIR);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBurn(BlockBurnEvent e) {
		if (!plugin.gameHasStarted && plugin.inArena(e.getBlock().getLocation())) {
			e.setCancelled(true);
		} else {
			doAddBlock(e.getBlock());
		}
	}
	
	private void doAddBlock(Block b) {
		doAddBlock(b.getLocation(), b.getTypeId(), b.getData());
	}
	private void doAddBlock(Block b, Material m, byte d) {
		doAddBlock(b.getLocation(), m.getId(), d);
	}
	private void doAddBlock(Location l, int m, byte d) {		
		// if nothing was here before it will resort to air
		if (plugin.inArena(l)) {
			synchronized(this) {
				resortBlocks.push(new int[][] {{l.getBlockX(), l.getBlockY(), l.getBlockZ()}, {m, d}}); // {{location}, {data}}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void restore(World w) {
		plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Resorting the arena. You may expreience some lag");
		
		synchronized(this) {
			try {
				Stack<int[][]> restortCopy = (Stack<int[][]>) resortBlocks.clone();
				resortBlocks.clear();
				while (!restortCopy.empty()) {
					int[][] data = (int[][]) restortCopy.pop();
					
					
					int[] locArr = data[0];
					Block b = w.getBlockAt(locArr[0], locArr[1], locArr[2]);
					
					int[] vals = data[1];
					b.setTypeId(vals[0]);
					b.setData((byte) vals[1]);
				}
			} catch (ConcurrentModificationException e) {
				e.printStackTrace();
			}
		}
		
		for (Item i:w.getEntitiesByClass(Item.class)) {
			if (plugin.inArena(i.getLocation())) {
				i.remove();
			}
		}
		
		plugin.getServer().broadcastMessage(ChatColor.LIGHT_PURPLE + "Done resorting.");
	}
}
