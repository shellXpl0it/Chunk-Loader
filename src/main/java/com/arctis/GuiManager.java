package com.arctis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.util.ArrayList;
import java.util.List;

public class GuiManager {

    public static final String MAIN_GUI_TITLE = "Chunk Loader - Page ";
    public static final String SUB_GUI_TITLE = "Chunk Options";
    private static ArctisChunkLoader pluginInstance;

    public static void init(ArctisChunkLoader plugin) {
        pluginInstance = plugin;
    }

    public static void openMainGui(Player player, ArctisChunkLoader plugin, int page) {
        init(plugin);
        boolean isAdmin = player.hasPermission("chunkloader.admin");
        Inventory gui = Bukkit.createInventory(null, 54, MAIN_GUI_TITLE + (page + 1));

        if (isAdmin) {
            gui.setItem(4, createItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Load New Chunk", "Click to load the chunk you are standing in."));
            gui.setItem(8, createItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Remove Current Chunk", "Click to unload the chunk you are standing in."));
        } else {
            gui.setItem(4, createItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Load New Chunk", "Click to load the chunk you are standing in."));
            gui.setItem(8, createItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Remove Current Chunk", "Click to unload the chunk you are standing in."));
        }

        // Middle Area: Loaded Chunks
        List<ChunkData> chunksToShow;
        if (isAdmin) {
            chunksToShow = plugin.getDataManager().getAllChunks();
        } else {
            chunksToShow = plugin.getDataManager().getPlayerChunks(player.getUniqueId());
        }

        int itemsPerPage = 36;
        int startIndex = page * itemsPerPage;
        int slot = 9;

        for (int i = startIndex; i < chunksToShow.size() && i < startIndex + itemsPerPage; i++) {
            ChunkData data = chunksToShow.get(i);
            String name = data.getCustomName() != null ? data.getCustomName() : "Chunk (" + data.getX() + ", " + data.getZ() + ")";
            
            OfflinePlayer owner = Bukkit.getOfflinePlayer(data.getOwner());
            String ownerName = owner.getName() != null ? owner.getName() : "Unknown";
            
            ItemStack item = createItem(Material.GRASS_BLOCK, ChatColor.YELLOW + name, 
                    "World: " + data.getWorldName(),
                    "Owner: " + ownerName,
                    "Click for options");
            
            // Store the chunk key in the item's NBT so we can retrieve it in the sub gui
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "chunk_key"), PersistentDataType.STRING, data.getKey());
            item.setItemMeta(meta);

            gui.setItem(slot++, item);
        }

        // Bottom Row: Pagination & Info
        if (page > 0) {
            gui.setItem(45, createItem(Material.ARROW, ChatColor.AQUA + "Previous Page"));
        }
        
        int limit = plugin.getConfig().getInt("user-chunk-limit", 5);
        int currentCount = plugin.getDataManager().getPlayerChunkCount(player.getUniqueId());
        String limitDisplay = (limit == -1 || isAdmin) ? "Limitless" : currentCount + " / " + limit;
        gui.setItem(49, createItem(Material.PAPER, ChatColor.GOLD + "Chunk Limit Info", "Loaded Chunks: " + ChatColor.WHITE + limitDisplay));
        
        if (startIndex + itemsPerPage < chunksToShow.size()) {
            gui.setItem(53, createItem(Material.ARROW, ChatColor.AQUA + "Next Page"));
        }

        player.openInventory(gui);
    }

    public static void openSubGui(Player player, ArctisChunkLoader plugin, String chunkKey) {
        init(plugin);
        boolean isAdmin = player.hasPermission("chunkloader.admin");
        Inventory gui = Bukkit.createInventory(null, 27, SUB_GUI_TITLE);

        ItemStack removeBtn = createItem(Material.RED_TERRACOTTA, ChatColor.RED + "Remove Loaded Chunk");
        ItemMeta removeMeta = removeBtn.getItemMeta();
        removeMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "chunk_key"), PersistentDataType.STRING, chunkKey);
        removeBtn.setItemMeta(removeMeta);
        gui.setItem(11, removeBtn);

        ItemStack renameBtn = createItem(Material.NAME_TAG, ChatColor.YELLOW + "Rename Loaded Chunk");
        ItemMeta renameMeta = renameBtn.getItemMeta();
        renameMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "chunk_key"), PersistentDataType.STRING, chunkKey);
        renameBtn.setItemMeta(renameMeta);
        gui.setItem(13, renameBtn);

        if (isAdmin) {
            ItemStack tpBtn = createItem(Material.ENDER_PEARL, ChatColor.AQUA + "TP to Loaded Chunk");
            ItemMeta tpMeta = tpBtn.getItemMeta();
            tpMeta.getPersistentDataContainer().set(new NamespacedKey(plugin, "chunk_key"), PersistentDataType.STRING, chunkKey);
            tpBtn.setItemMeta(tpMeta);
            gui.setItem(15, tpBtn);
        }

        // Chunk Info
        int limit = plugin.getConfig().getInt("user-chunk-limit", 5);
        int currentCount = plugin.getDataManager().getPlayerChunkCount(player.getUniqueId());
        String limitDisplay = (limit == -1 || isAdmin) ? "Limitless" : currentCount + " / " + limit;
        gui.setItem(22, createItem(Material.PAPER, ChatColor.GOLD + "Chunk Limit Info", "Loaded Chunks: " + ChatColor.WHITE + limitDisplay));


        gui.setItem(26, createItem(Material.DARK_OAK_DOOR, ChatColor.GRAY + "Back"));

        player.openInventory(gui);
    }

    private static ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            for (String l : lore) {
                loreList.add(ChatColor.GRAY + l);
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
}