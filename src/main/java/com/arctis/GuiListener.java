package com.arctis;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class GuiListener implements Listener {

    private final ArctisChunkLoader plugin;

    public GuiListener(ArctisChunkLoader plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(GuiManager.MAIN_GUI_TITLE) && !title.equals(GuiManager.SUB_GUI_TITLE)) {
            return;
        }

        event.setCancelled(true); // Prevent item moving

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || clicked.getType() == Material.AIR) return;
        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (title.startsWith(GuiManager.MAIN_GUI_TITLE)) {
            int currentPage = Integer.parseInt(title.replace(GuiManager.MAIN_GUI_TITLE, "")) - 1;
            handleMainGuiClick(player, itemName, clicked, currentPage);
        } else if (title.equals(GuiManager.SUB_GUI_TITLE)) {
            handleSubGuiClick(player, itemName, clicked);
        }
    }

    private void handleMainGuiClick(Player player, String itemName, ItemStack clicked, int currentPage) {
        if (itemName.equals("Load New Chunk")) {
            Chunk chunk = player.getLocation().getChunk();
            
            if (!player.hasPermission("chunkloader.admin")) {
                int limit = plugin.getConfig().getInt("user-chunk-limit", 5);
                if (limit != -1 && plugin.getDataManager().getPlayerChunkCount(player.getUniqueId()) >= limit) {
                    player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "You have reached your chunk loading limit of " + limit + ".");
                    player.closeInventory();
                    return;
                }
            }

            boolean success = plugin.getDataManager().addChunk(player.getUniqueId(), chunk);
            if (success) {
                player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.GREEN + "Chunk successfully loaded!");
                plugin.getLogManager().logAction("Player " + player.getName() + " loaded a new chunk at X:" + chunk.getX() + " Z:" + chunk.getZ() + " in " + chunk.getWorld().getName());
            } else {
                player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "This chunk is already loaded.");
            }
            player.closeInventory();

        } else if (itemName.equals("Remove Current Chunk")) {
            Chunk chunk = player.getLocation().getChunk();
            
            ChunkData data = plugin.getDataManager().getChunk(chunk);
            if (data == null) {
                player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "This chunk is not currently force-loaded.");
                player.closeInventory();
                return;
            }

            if (!player.hasPermission("chunkloader.admin") && !data.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "You can only remove your own loaded chunks.");
                player.closeInventory();
                return;
            }

            plugin.getDataManager().removeChunk(chunk);
            player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "Chunk successfully unloaded!");
            plugin.getLogManager().logAction("Player " + player.getName() + " removed a loaded chunk at X:" + chunk.getX() + " Z:" + chunk.getZ() + " in " + chunk.getWorld().getName());
            // Instead of closing, update/re-open Main GUI
            GuiManager.openMainGui(player, plugin, currentPage);

        } else if (itemName.equals("Next Page")) {
            GuiManager.openMainGui(player, plugin, currentPage + 1);
        } else if (itemName.equals("Previous Page")) {
            GuiManager.openMainGui(player, plugin, currentPage - 1);
        } else if (clicked.getType() == Material.GRASS_BLOCK) {
            String chunkKey = clicked.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "chunk_key"), PersistentDataType.STRING);
            if (chunkKey != null) {
                GuiManager.openSubGui(player, plugin, chunkKey);
            }
        }
    }

    private void handleSubGuiClick(Player player, String itemName, ItemStack clicked) {
        String chunkKey = null;
        if (clicked.hasItemMeta()) {
             chunkKey = clicked.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "chunk_key"), PersistentDataType.STRING);
        }

        if (itemName.equals("Back")) {
            GuiManager.openMainGui(player, plugin, 0);
            return;
        }

        if (chunkKey == null) return;
        ChunkData data = plugin.getDataManager().getChunk(chunkKey);
        
        if (data == null) {
            player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "This chunk is no longer loaded.");
            player.closeInventory();
            return;
        }

        if (itemName.equals("Remove Loaded Chunk")) {
            plugin.getDataManager().removeChunk(chunkKey);
            player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "Successfully removed loaded chunk.");
            plugin.getLogManager().logAction("Player " + player.getName() + " removed loaded chunk " + data.getKey());
            // Go back to main GUI instead of closing
            GuiManager.openMainGui(player, plugin, 0);
        } else if (itemName.equals("Rename Loaded Chunk")) {
            plugin.setRenamingChunk(player.getUniqueId(), chunkKey);
            player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.YELLOW + "Please type the new name for the chunk in chat.");
            player.closeInventory();
        } else if (itemName.equals("TP to Loaded Chunk")) {
            if (player.hasPermission("chunkloader.admin")) {
                org.bukkit.World world = org.bukkit.Bukkit.getWorld(data.getWorldName());
                if (world != null) {
                    Location loc = new Location(world, (data.getX() << 4) + 8, world.getHighestBlockYAt((data.getX() << 4) + 8, (data.getZ() << 4) + 8) + 1, (data.getZ() << 4) + 8);
                    player.teleport(loc);
                    player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.AQUA + "Teleported to the loaded chunk.");
                } else {
                    player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "The world for this chunk is not loaded.");
                }
            }
            player.closeInventory();
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.isRenaming(player.getUniqueId())) {
            event.setCancelled(true);
            String chunkKey = plugin.getRenamingChunk(player.getUniqueId());
            String newName = event.getMessage();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                ChunkData data = plugin.getDataManager().getChunk(chunkKey);
                if (data != null) {
                    plugin.getDataManager().renameChunk(chunkKey, newName);
                    player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.GREEN + "Chunk successfully renamed to: " + newName);
                    plugin.getLogManager().logAction("Player " + player.getName() + " renamed chunk " + data.getKey() + " to " + newName);
                } else {
                    player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "That chunk no longer exists.");
                }
                plugin.clearRenamingChunk(player.getUniqueId());
            });
        }
    }
}