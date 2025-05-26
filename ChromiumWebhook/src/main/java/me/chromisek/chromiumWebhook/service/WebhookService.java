package me.chromisek.chromiumWebhook.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.chromisek.chromiumCore.ChromiumLogger;
import me.chromisek.chromiumWebhook.ChromiumWebhook;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

public class WebhookService {
    private final ChromiumWebhook plugin;
    
    public WebhookService(ChromiumWebhook plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Send a report to Discord
     * @param reporter The player who reported
     * @param reported The player who was reported
     * @param reason The reason for the report
     * @param proof The proof URL or "None" if no proof was provided
     */
    public void sendReport(Player reporter, Player reported, String reason, String proof) {
        // Get configuration safely
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        if (config == null) {
            ChromiumLogger.severe("Configuration is null. Check if ChromiumWebhook was initialized properly.");
            return;
        }
        
        String webhookUrl = config.getString("webhook-url", "");
        if (webhookUrl.isEmpty()) {
            ChromiumLogger.severe("Webhook URL is not set in config.yml");
            return;
        }
        
        boolean debugMode = config.getBoolean("debug-mode", false);
        
        // Create embed for Discord
        JsonObject embed = new JsonObject();
        embed.addProperty("title", "Player Report");
        embed.addProperty("color", 16711680); // Red color
        embed.addProperty("timestamp", Instant.now().toString());
        
        // Add fields to embed
        JsonArray fields = new JsonArray();
        
        // Reporter field
        JsonObject reporterField = new JsonObject();
        reporterField.addProperty("name", "Reporter");
        reporterField.addProperty("value", reporter.getName());
        reporterField.addProperty("inline", true);
        fields.add(reporterField);
        
        // Reported field
        JsonObject reportedField = new JsonObject();
        reportedField.addProperty("name", "Reported Player");
        reportedField.addProperty("value", reported.getName());
        reportedField.addProperty("inline", true);
        fields.add(reportedField);
        
        // Reason field
        JsonObject reasonField = new JsonObject();
        reasonField.addProperty("name", "Reason");
        reasonField.addProperty("value", reason);
        reasonField.addProperty("inline", false);
        fields.add(reasonField);
        
        // Proof field
        JsonObject proofField = new JsonObject();
        proofField.addProperty("name", "Proof");
        proofField.addProperty("value", proof.equals("None") ? "N/A" : proof);
        proofField.addProperty("inline", false);
        fields.add(proofField);
        
        // Server field
        JsonObject serverField = new JsonObject();
        serverField.addProperty("name", "Server");
        serverField.addProperty("value", config.getString("server-name", "Minecraft Server"));
        serverField.addProperty("inline", true);
        fields.add(serverField);
        
        embed.add("fields", fields);
        
        // Add footer with plugin info
        JsonObject footer = new JsonObject();
        footer.addProperty("text", "ChromiumReports v" + plugin.getDescription().getVersion());
        embed.add("footer", footer);
        
        // Add thumbnail with player's avatar
        if (config.getBoolean("show-avatars", true)) {
            JsonObject thumbnail = new JsonObject();
            thumbnail.addProperty("url", "https://crafatar.com/avatars/" + reported.getUniqueId().toString() + "?overlay");
            embed.add("thumbnail", thumbnail);
        }
        
        // Create main payload
        JsonObject payload = new JsonObject();
        
        // Set username from config
        String webhookUsername = config.getString("webhook-username", "ChromiumWebhook");
        payload.addProperty("username", webhookUsername);
        
        // Set avatar from config
        String webhookAvatar = config.getString("webhook-avatar-url", "");
        if (!webhookAvatar.isEmpty()) {
            payload.addProperty("avatar_url", webhookAvatar);
        }
        
        // Add content
        String contentFormat = config.getString("report-message", "New player report submitted");
        payload.addProperty("content", contentFormat);
        
        // Add embed
        JsonArray embeds = new JsonArray();
        embeds.add(embed);
        payload.add("embeds", embeds);
        
        // Send payload to Discord in async task
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(webhookUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    
                    String jsonPayload = payload.toString();
                    
                    if (debugMode) {
                        ChromiumLogger.debug("Sending webhook payload: " + jsonPayload);
                    }
                    
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    int responseCode = connection.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        ChromiumLogger.info("Successfully sent report to Discord webhook");
                        // Success status is handled in ChromiumReports class with green color
                    } else {
                        ChromiumLogger.severe("Failed to send report to Discord webhook. Response code: " + responseCode);
                        // We'll notify the player in the calling class with appropriate red color
                    }
                    
                    connection.disconnect();
                } catch (Exception e) {
                    ChromiumLogger.severe("Error sending webhook: " + e.getMessage());
                    if (debugMode) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }
    
    /**
     * Send a custom webhook with custom data
     * @param data JsonObject containing webhook data to send
     */
    public void sendCustomWebhook(JsonObject data) {
        // Get configuration safely
        org.bukkit.configuration.file.FileConfiguration config = plugin.getConfig();
        if (config == null) {
            ChromiumLogger.severe("Configuration is null. Check if ChromiumWebhook was initialized properly.");
            return;
        }
        
        String webhookUrl = config.getString("webhook-url", "");
        if (webhookUrl.isEmpty()) {
            ChromiumLogger.severe("Webhook URL is not set in config.yml");
            return;
        }
        
        boolean debugMode = config.getBoolean("debug-mode", false);
        
        // Send data to Discord in async task
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(webhookUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    
                    String jsonPayload = data.toString();
                    
                    if (debugMode) {
                        ChromiumLogger.debug("Sending custom webhook payload: " + jsonPayload);
                    }
                    
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }
                    
                    int responseCode = connection.getResponseCode();
                    if (responseCode >= 200 && responseCode < 300) {
                        ChromiumLogger.info("Successfully sent custom data to Discord webhook");
                    } else {
                        ChromiumLogger.severe("Failed to send custom data to Discord webhook. Response code: " + responseCode);
                    }
                    
                    connection.disconnect();
                } catch (Exception e) {
                    ChromiumLogger.severe("Error sending webhook: " + e.getMessage());
                    if (debugMode) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
