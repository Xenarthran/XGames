package xenar47.bukkit.xgames;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

/**
 * @author Matthew Merrill
 * 
 */
public abstract class XGames extends JavaPlugin {

	public static final int METADATA_GAME = 0;

	GameManager gm;
	MetadataManager mm;
	WorldConfigManager wcm;

	BasePlayerListener ll;
	
	public void onEnable() {
		mm = new MetadataManager(this);
		wcm = new WorldConfigManager(this);		
		wcm.loadWorlds();
		
		ll = new BasePlayerListener(this);
		Bukkit.getPluginManager().registerEvents(ll, this);
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			toLobby(player);
			player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString()
					+ "Welcome back to the XGames; the plugin is being enabled.");
		}

	}
	public void onDisable() {
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			toLobby(player);
			ScoreboardManager.remove(player);
			player.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC.toString()
					+ "Sorry for any inconvenience; the plugin is being disabled.");
		}
		
		wcm.saveWorlds();
	}
	
	public abstract GameManager getGameManager();

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {

		if (label.equalsIgnoreCase("join")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED
						+ "You must be a player to use this command.");
				return true;
			}

			Player player = (Player) sender;
			
			if (gm == null)
				gm = getGameManager();
			
			if(gm == null)
				sender.sendMessage("[XGames] Could not attach to GameManager!");
			else
				gm.joinGame(player);

			return true;
		} else if (label.equalsIgnoreCase("start")) {
			if (gm == null)
				gm = getGameManager();
			
			if(gm == null)
				sender.sendMessage("[XGames] Could not attach to GameManager!");
			else
				gm.startGame();

		} else if (label.equalsIgnoreCase("setup")) {
			
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED
						+ "You must be a player to use this command.");
				return true;
			}

			Player player = (Player) sender;
			toSetup(player);

			return true;
		}if (label.equalsIgnoreCase("play")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED
						+ "You must be a player to use this command.");
				return true;
			}

			Player player = (Player) sender;
			toLobby(player);

			return true;
		}else if (label.equalsIgnoreCase("setworld")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED
						+ "You must be a player to use this command.");
				return true;
			}

			Player player = (Player) sender;
			try {
				player.teleport(Bukkit.getWorld(args[0]).getSpawnLocation());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return true;
		}else if (label.equalsIgnoreCase("prepworld")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED
						+ "You must be a player to use this command.");
				return true;
			}
			
			World world = ((Player)sender).getWorld();
			wcm.setWorldOptions(world);
			
			return true;
		}else if (label.equalsIgnoreCase("addworld")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED
						+ "You must be a player to use this command.");
				return true;
			}
			
			World world = ((Player)sender).getWorld();
			wcm.addToList(world.getName());
			
			return true;
		}else if (label.equalsIgnoreCase("saveworld")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED
						+ "You must be a player to use this command.");
				return true;
			}
			
			World world = ((Player)sender).getWorld();
			world.save();
			
			return true;
		}
		
		return false;
	}
	
	public MetadataManager getMetaMgr() {
		return mm;
	}
	public WorldConfigManager getWorldMgr() {
		return wcm;
	}
	
	public void toLobby(Player player) {
		player.setHealth(20);
		player.setFoodLevel(20);
		
		player.getInventory().setArmorContents(null);
		player.getInventory().clear();
		
		player.setGameMode(GameMode.CREATIVE);
		player.setAllowFlight(false);
		player.setCanPickupItems(false);
		
		player.setFireTicks(-1);
		player.getInventory().clear();
		for (PotionEffect pe : player.getActivePotionEffects())
			player.removePotionEffect(pe.getType());
		
		player.teleport(lobbyLocation());
		try {
		mm.setInLobby(player);
		} catch (Exception e){}
		ScoreboardManager.remove(player);
	}
	public void toSetup(Player player) {
		player.setHealth(20);
		player.setFoodLevel(20);
		
		player.getInventory().setArmorContents(null);
		player.getInventory().clear();
		
		player.setGameMode(GameMode.CREATIVE);
		player.setAllowFlight(true);
		player.setCanPickupItems(true);
		
		player.setFireTicks(-1);
		
		PlayerInventory inv = player.getInventory();
		inv.setItem(0, WorldConfigManager.getSpawnTool(DyeColor.RED));
		inv.setItem(1, WorldConfigManager.getSpawnTool(DyeColor.BLUE));
		inv.setItem(2, WorldConfigManager.getSpawnTool(DyeColor.GREEN));
		inv.setItem(3, WorldConfigManager.getSpawnTool(DyeColor.YELLOW));
		inv.setItem(4, WorldConfigManager.getBoundsTool(1));
		inv.setItem(5, WorldConfigManager.getBoundsTool(2));

		try {
		mm.setInSetup(player);
		} catch (Exception e){}
		ScoreboardManager.remove(player);
	}
	
	public Location lobbyLocation() {
		return Bukkit.getWorlds().get(0).getSpawnLocation();
	}
	
	public void killGame(){
		HandlerList.unregisterAll(gm.game);
		gm = null;
	}
}
