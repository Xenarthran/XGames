package xenar47.bukkit.xgames.api;

import java.util.ArrayList;
import java.util.HashMap;

import xenar47.bukkit.xgames.XGames;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.Wool;
import org.bukkit.scoreboard.Team;

public abstract class TeamGame extends Game {

	Team red;
	Team blue;
	Team green;
	Team yellow;
	
	HashMap<String, Color> teamColors;
	HashMap<String, Integer> teamPoints;
	
	private boolean autoAssign;
	private boolean autoArmor;
	
	public TeamGame(XGames xgames, boolean autoAssign, boolean autoArmor) {
		super(xgames);
		
		teamColors = new HashMap<String, Color>();
		
		red = sbm.getTeam("RED");
		teamColors.put(red.getName(), Color.RED);
		
		blue = sbm.getTeam("BLUE");
		teamColors.put(blue.getName(), Color.BLUE);
		
		green = sbm.getTeam("GREEN");
		teamColors.put(green.getName(), Color.GREEN);
		
		yellow = sbm.getTeam("YELLOW");
		teamColors.put(yellow.getName(), Color.YELLOW);
		
		this.setShowTeamPoints(true);
	}
	
	@Override
	public void preparePlayer(Player player) {
		if (autoAssign) {
			Team team = getSmallestTeam();
			setTeam(player, team);
			preparePlayer(player, team);
		} else {
			preparePlayer(player, null);
		}
	}
	
	/**
	 * If you are assigning teams, make sure to use .setTeam(Player player, Team team).
	 * @param player = player to prepare.
	 * @param team = assigned team. Will be null if autoAssign = false.
	 */
	public abstract void preparePlayer(Player player, Team team);
	
	public void setTeam(Player player, Team team) {
		if (!team.getPlayers().contains(player))
			team.addPlayer(player);
		
		if (autoArmor) {
			PlayerInventory pi = player.getInventory();
			pi.clear();
			
			Wool w = new Wool();
			w.setColor(DyeColor.getByColor(getTeamColor(team)));
			pi.setHelmet(w.toItemStack());
			
			ItemStack lChest = new ItemStack(Material.LEATHER_CHESTPLATE);
			LeatherArmorMeta lam = (LeatherArmorMeta)lChest.getItemMeta();
			lam.setColor(getTeamColor(team));
			lChest.setItemMeta(lam);
			pi.setChestplate(lChest);

			ItemStack lLegs = new ItemStack(Material.LEATHER_LEGGINGS);
			lam = (LeatherArmorMeta)lLegs.getItemMeta();
			lam.setColor(getTeamColor(team));
			lLegs.setItemMeta(lam);
			pi.setLeggings(lLegs);
			
			ItemStack lBoots = new ItemStack(Material.LEATHER_BOOTS);
			lam = (LeatherArmorMeta)lBoots.getItemMeta();
			lam.setColor(getTeamColor(team));
			lBoots.setItemMeta(lam);
			pi.setBoots(lBoots);			
		}
	}
	
	public ArrayList<Team> getTeams() {
		ArrayList<Team> teams = new ArrayList<Team>();

		teams.add(red);
		teams.add(blue);
		teams.add(green);
		teams.add(yellow);
		
		return teams;
	}
	
	public ArrayList<Team> getPopulatedTeams() {
		ArrayList<Team> teams = new ArrayList<Team>();
		if (red.getSize() > 0)
			teams.add(red);
		if (blue.getSize() > 0)
			teams.add(blue);
		if (green.getSize() > 0)
			teams.add(green);
		if (yellow.getSize() > 0)
			teams.add(yellow);
		return teams;
	}
	
	public Team getSmallestTeam() {
		int minimum = Math.min(Math.min(red.getSize(), blue.getSize()), Math.min(green.getSize(), yellow.getSize()));
		
		if (red.getSize() == minimum)
			return red;
		else if (blue.getSize() == minimum)
			return blue;
		else if (green.getSize() == minimum)
			return green;
		else
			return yellow;
	}
	
	public void setTeamColor(Team team, Color color) {
		teamColors.put(team.getName(), color);
	}
	public Color getTeamColor(Team team) {
		return teamColors.get(team.getName());
	}
	
	public void setTeamPoints(Team team, int points) {
		teamPoints.put(team.getName(), points);
	}
	public int getTeamPoints(Team team) {
		return teamPoints.get(team.getName());
	}
	
	public void setAutoAssign(boolean autoAssign) {
		this.autoAssign = autoAssign;
	}
	public void setAutoArmor(boolean autoArmor) {
		this.autoArmor = autoArmor;
	}
	
	public boolean getAutoAssign() {
		return autoAssign;
	}
	public boolean getAutoArmor() {
		return autoArmor;
	}
}
