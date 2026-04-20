package com.arctis;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChunkLoaderCommand implements CommandExecutor {

    private final ArctisChunkLoader plugin;

    public ChunkLoaderCommand(ArctisChunkLoader plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "Only players can open the ChunkLoader GUI.");
            return true;
        }

        Player player = (Player) sender;
        boolean isAdmin = player.hasPermission("chunkloader.admin");

        if (isAdmin || player.hasPermission("chunkloader.use")) {
            GuiManager.openMainGui(player, plugin, 0);
        } else {
            player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.RED + "You do not have permission to use this command.");
        }

        return true;
    }
}
