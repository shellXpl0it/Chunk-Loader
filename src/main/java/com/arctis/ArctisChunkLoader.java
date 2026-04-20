package com.arctis;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;
import java.util.HashMap;
import java.util.UUID;

public class ArctisChunkLoader extends JavaPlugin {

    public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "ArctisChunkLoader" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    private DataManager dataManager;
    private LogManager logManager;
    
    // Tracks players currently trying to rename a chunk (Player UUID -> Chunk Key)
    private final HashMap<UUID, String> renamingPlayers = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        logManager = new LogManager(this);

        // Initialize backend storage and apply force loads
        dataManager = new DataManager(this);
        dataManager.applyForceLoads(); // Always applied now, no longer toggleable

        // Register the command
        getCommand("chunkloader").setExecutor(new ChunkLoaderCommand(this));

        // Register the GUI events and Chunk border listener
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new MoveListener(this), this);

        getLogger().info("Arctis Chunk Loader has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        getLogger().info("Arctis Chunk Loader has been disabled!");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setRenamingChunk(UUID playerUUID, String chunkKey) {
        renamingPlayers.put(playerUUID, chunkKey);
    }

    public String getRenamingChunk(UUID playerUUID) {
        return renamingPlayers.get(playerUUID);
    }

    public void clearRenamingChunk(UUID playerUUID) {
        renamingPlayers.remove(playerUUID);
    }

    public boolean isRenaming(UUID playerUUID) {
        return renamingPlayers.containsKey(playerUUID);
    }
}
