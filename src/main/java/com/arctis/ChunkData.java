package com.arctis;

import java.util.UUID;

public class ChunkData {
    private final UUID owner;
    private final String worldName;
    private final int x;
    private final int z;
    private String customName;

    public ChunkData(UUID owner, String worldName, int x, int z, String customName) {
        this.owner = owner;
        this.worldName = worldName;
        this.x = x;
        this.z = z;
        this.customName = customName;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    // Unique identifier for the chunk
    public String getKey() {
        return worldName + ";" + x + ";" + z;
    }
}
