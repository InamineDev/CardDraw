package me.inamine.carddraw;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class CDFileManager {

    Plugin plugin;

    public CDFileManager(Plugin plugin) {
        this.plugin = plugin;
    }

    private static YamlConfiguration msg;
    private static YamlConfiguration decks;


    public void checkFiles() {
        if(!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdir()) {
                Bukkit.getLogger().warning("Error loading plugin folder. Disabling plugin.");
                plugin.getPluginLoader().disablePlugin(plugin);
            }
        }

        File m = new File(plugin.getDataFolder(), "messages.yml");
        if(!m.exists()){
            plugin.saveResource("messages.yml", true);
        }
        msg = YamlConfiguration.loadConfiguration(m);
        File d = new File(plugin.getDataFolder(), "decks.yml");
        if(!d.exists()){
            plugin.saveResource("decks.yml", true);
        }
        decks = YamlConfiguration.loadConfiguration(d);
    }

    public YamlConfiguration getMsg() {
        return msg;
    }
    public YamlConfiguration getDecks() {
        return decks;
    }
}
