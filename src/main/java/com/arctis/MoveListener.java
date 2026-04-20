package com.arctis;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MoveListener implements Listener {

    private final ArctisChunkLoader plugin;

    public MoveListener(ArctisChunkLoader plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
            return; // Only trigger when crossing chunk borders
        }

        Player player = event.getPlayer();
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();

        boolean fromIsLoaded = plugin.getDataManager().getChunk(fromChunk) != null;
        boolean toIsLoaded = plugin.getDataManager().getChunk(toChunk) != null;

        if (toIsLoaded && !fromIsLoaded) {
            player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.GREEN + "You entered a loaded chunk.");
        } else if (fromIsLoaded && !toIsLoaded) {
            player.sendMessage(ArctisChunkLoader.PREFIX + ChatColor.YELLOW + "You left a loaded chunk.");
        }
    }
}
