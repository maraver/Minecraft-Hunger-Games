<h2>The Hunger Games Plugin v 1.4 for bukkit servers</h2>

<h3>IMPORTANT</h3>

For the time being, once you have configured radius, centerX, and centerZ 
and created the arena it is important that the do NOT change

--------------------------------------------------------------------------------------------------------------
<h3>SETTING UP</h3>

<h4>Setting up the plugin</h4>
 1. Download Bukkit and configure the server like normal
 2. If you haven't already start the server at least once before installing the plugin
 3. Safely stop the server to save the world (using the stop command)
 4. Drag and drop the hungergames.jar file into the plugins folder of your server like normal
 5. Restart the server. You will get a message saying "A starting location hasn't been set!"
 6. Connect to the server and find the point you want to be the start and center of the arena using f3
 7. Open the config.yml in the new 'The Hunger Games' that has been created (notepad may not work, try notepad++)
 8. You MUST change the _centerX _and _centerZ _from 0 and set the _worldName _to the appropriate world. See [The Config File](https://github.com/maraver/Minecraft-Hunger-Games/wiki/The-Config-File) for more information.
 9. Restart the server, it should work. You may get and error just move the centerX and Z accoring to what it says

<h4>Starting your first Games</h4>
 1. Make yourself and anyone you want to manage the game an OP (op <name>)
 2. If you want to make changes to the arena use '/games-gamemaster'
 3. Use '/games-accept' to begin accepting tributes
 4. Use '/games-tribute <name> to select someone as a tribute
 5. After you have all the tributes use '/games-prepare'
 6. Count down or something the use '/games-start'

--------------------------------------------------------------------------------------------------------------
<h3>UPDATING</h3>

<h4>------- Special Notes --------</h4>
    
 * <b>See the new updating key for which update method to use</b>
	
<h5>To 1.3.X</h5>
 * You must have 1.3 before you can update to 1.3.X
 *  Ignore the warning that look like this:  Invalid type for tribute's HashMap in bin

<h4>------ General Updating -------</h4>
      
 * You must always follow this first step when updating *
 1. Untribute/spectate/GM everyone and stop the games
          * use /games-end to clear all tribute and spectators
          * you have to manually remove all GM (/games-ungamemaker <name>)

<h5>Minor (x.x.1)<i>(ex: 1.3 to 1.3.1)</i></h5>
 2. Replace your current version of 'minecraftgames.jar' with the new one. Change nothing else

<h5>Major (x.1.x)<i>(ex: 1.2.1 to 1.3)</i></h5>
 2. Make a backup of 'config.yml' so you can find the centerX, centerZ, and radius from before the update
 3. Delete all Hunger Games files
 4. Drag and drop the new 'minecraftgames.jar' file
 5. Start your server to create the new 'config.yml'
 6. Stop the server and reconfigure 'config.yml'
*** centerX, centerZ, and radius MUST be the same as before you updated ***
*** Editing with notepad may not work, try notepad++ ***
    * You must also leave depth as 15
    * Set remove to true
    * Set updating to true
 7. Restart the server to remove the old arena
 8. Find a new location for the arena and configure 'config.yml' to your liking
 9. Restart the server one more time

--------------------------------------------------------------------------------------------------------------
<h3>CHANGELOG</h3>

<h4>------- Updating Key -------</h4>
<p>
 -<i>version</i>-&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Update using the "Minor" method<br/>
 =<i>version</i>=&nbsp;&nbsp;&nbsp;&nbsp;Update using the "Major" method
</p><br/>
  
<h4>-v 1.4-</h4>
 * Updated to support Minecraft 1.3.2
 * Added new permissions node "starter" who can use 'games-accept', 'games-prepare', and 'games-start'

<h4>-v 1.3.6-</h4>
 * Set gmCreative in config.yml to false to stop gamemakers from getting creative mode
 * Set fullArena in config.yml to have the item blocks spread around the entire arena
 * Give someone HungerGames.outcast permissions to stop them from joining a game
 * Fixed bug with /games-spawn
 * Fixed bug with spectators falling to their death
 * Fixed bug with gamemakers changes not saving
 * Fixed bug with autorun not picking enough players
 * Small improvements

<h4>-v 1.3.5b-</h4>
 * Fixed custom winnings

<h4>-v 1.3.5-</h4>
 * Games-save command to save the arena
 * Custom winnings
 * Option to disable mining
 * better help with installing
 * bug fixes with HungerGames.gamemaker permissions
 * bug fixes to autorun

<h4>-v 1.3.4-</h4>
 * Bug fixes

<h4>-v 1.3.3-</h4>
 * Inventory saving
 * Revamped arena restore
 * Fixed day at start
 * Tributes and spectators can use the list commands
 * Fixed under pedestule bug
 * Fixed GM bugs
 * Give HungerGames.gamemaker permissions have
           access to all HG commands without being OP
 * Small bug fixes

<h4>-v 1.3.2-</h4>
 * Multiverse support
 * If autorun is on items respawn at a customizable interval
 * Small improvements to arena restore (again...)
 * Fixed autorun
 * Bug fixes

<h4>-v 1.3.1-</h4>
 * Custom random items
 * Improved arena restore (again)
 * Fixed multiple tributes in one room (maybe, again)
 * Mobs wont attack spectators
 * Option to automatically make day when games start
 * Kill tributes that cheat their way out of the arena
 * Added /games-state
 * Added command aliases ex: /games-notrib
 * Broadcast tribute at dawn
 * Improved tribute tp
 * Fixed items being left from previous games
 * Fixed not being able to become a GM
 * Bug fixes

<h4>=v 1.3 beta=</h4>
 * Arena automation
 * Fixed arena restore (I think :P)
 * Updated the item system
 * Ability to make arena over the spawn
 * Right click with compass to point at nearest tribute
 * New redstone-less recipe for compass
      [][Leaves][]
      [Iron Ignot][Water Bucket][Iron Ignot]
      [][Iron Ignot][]
 * Broadcast the remaining tributes at dusk
 * Changed from '/games <command>' to '/games-<command>' to improve help
 * Added '/games-notribute' for people who don't want to be randomly chosen
 * use '/games-dotribute' to allow yourself to be autochosen
 * Improved the config
 * Fixed tribute initial placement
 * Updated random items
 * Gamemakers are invisible to tributes & spectators
 * Add items to the config
       - Option to unop
       - Configuration for autorunning
       - Custom Max depth
       - Spawn in arena
       - Remove arena
 * Improved landmines
 * Added mob eggs to random items
 * Less error prone if the server crashes
 * bug fixes
 * some other stuff I can't remember

<h4>-v 1.2.1-</h4>
 * Remove tribute armour
 * Updated random items
 * New item display system
 * Remove dropped items from previous games
 * Fixed minor teleporting bug

<h4>=v 1.2=</h4>
 * Changed gamemasters to gamemakers
 * Fixed spectators not being able to give items more than once
 * Tributes can only hear other tributes and spectators and only if they are near by
 * Configure distance that tributes can be to hear

<h4>v 1.1</h4>
 * Initial public release