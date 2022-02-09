package me.inamine.carddraw;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CardDraw extends JavaPlugin {

  public CDFileManager fileManager;
  public CDCommandManager commandManager;
  @Override
  public void onEnable() {
    this.fileManager = new CDFileManager(this);
    this.saveDefaultConfig();
    fileManager.checkFiles();
    commandManager = new CDCommandManager(this, fileManager);
    commandManager.createAll();
    int pluginID = 14241;
    new CDMetrics(this, pluginID);
    Bukkit.getLogger().info("Card Draw started successfully!");
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
