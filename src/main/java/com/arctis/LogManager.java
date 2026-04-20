package com.arctis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogManager {

    private final ArctisChunkLoader plugin;
    private File logsFile;
    private FileConfiguration logsConfig;

    public LogManager(ArctisChunkLoader plugin) {
        this.plugin = plugin;
        createLogsFile();
    }

    private void createLogsFile() {
        logsFile = new File(plugin.getDataFolder(), "logs.yml");
        if (!logsFile.exists()) {
            logsFile.getParentFile().mkdirs();
            plugin.saveResource("logs.yml", false);
        }
        logsConfig = YamlConfiguration.loadConfiguration(logsFile);
    }

    public void logAction(String message) {
        // Send to in-game admins if configured
        if (plugin.getConfig().getBoolean("admin-notifications")) {
            String adminMessage = ArctisChunkLoader.PREFIX + ChatColor.GRAY + message;
            Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.hasPermission("chunkloader.admin"))
                    .forEach(player -> player.sendMessage(adminMessage));
        }

        // Send to Discord if configured
        if (logsConfig.getBoolean("discord-logging-enabled")) {
            String webhookUrl = logsConfig.getString("discord-webhook-url");
            if (webhookUrl != null && !webhookUrl.isEmpty() && !webhookUrl.contains("YOUR_WEBHOOK_ID")) {
                sendDiscordWebhook(webhookUrl, message);
            }
        }
    }

    private void sendDiscordWebhook(String urlString, String content) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "ArctisChunkLoader");
                connection.setDoOutput(true);

                // Simple JSON payload
                String jsonPayload = "{\"content\": \"**[ArctisChunkLoader]** " + content.replace("\"", "\\\"") + "\"}";

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonPayload.getBytes("UTF-8"));
                }

                connection.getResponseCode(); // Execute the request
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
            }
        });
    }
}
