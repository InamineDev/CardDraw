package me.inamine.carddraw;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class CDCommandManager {

  private final Plugin plugin;
  private final CDCommandExecutor commandExecutor;
  private final CDTabCompleter tabCompleter;

  public CDCommandManager(Plugin plugin, CDFileManager fileManager) {
    this.plugin = plugin;
    commandExecutor = new CDCommandExecutor(fileManager, plugin);
    tabCompleter = new CDTabCompleter(plugin.getConfig(), fileManager);
  }

  private void registerCommand(String name, String description, String usage, String permission, String... aliases) {
    PluginCommand command = getCommand(name, plugin);
    command.setName(name);
    command.setDescription(description);
    command.setUsage(usage);
    command.setPermission(permission);
    command.setAliases(Arrays.asList(aliases));
    command.setExecutor(commandExecutor);
    command.setTabCompleter(tabCompleter);
    getCommandMap().register(plugin.getDescription().getName(), command);
  }

  private static PluginCommand getCommand(String name, Plugin plugin) {
    PluginCommand command = null;
    try {
      Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
      c.setAccessible(true);
      command = c.newInstance(name, plugin);
    } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
    }
    return command;
  }

  private static CommandMap getCommandMap() {
    CommandMap commandMap = null;
    try {
      if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
        Field f = SimplePluginManager.class.getDeclaredField("commandMap");
        f.setAccessible(true);

        commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
      }
    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
    return commandMap;
  }

  public void createAll() {
    String mainPermission = "carddraw.";
    // Base Command
    String baseCommand = plugin.getConfig().getString("commands.base.command", "carddraw");
    String[] baseAliases = plugin.getConfig().getStringList("commands.base.aliases").toArray(new String[0]);
    String baseDescription = "Card Draw base command";
    String baseUsage = "/" + baseCommand + " [reload|help]";
    String basePermission = mainPermission + "help";
    registerCommand(baseCommand, baseDescription, baseUsage, basePermission, baseAliases);
    // Draw
    String drawCommand = plugin.getConfig().getString("commands.draw.command", "draw");
    String[] drawAliases = plugin.getConfig().getStringList("commands.draw.aliases").toArray(new String[0]);
    String drawDescription = "Draw a card";
    String drawUsage = "/" + drawCommand + " d<#>|player [quantity]";
    String drawPermission = mainPermission + "roll";
    registerCommand(drawCommand, drawDescription, drawUsage, drawPermission, drawAliases);
    // Shuffle
    String shuffleCommand = plugin.getConfig().getString("commands.shuffle.command", "shuffle");
    String[] shuffleAliases = plugin.getConfig().getStringList("commands.shuffle.aliases").toArray(new String[0]);
    String shuffleDescription = "Shuffle a deck";
    String shuffleUsage = "/" + shuffleCommand + " (deck)";
    String shufflePermission = mainPermission + "shuffle";
    registerCommand(shuffleCommand, shuffleDescription, shuffleUsage, shufflePermission, shuffleAliases);
  }
}
