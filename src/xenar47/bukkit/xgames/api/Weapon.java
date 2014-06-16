package xenar47.bukkit.xgames.api;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class Weapon extends ItemStack {

	public abstract void primary(Game game, Player player);
	public abstract void secondary(Game game, Player player);
	/**
	 * Called when a player uses this weapon in a melee attack on someone else.
	 * @param game
	 * @param player
	 * @param Victim
	 * @return How much damage should be done to the victim
	 */
	public abstract int melee(Game game, Player player, Player Victim);
	
	public abstract boolean interact(Game game, Player player, Entity target);
	public abstract void reload(Game game, Player player);
	
	protected Weapon(Material material) {
		super(material);
		
		;
	}
	
	public static HashMap< , Weapon> ();
}
