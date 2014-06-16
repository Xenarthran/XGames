package xenar47.bukkit.xgames;

import java.util.ArrayList;
import java.util.UUID;

import xenar47.bukkit.xgames.api.Game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Matthew Merrill
 * 
 */
public class GameManager {

	private XGames plugin;
	protected ArrayList<UUID> players = new ArrayList<UUID>();
	protected Game game;

	//private String world;
	
	public GameManager(XGames plugin, Game game) {

		this.plugin = plugin;
		this.game = game;

	//world = this.plugin.getWorldMgr().getRandomWorld();
		
	}

	public boolean canJoin(Player player) {
		return !players.contains(player.getName());
	}

	public void joinGame(Player player) {
		if (!canJoin(player))
			return;
		
		players.add(player.getUniqueId());
		
		ScoreboardManager.waitingList(player, this);
		
		player.sendMessage(ChatColor.GRAY
				+ "You have joined the waiting list.");
		
	}
	
	public ArrayList<UUID> getPlayers(){
		return players;
	}

	public void startGame() {
		
		if (game.isRunning())
			return;

		game.setPlayers(players);
		game.setWorld(plugin.getWorldMgr().getWorld(plugin.getWorldMgr().getRandomWorld()));

		prepareGame();
		//teleportPlayers();
		//pregame(game);
		
		game.startGame(this);

	}

	private void prepareGame() {
		game.prepareGame();
	}

	/*private void teleportPlayers() {

		for (String name : game.getPlayers()) {
			Player player = Bukkit.getPlayer(name);
			
			plugin.getMetaMgr().setInGame(player);
			
			player.teleport(game.getTeleportLocation(player));
		}
	}*/
	
	public void stopGame() {
		
		for (UUID id : game.getPlayers()) {
			Player player = Bukkit.getPlayer(id);
			plugin.toLobby(player);
		}
		plugin.killGame();
		//plugin.getWorldMgr().rollback(world);
	}

}
