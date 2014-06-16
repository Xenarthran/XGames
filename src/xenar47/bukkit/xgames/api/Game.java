package xenar47.bukkit.xgames.api;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import xenar47.bukkit.xgames.*;
import xenar47.bukkit.xgames.WorldConfigManager.LOCATIONS;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

/**
 * @author Matthew Merrill
 * 
 */
interface IGame {
	
	public String getName();
	
	public void prepareGame();
	public void preparePlayer(Player player);
	
	public void startGame(GameManager gameManager);
	public boolean isRunning();
	public void stopGame(Player... player);
	
	/**
	 * return true if player should be damaged, false if attack should be cancelled.
	 */
	public boolean playerDamagePlayer(Player attacker, Player victim);
	
	/**
	 * return true if the game should end
	 */
	public boolean playerKilledEvent(EntityDamageEvent event);
}

public abstract class Game implements IGame, Listener {
	
	protected final XGames xgames;
	private ArrayList<UUID> players = new ArrayList<UUID>();
	
	ScoreboardManager sbm;
	GameManager gm;
	
	boolean showHealth = true;
	boolean showTeamPoints = false;
	
	public Game(XGames xgames){
		this.xgames = xgames;
		sbm = new ScoreboardManager(xgames, this);
	}
	
	@Override
	public void prepareGame() {
		ArrayList<UUID> players = this.getPlayers();
		for (UUID uuid : players) {
			Player player = Bukkit.getPlayer(uuid);
			
			player.setGameMode(GameMode.SURVIVAL);
			preparePlayer(player);
		}
	}
	
	@Override
	public void startGame(GameManager gm) {
		
		if (showHealth)
			sbm.showHealth();
		if (showTeamPoints)
			sbm.showTeamPoints();
		if (showHealth || showTeamPoints)
			sbm.show();
		
		this.gm = gm;
		this.isRunning = true;
		
		for (UUID uuid : getPlayers()) {
			Player player = Bukkit.getPlayer(uuid);
			player.teleport(getSpawnLocation(player));
		}

		Bukkit.getPluginManager().registerEvents(this, xgames);
		
	}

	private boolean isRunning;
	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void stopGame(Player... winners) {
		this.isRunning = false;
		sbm.hide();
		
		for (UUID uuid : getPlayers())
			sbm.removeEntry(uuid);
		
		if (winners.length >= 1){//if (playerLives.size() <= 1) {
			if (winners.length == 1) {
				Bukkit.broadcastMessage(ChatColor.GREEN + "[" + getName() + ChatColor.GREEN + "]" + ChatColor.BLUE + Bukkit.getPlayer(getPlayers().get(0)).getDisplayName() +ChatColor.GRAY + " takes the victory!");
			} else if (winners.length > 1){
				Bukkit.broadcastMessage(ChatColor.GREEN + getName() + ChatColor.GRAY + "All participants have died... No Winner!");
			}
		} else {
			Bukkit.broadcastMessage(ChatColor.GRAY + "All participants have died... No Winner!");
		}
		gm.stopGame();
	}
	
	// <World>
	protected World world;
	public void setWorld(World world) {
		this.world = world;
	}
	
	public Location getSpawnLocation(Player player) {
		
		LOCATIONS l = LOCATIONS.RED;
		
		Random random = new Random();
		int i = random.nextInt(4);
		
		switch (i){
		case (0):{l = LOCATIONS.RED; break;}
		case (1):{l = LOCATIONS.BLUE; break;}
		case (2):{l = LOCATIONS.GREEN; break;}
		default:{l = LOCATIONS.YELLOW; break;}
		}
		
		return (TeamWorld.getLocation(xgames.getWorldMgr(), world, l));
	}
	// </World>
	
	
	// <Players>
	public void setPlayers(ArrayList<UUID> players) {
		this.players = players;
	}

	public ArrayList<UUID> getPlayers() {
		return players;
	}
	
	public boolean hasPlayer(Player player){
		return players.contains(player.getUniqueId());
	}
	
	public void removePlayer(Player player) {
		players.remove(player.getUniqueId());
		sbm.removeEntry(player.getUniqueId());
		xgames.toLobby(player);
	}
	// </Players>
	
	
	public void setShowHealth(boolean b) {
		showHealth = b;
	}
	public void setShowTeamPoints(boolean b) {
		showTeamPoints = b;
	}
	
	
	/****************************************************
	 * 
	 *				LISTENERS
	 *
	 ***************************************************/
	
	@EventHandler
	public void onPlayerMelee(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
		
			Player damager = (Player) event.getDamager();
			Player victim = (Player) event.getEntity();
			
			if (players.contains(damager.getUniqueId()) && players.contains(victim.getUniqueId())) {
				
				ItemStack is = damager.getItemInHand();
				if (is instanceof Weapon) {
					((Weapon)is).melee(this, damager, victim);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (players.contains(player.getUniqueId()) && event.getItem() instanceof Weapon) {
			Weapon weapon = (Weapon)event.getItem();
			
			Action action = event.getAction();
			
			if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
				weapon.primary(this, player);
			} else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
				weapon.secondary(this, player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (players.contains(player.getUniqueId())) {
			if (player.getItemInHand() instanceof Weapon) {
				Weapon weapon = (Weapon)player.getItemInHand();
				event.setCancelled(!weapon.interact(this, player, event.getRightClicked()));
			}
		}
	}
	
	@EventHandler
	public void onPlayerReload(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (players.contains(player.getUniqueId()) && player.getItemInHand() instanceof Weapon) {
			((Weapon)player.getItemInHand()).reload(this, player);
		}
	}
	
	@EventHandler
	public void playerDamagePlayer(EntityDamageByEntityEvent event) {
		
		if ((!(event.getDamager() instanceof Player)) || (!(event.getEntity() instanceof Player)))
			return;
		
		Player attacker = (Player)event.getDamager();
		Player victim = (Player)event.getEntity();
		
		if (!(hasPlayer(attacker) && hasPlayer(victim)))
			return;
		
		event.setCancelled(!playerDamagePlayer(attacker, victim));
	}
	
	@EventHandler
	public void onPlayerDeath(EntityDamageEvent event) {
		
		if (! (event.getEntity() instanceof Player))
			return;
		
		Player player = (Player)event.getEntity();
		
		if (!hasPlayer(player))
			return;
		
		if (player.getHealth() - event.getDamage() <= 0){

			player.teleport(getSpawnLocation(player));
			preparePlayer(player);
			
			if (this.playerKilledEvent(event))
				stopGame();

			event.setCancelled(true);
			event.setDamage(0);
			
			player.setHealth(player.getMaxHealth());
		}
		
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (!hasPlayer(event.getPlayer()))
			return;
		
		event.setRespawnLocation(getSpawnLocation(event.getPlayer()));
	}
	
	@EventHandler
	public void onArrowHit(ProjectileHitEvent event) {
		if (event.getEntity() instanceof Arrow) {
			Arrow arrow = (Arrow)event.getEntity();
			
			//Weapons system not working - just make all explosive to look fancy
			//if (arrow.getMetadata("Explosive").contains(true)) {
				ProjectileSource ps = arrow.getShooter();
				
				if (ps instanceof Player) {
					
					Player player = (Player)ps;
					if (!hasPlayer(player) || !isRunning())
						return;
					
					Location loc = arrow.getLocation();
					arrow.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(), .75f, false, false);
					arrow.remove();
				}
			//}
		}
	}

}
