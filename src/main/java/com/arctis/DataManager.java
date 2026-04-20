package com.arctis;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataManager {

    private final ArctisChunkLoader plugin;
    private final File dataFile;
    private FileConfiguration dataConfig;

    // In-memory cache: Chunk Key (world;x;z) -> ChunkData
    private final Map<String, ChunkData> loadedChunks = new HashMap<>();

    public DataManager(ArctisChunkLoader plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "chunks.yml");
        loadData();
    }

    public void loadData() {
        loadedChunks.clear();
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection section = dataConfig.getConfigurationSection("chunks");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    UUID owner = UUID.fromString(section.getString(key + ".owner"));
                    String worldName = section.getString(key + ".world");
                    int x = section.getInt(key + ".x");
                    int z = section.getInt(key + ".z");
                    String customName = section.getString(key + ".customName");

                    ChunkData data = new ChunkData(owner, worldName, x, z, customName);
                    loadedChunks.put(data.getKey(), data);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load chunk data for key: " + key);
                }
            }
        }
    }

    public void saveData() {
        dataConfig.set("chunks", null); // Clear old data

        for (ChunkData data : loadedChunks.values()) {
            String path = "chunks." + data.getKey().replace(".", "_"); // Avoid YAML dot notation issues
            dataConfig.set(path + ".owner", data.getOwner().toString());
            dataConfig.set(path + ".world", data.getWorldName());
            dataConfig.set(path + ".x", data.getX());
            dataConfig.set(path + ".z", data.getZ());
            dataConfig.set(path + ".customName", data.getCustomName());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean addChunk(UUID owner, Chunk chunk) {
        String key = chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
        if (loadedChunks.containsKey(key)) return false; // Already loaded

        ChunkData data = new ChunkData(owner, chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), null);
        loadedChunks.put(key, data);
        chunk.setForceLoaded(true); // Bukkit API to keep chunk loaded
        saveData();
        return true;
    }

    public boolean removeChunk(String key) {
        ChunkData data = loadedChunks.remove(key);
        if (data != null) {
            World world = Bukkit.getWorld(data.getWorldName());
            if (world != null) {
                world.setChunkForceLoaded(data.getX(), data.getZ(), false);
            }
            saveData();
            return true;
        }
        return false;
    }

    public boolean removeChunk(Chunk chunk) {
        String key = chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
        return removeChunk(key);
    }

    public void renameChunk(String key, String newName) {
        ChunkData data = loadedChunks.get(key);
        if (data != null) {
            data.setCustomName(newName);
            saveData();
        }
    }

    public ChunkData getChunk(String key) {
        return loadedChunks.get(key);
    }

    public ChunkData getChunk(Chunk chunk) {
        String key = chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
        return loadedChunks.get(key);
    }

    public List<ChunkData> getAllChunks() {
        return new ArrayList<>(loadedChunks.values());
    }

    public List<ChunkData> getPlayerChunks(UUID owner) {
        List<ChunkData> playerChunks = new ArrayList<>();
        for (ChunkData data : loadedChunks.values()) {
            if (data.getOwner().equals(owner)) {
                playerChunks.add(data);
            }
        }
        return playerChunks;
    }

    public void applyForceLoads() {
        for (ChunkData data : loadedChunks.values()) {
            World world = Bukkit.getWorld(data.getWorldName());
            if (world != null) {
                world.setChunkForceLoaded(data.getX(), data.getZ(), true);
            }
        }
    }

    public void removeAllForceLoads() {
        for (ChunkData data : loadedChunks.values()) {
            World world = Bukkit.getWorld(data.getWorldName());
            if (world != null) {
                world.setChunkForceLoaded(data.getX(), data.getZ(), false);
            }
        }
    }

    public int getPlayerChunkCount(UUID owner) {
        return getPlayerChunks(owner).size();
    }
}
