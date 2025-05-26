package me.chromisek.chromiumWebhook;

import org.bukkit.plugin.java.JavaPlugin;

import me.chromisek.chromiumCore.ChromiumConfig;
import me.chromisek.chromiumCore.ChromiumLogger;
import me.chromisek.chromiumWebhook.service.WebhookService;

public final class ChromiumWebhook extends JavaPlugin {
    private static ChromiumWebhook instance;
    private ChromiumConfig config;
    private WebhookService webhookService;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        ChromiumLogger.init(this, "[" + this.getDescription().getName() + "]");
        
        // Load config
        this.config = new ChromiumConfig(this, "config.yml");
        this.config.saveDefaultConfig();
        
        // Initialize webhook service
        this.webhookService = new WebhookService(this);
        
        ChromiumLogger.info("Plugin enabled");
        ChromiumLogger.info("Webhook URL: " + (this.config.getConfig().getString("webhook-url").isEmpty() ? "Not set" : "Set"));
        ChromiumLogger.info("Debug mode: " + this.config.getConfig().getBoolean("debug-mode", false));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ChromiumLogger.info("Plugin disabled");
    }
    
    /**
     * Reload the plugin configuration
     * Used when reloading the plugin via the command
     */
    @Override
    public void reloadConfig() {
        // Reload using ChromiumConfig instead of super.reloadConfig()
        if (this.config != null) {
            this.config.reloadConfig();
        } else {
            super.reloadConfig();
        }
        
        ChromiumLogger.info("Webhook configuration reloaded");
        
        // Log the status after reload
        ChromiumLogger.info("Webhook URL: " + (this.config.getConfig().getString("webhook-url").isEmpty() ? "Not set" : "Set"));
    }
            
    public static ChromiumWebhook getInstance() {
        return instance;
    }
    
    public ChromiumConfig getChromiumConfig() {
        return config;
    }
    
    /**
     * Override getConfig to return our ChromiumConfig's configuration
     */
    @Override
    public org.bukkit.configuration.file.FileConfiguration getConfig() {
        return this.config != null ? this.config.getConfig() : super.getConfig();
    }
    
    public WebhookService getWebhookService() {
        return webhookService;
    }
}
