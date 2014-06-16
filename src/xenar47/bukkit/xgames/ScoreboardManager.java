package xenar47.bukkit.xgames;

import java.util.HashMap;
import java.util.UUID;

import xenar47.bukkit.xgames.api.Game;
import xenar47.bukkit.xgames.api.TeamGame;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class ScoreboardManager {
	
	static org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
	
	public static void remove(OfflinePlayer player) {
		Player online = Bukkit.getPlayer(player.getUniqueId());
		if (online != null) {
			online.setScoreboard(manager.getNewScoreboard());
		}
	}
	
	public static void waitingList(Player player, GameManager gm) {
		Scoreboard scoreboard = manager.getNewScoreboard();
		
		Objective obj = scoreboard.registerNewObjective("waitingList", "dummy");
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(ChatColor.GRAY + "Waiting List:");
		
		Score score = obj.getScore(player.getName());
		score.setScore(gm.getPlayers().indexOf(player.getName())+1);
		
		player.setScoreboard(scoreboard);
	}
	
	XGames plugin;
	HashMap<String, Team> teams;
	Scoreboard board;
	Game game;
	
	public ScoreboardManager(XGames plugin, Game game) {
		teams = new HashMap<String, Team>();
		board = manager.getNewScoreboard();

		this.plugin = plugin;
		this.game = game;
	}
	
	private boolean visible = false;
	public void show(){
	    for(UUID id : game.getPlayers()){
	    	Player player = Bukkit.getPlayer(id);
	    	player.setHealth(player.getHealth()); //Update their health
	    	player.setScoreboard(board);
	    }
		visible = true;
		
		update();
	}
	public void hide(){
		for(UUID id : game.getPlayers()){
	    	removeEntry(id);
	    }
		visible = false;
	}
	
	public void update() {
		//ScoreboardUpdateEvent event = new ScoreboardUpdateEvent(game, board);
		//Bukkit.getServer().getPluginManager().callEvent(event);

		//Game game = event.getGame();
		//Scoreboard board = event.getScoreboard();
		if (visible) {
			
			if (game instanceof TeamGame) {
				TeamGame teamGame = (TeamGame)game;
				Objective teamPoints = board.getObjective("teamPoints");
				for (Team team : teams.values()) {
					teamPoints.getScore(team.getDisplayName()).setScore(teamGame.getTeamPoints(team));
				}
			}
			
			for (UUID id : game.getPlayers()) {
				Player player = Bukkit.getPlayer(id);
		    	//player.setHealth(player.getHealth());
				player.setScoreboard(board);
			}
			//Bukkit.broadcastMessage("Updated Scoreboards");
		}
	}
	
	public Scoreboard getBoard(){
		return board;
	}

	public Team getTeam(String name) {
		if (board.getTeam(name) != null)
			return teams.get(name);
		
		Team team = board.registerNewTeam(name);
		teams.put(name, team);
		return team;
	}
	
	public void showHealth() {
		Objective health = board.registerNewObjective("showhealth", "health");
		health.setDisplaySlot(DisplaySlot.BELOW_NAME);
		health.setDisplayName("/ 20");
		
	    for(UUID id : game.getPlayers()){
	    	Player player = Bukkit.getPlayer(id);
	    	player.setHealth(player.getHealth()); //Update their health
	    }
	    
	    update();
	}
	public void hideHealth() {
		Objective health = board.getObjective("health");
		if (health != null)
			health.unregister();
		
		update();
	}
	
	public void showTeamPoints() {
		
		if (!(game instanceof TeamGame))
			return;
		
		TeamGame teamGame = (TeamGame) this.game;
		
		Objective teamPoints = board.registerNewObjective("teamPoints", "dummy");
		teamPoints.setDisplayName("Team Points:");
		teamPoints.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		for (Team team : board.getTeams()) {
			teamPoints.getScore(team.getDisplayName()).setScore(teamGame.getTeamPoints(team));
		}
		
		update();
	}
	public void hideTeamPoints() {
		Objective teamPoints = board.getObjective("teamPoints");
		if (teamPoints != null)
			teamPoints.unregister();
		
		update();
	}
	
	public void removeEntry(UUID id) {
		board.resetScores(Bukkit.getPlayer(id).getName());
		remove(Bukkit.getOfflinePlayer(id));
		
		Team playerTeam = board.getPlayerTeam(Bukkit.getOfflinePlayer(id));
		if (playerTeam != null) {
			playerTeam.removePlayer(Bukkit.getOfflinePlayer(id));
		}
		
		update();
	}

}
