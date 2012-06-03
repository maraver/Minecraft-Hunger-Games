package co.cc.free.dash.javagames.theone15247.TheHungerGames;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.Timer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Autorun implements ActionListener {
	public static final int RUN_ATTEMPTS = 20;
	
	final HungerGames plugin;
	final Random r;
	final Set<String> dontChoose = new HashSet<String>();
	
	int timeBetweenInSeconds = 10;
	int startTributes = 2;
	int randomPickTributes = 10;
	int waitTime = 10;
	int respawnItemsTime = 300;
	
	final Timer timer;
	int state = 0;
	int pickedTributes = 0;
	int startAttemps = 0;
	
	
	public Autorun(HungerGames g, int tBetween, int sTribs, int rndTribs, int waitT, int respawnT) {
		plugin = g;
		r = new Random();
		
		// if greater than zero use, otherwise default
		timeBetweenInSeconds = tBetween;
		respawnItemsTime = respawnT;
		
		startTributes = (sTribs >= HungerGames.MIN_TRIB) ? sTribs : startTributes;
		if (startTributes > 24)
			startTributes = 24;
		else if (startTributes < 2)
			startTributes = 2;
		
		waitTime = (waitT >= 0) ? waitT : waitTime;
		
		randomPickTributes = (rndTribs >= 0) ? rndTribs : randomPickTributes;
		if (randomPickTributes > 24)
			randomPickTributes = 24;
		
		timer = new Timer(0, this);
		state = 0;
	}
	
	public void startAutorun() {
		plugin.log.info("Autorunning with " + timeBetweenInSeconds + " sec in between games");
		plugin.log.info(" - " + startTributes + " minimum tributes");
		plugin.log.info(" - " + randomPickTributes + " tributes chosen randomly");
		plugin.log.info(" - " + waitTime + " sec for tributes to be chosen");
		if (respawnItemsTime > 0)
			plugin.log.info(" - Respawning items " + (respawnItemsTime/60) + " minutes after start");
		
		// games arn't accepting start accept timer
		if (!plugin.gameAccept && !plugin.gameReady()) {
			if (timeBetweenInSeconds >= 0) {
				state = 1;
				timer.setInitialDelay(timeBetweenInSeconds * 1000);
				timer.start();
			} else {
				plugin.log.info("Autorun will begin after /games-accept is called");
			}
		// if games are accepting start prepare timer
		} else if (plugin.gameAccept) {
			state = 2;
			startAttemps = 0;
			timer.setInitialDelay(waitTime * 1000);
			timer.start();
		// if game is preparing start gameStart timer
		} else if (plugin.gamePrepare) {
			state = 3;
			timer.setInitialDelay(randomTime());
			timer.start();
		}
	}
	
	private int randomTime() {
		// random time between 5 and 25 seconds
		return Math.round(r.nextFloat() * 1000 * 20 + 5000);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// if not auto-running don't do
		if (!plugin.doAutorun) {
			timer.stop();
			return;
		}
		
		Timer sourceTimer;
		
		if (e.getSource() instanceof Timer) {
			sourceTimer = (Timer)e.getSource();
		} else {
			plugin.log.warning("Autorun received an event from an invalid source!");
			return;
		}
		
		sourceTimer.stop();
		
		// if the timer to start accepting tributes
		if (state == 1) {
			// accept tributes to the game
			plugin.acceptTributes();	
			
		// if it is the tribute wait timer
		} else if (state == 2) {
			// and there are enough tributes
			if (plugin.tributes.size() >= startTributes) {
				startAttemps = 0;
				// prepare the game
				plugin.prepareGame();
			} else {
				startAttemps += 1;
				if (startAttemps < RUN_ATTEMPTS) {
					// else give them a message
					plugin.getServer().broadcastMessage(ChatColor.RED + "Not enough tributes to start!");
					// and loop the timer
					sourceTimer.setInitialDelay(this.waitTime * 1000);
					sourceTimer.restart();
				} else {
					plugin.getServer().broadcastMessage(ChatColor.GOLD + "Auto-accepting tributes has timed out!");
					plugin.getServer().broadcastMessage(ChatColor.GOLD + "Autorun will be disabled until The Games advance to the next stage!");
					sourceTimer.stop();
				}
			}
			
		// the start game timer
		} else if (state == 3) {
			plugin.startGame();
		
	    // chest regen timer
		} else if (state == 4) {
			plugin.log.info("State 4 ALERT");
			if (respawnItemsTime > 0) {
				plugin.cGen.clearChests(plugin.world);
				plugin.cGen.createChests(plugin.world, false); // create but no cornucopia chests
				plugin.getServer().broadcastMessage(ChatColor.BLUE + "Items have respawned!");
				state = 4;
				timer.restart();
			} else {
				timer.stop();
				state = 0;
			}
		}
	}
	
	public void autoChoosePlayerAsTribute(Player p, boolean justConnected) {
		
		List<Player> players = plugin.world.getPlayers();
		int size = players.size();
		
		// This guy just logged in, don't forget them!
		if (justConnected) size += 1;
		
		if (pickedTributes < randomPickTributes && pickedTributes < size &&
						plugin.tributes.size() < CustomGen.MAX_TRIB && !plugin.tributes.contains(p.getName()) &&
						!plugin.dontChoose.contains(p.getName())) {
			plugin.makeTribute(p);
			plugin.getServer().broadcastMessage(ChatColor.BLUE + p.getName() + 
					ChatColor.WHITE + " has been chosen as a tribute!");
			pickedTributes++;
		}
	}
	
	public void gamesStarted() {
		state = 4;
		timer.setInitialDelay(respawnItemsTime * 1000);
		timer.restart();
	}

	public void gamesPrepated() {
		// and start the timer for starting the game
		state = 3;
		timer.setInitialDelay(randomTime());
		timer.restart();
	}

	public void gamesAccepting() {
		state = 2;
		
		// choose random people
		if (randomPickTributes > 0) {
			pickedTributes = 0;

			List<Player> players = plugin.world.getPlayers();
			// loop until there are enough tributes or have everyone online or tributes is full
			while (pickedTributes < randomPickTributes && pickedTributes < players.size() &&
					plugin.tributes.size() < CustomGen.MAX_TRIB) {
				for (Player p:players) {
					// if have enough or have all no need to go through rest
					if (pickedTributes >= randomPickTributes || pickedTributes >= players.size() ||
							plugin.tributes.size() >= CustomGen.MAX_TRIB) break;
					
					// 50% chance to be drafted
					if (r.nextBoolean()) {
						// if not already a tribute
						if (!plugin.tributes.contains(p)) {
							// make them a tribute
							autoChoosePlayerAsTribute(p, false);
							pickedTributes++;
						}
					}
				}
			}
		}
		
		startAttemps = 0;
		// start the prepare timer
		timer.setInitialDelay(this.waitTime * 1000);
		timer.restart();
	}

	public void gamesReset() {
		if (timeBetweenInSeconds >= 0) {
			state = 1;
			if (randomPickTributes > 0)
				plugin.getServer().broadcastMessage(ChatColor.BLUE + 
						String.valueOf(randomPickTributes) + " tribute will be chosen in " + timeBetweenInSeconds + " seconds!");
			// reset the timer looping
			timer.setInitialDelay(timeBetweenInSeconds * 1000);
			timer.restart();
		} else {
			plugin.getServer().broadcastMessage(ChatColor.BLUE + "Autorun will begin after /games-accept is called");
		}
	}
}