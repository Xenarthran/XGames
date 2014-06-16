package xenar47.bukkit.xgames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import com.google.common.collect.Lists;

public class WorldConfigManager {

	private final XGames xgames;
	private WorldConfigListener wcl;

	public static final String WORLD_LIST_PATH = "world.game.list";

	private FileConfiguration defConfig;
	private List<String> list;

	private HashMap<String, FileConfiguration> configs = new HashMap<String, FileConfiguration>();
	private HashMap<String, File> configFiles = new HashMap<String, File>();
	private HashMap<String, World> worlds = new HashMap<String, World>();

	public enum LOCATIONS {
		RED, BLUE, GREEN, YELLOW, BOUNDS1, BOUNDS2
	};
	public enum WORLD_TYPE {
		BASIC_TEAM, TEAM_CAPTURE, SPLEEF,
	};

	public WorldConfigManager(XGames xgames) {

		this.xgames = xgames;
		this.wcl = new WorldConfigListener(xgames, this);
		Bukkit.getPluginManager().registerEvents(wcl, xgames);

		defConfig = xgames.getConfig();

		loadWorlds();
	}

	public void loadWorlds() {
		
		configs = new HashMap<String, FileConfiguration>();
		configFiles = new HashMap<String, File>();
		worlds = new HashMap<String, World>();

		list = defConfig.getStringList(WORLD_LIST_PATH);

		for (String key : list) {

			configs.put(key, getConfig(key));
			Bukkit.broadcastMessage("Loading" + key);
			
			World world = Bukkit.getServer().createWorld(new WorldCreator(key));
			world = setWorldOptions(world);			
			worlds.put(key, world);

		}
		Bukkit.broadcastMessage("Loads are complete.");

	}

	public void addToList(String string) {
		list.add(string);
		
		saveWorlds();
		loadWorlds();
	}

	public void removeFromList(String string) {
		list.remove(string);
	}
	
	public String getRandomWorld() {
		
		return "fourcorners";
		/*
		if (list.size() < 1)
			return null;
		
		Random r = new Random();
		int i = r.nextInt(worlds.keySet().size());
		return list.get(i);*/
	}
	
	public World setWorldOptions(World world) {
		
		world.setAutoSave(false);
		world.setDifficulty(Difficulty.HARD);
		world.setPVP(true);
		
		world.setAnimalSpawnLimit(0);
		world.setMonsterSpawnLimit(0);
		world.setWaterAnimalSpawnLimit(0);
		world.setAmbientSpawnLimit(0);
		world.setSpawnFlags(false, false);
		
		for (Entity entity : world.getEntities())
			if (entity instanceof Creature)
				if (!(entity instanceof Player))
					entity.remove();
		
		return world;
	}

	public void saveWorlds() {

		defConfig.set(WORLD_LIST_PATH, list);

		for (String key : list)
			saveConfig(key);
		
		xgames.saveConfig();
	}

	public World getWorld(String key) {
		if (worlds.containsKey(key))
			return worlds.get(key);
		else
			return null;
	}

	
	
	public String getWorldType(World world) {
		return getConfig(world.getName()).get(getTypePath()).toString();
	}
	
	public String getTypePath() {
		return "type";
	}
	
	/**
	 * Map resetting
	 */

	private void unloadMap(String mapname) {
		if (Bukkit.getServer().unloadWorld(Bukkit.getServer().getWorld(mapname), false)) {
			xgames.getLogger().info("Successfully unloaded " + mapname);
		} else {
			xgames.getLogger().severe("COULD NOT UNLOAD " + mapname);
		}
	}

	private void loadMap(String mapname) {
		Bukkit.getServer().createWorld(new WorldCreator(mapname));
	}

	public void rollback(String mapname) {
		unloadMap(mapname);
		loadMap(mapname);
	}

	/*
	 * Methods for configuration reloading, getting, and saving.
	 */

	private void reloadConfig(String key) {

		FileConfiguration config = configs.get(key);
		File file = configFiles.get(key);

		if (file == null) {
			file = new File(xgames.getDataFolder(), key + ".yml");
		}
		config = YamlConfiguration.loadConfiguration(file);

		// Look for defaults in the jar
		InputStream defConfigStream = xgames.getResource(key + ".yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			config.setDefaults(defConfig);
		}

		configs.put(key, config);
		configFiles.put(key, file);
	}

	public void saveConfig(String key) {

		FileConfiguration config = configs.get(key);
		File file = configFiles.get(key);

		if (config == null || file == null) {
			return;
		}
		try {
			getConfig(key).save(file);
		} catch (IOException e) {
			xgames.getLogger().log(Level.SEVERE,
					"Could not save config to " + file, e);
		}
	}

	public FileConfiguration getConfig(String key) {

		FileConfiguration config = configs.get(key);

		if (config == null) {
			reloadConfig(key);
		}
		return configs.get(key);
	}

	/*
	 * Methods for world config items
	 */

	public static ItemStack getSpawnTool(DyeColor color) {
		Wool wool = new Wool();
		wool.setColor(color);

		ItemStack is = wool.toItemStack();
		is.setAmount(1);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName("Tool: " + color.toString());
		
		List<String> lore = Lists.newArrayList(new String[]{"Place block at desired spawn point."});
		
		meta.setLore(lore);
		is.setItemMeta(meta);

		return is;
	}

	public static ItemStack getBoundsTool(int i) {
		Wool wool = new Wool();
		wool.setColor((i == 1)?DyeColor.WHITE:DyeColor.BLACK);

		ItemStack is = wool.toItemStack();
		is.setAmount(i);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName("Tool: bounds" + i);
		
		List<String> lore = Lists.newArrayList(new String[]{"Place block at desired spawn point."});
		
		meta.setLore(lore);
		is.setItemMeta(meta);

		return is;
	}
}
