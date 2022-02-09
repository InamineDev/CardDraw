package me.inamine.carddraw;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class CDCommandExecutor implements CommandExecutor {

  private final CDFileManager fileManager;
  private final FileConfiguration config;
  private final Plugin plugin;

  String baseCommand;
  String drawCommand;
  String shuffleCommand;
  String prefix;
  HashMap<String, HashMap<String, Boolean>> decks = new HashMap<>();
  YamlConfiguration deckYaml;

  public CDCommandExecutor(CDFileManager fileManager, Plugin plugin) {
    this.fileManager = fileManager;
    this.plugin = plugin;
    this.config = plugin.getConfig();
    baseCommand = plugin.getConfig().getString("commands.base.command", "carddraw");
    drawCommand = plugin.getConfig().getString("commands.draw.command", "draw");
    shuffleCommand = plugin.getConfig().getString("commands.shuffle.command", "shuffle");
    prefix = fileManager.getMsg().getString("prefix", "&e[&5Card Draw&e] ");
    deckYaml = fileManager.getDecks();
    populateDecks();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    String commandName = command.getName();
    if (commandName.equalsIgnoreCase(baseCommand)) {
      if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
        if (sender.hasPermission("carddraw.reload")) {
          plugin.reloadConfig();
          fileManager.checkFiles();
          baseCommand = plugin.getConfig().getString("commands.base.command", "carddraw");
          drawCommand = plugin.getConfig().getString("commands.draw.command", "draw");
          shuffleCommand = plugin.getConfig().getString("commands.shuffle.command", "shuffle");
          prefix = fileManager.getMsg().getString("prefix", "&e[&5Card Draw&e] ");
          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', fileManager.getMsg().getString("reload-message")
                  .replace("%prefix%", prefix)));
        } else {
          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', fileManager.getMsg().getString("no-permission", "&cYou do not have permission to do that!")));
        }
      } else {
        for (String messageLine : fileManager.getMsg().getStringList("help")) {
          messageLine = messageLine.replace("%base-command%", baseCommand);
          messageLine = messageLine.replace("%draw-command%", drawCommand);
          messageLine = messageLine.replace("%shuffle-command%", shuffleCommand);

          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageLine));
        }
      }
    } else if (commandName.equalsIgnoreCase(shuffleCommand)) {
      if (args.length == 1 && decks.containsKey(args[0].toLowerCase())) {
        if (config.getBoolean("anyone-can-shuffle") || sender.hasPermission("carddraw.shuffle." + args[0].toLowerCase())) {
          shuffleDeck(args[0], sender);
        } else {
          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', fileManager.getMsg().getString("no-permission", "&cYou do not have permission to do that!")));
        }
      } else {
        String message = fileManager.getMsg().getString("invalid-use", "&cInvalid usage! Use /%command% <deck>");
        message = message.replace("%command%", commandName);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        StringBuilder deckMessage = new StringBuilder();
        for (String deckName : decks.keySet()) {
          deckMessage.append("&e").append(deckName).append("&a, ");
        }
        deckMessage = new StringBuilder(deckMessage.substring(0, deckMessage.length() - 5));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', deckMessage.toString()));
      }
    } else if (commandName.equalsIgnoreCase(drawCommand)) {
      if (sender instanceof Player) {
        Player player = (Player) sender;
        if (args.length >= 1 && decks.containsKey(args[0].toLowerCase())) {
          int quantity = 1;
          if (args.length == 2) {
            if (isNumeric(args[1])) {
              quantity = Integer.parseInt(args[1]);
            }
          }
          if (player.hasPermission("carddraw.draw." + args[0].toLowerCase())) {
            drawCard(args[0].toLowerCase(), player, quantity);
          }
        } else {
          String message = fileManager.getMsg().getString("invalid-use", "&cInvalid usage! Use /%command% <deck>");
          message = message.replace("%command%", commandName);
          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
          StringBuilder deckMessage = new StringBuilder();
          for (String deckName : decks.keySet()) {
            deckMessage.append("&e").append(deckName).append("&a, ");
          }
          deckMessage = new StringBuilder(deckMessage.substring(0, deckMessage.length() - 4));
          sender.sendMessage(ChatColor.translateAlternateColorCodes('&', deckMessage.toString()));
        }
      } else {
        sender.sendMessage(fileManager.getMsg().getString("console-cannot", "Console cannot use this command!"));
      }
    }
    return true;
  }

  private void populateDecks() {
    decks.clear();
    for (String deckName : deckYaml.getKeys(false)) {
      HashMap<String, Boolean> deck = new HashMap<>();
      for (String card : deckYaml.getStringList(deckName + ".cards")) {
        deck.put(card, true);
      }
      decks.put(deckName.toLowerCase(), deck);
    }
  }

  private void drawCard(String deckName, Player player, int quantity) {
    List<String> possibleCards = new ArrayList<>();
    HashMap<String, Boolean> deck = decks.get(deckName);
    for (String key : deck.keySet()) {
      if (deck.get(key)) possibleCards.add(key);
    }
    int i = 0;
    while (i < quantity) {
      if (possibleCards.size() == 0) {
        // Deck is empty, tell player
        if (config.getBoolean("auto-shuffle")) {
          if (config.getBoolean("auto-shuffle-permissionless")
                  || player.hasPermission("carddraw.shuffle." + deckName)
                  || config.getBoolean("anyone-can-shuffle")) {
            shuffleDeck(deckName, player);
          }
        }
        String message = prefix + fileManager.getMsg().getString("empty-deck");
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        break;
      } else {
        // Deck has cards
        int cardNumber = ThreadLocalRandom.current().nextInt(0, possibleCards.size());
        String card = possibleCards.get(cardNumber);
        String message = prefix + fileManager.getMsg().getString("draw-card");
        message = message.replace("%deck%", deckName);
        message = message.replace("%card%", card);
        if (deckYaml.getBoolean(deckName + ".unique-cards")) {
          possibleCards.remove(cardNumber);
          deck.put(card, false);
          decks.put(deckName, deck);
        }
        if (deckYaml.getBoolean(deckName + ".secret")) {
          message = message.replace("%player%", "You");
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        } else {
          message = message.replace("%player%", player.getName());
          broadcastMessage(message);
        }
        i++;
      }
    }
  }

  private void shuffleDeck(String deckName, CommandSender sender) {
    HashMap<String, Boolean> deck = decks.get(deckName);
    deck.replaceAll((k, v) -> true);
    String message = fileManager.getMsg().getString("deck-shuffled", "&e%deck% has been shuffled by %player%");
    message = message.replace("%deck%", deckName);
    message = message.replace("%player%", sender.getName());
    decks.put(deckName, deck);
    if (config.getBoolean("broadcast-shuffles")) {
      broadcastMessage(message);
    } else {
      sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
  }

  private void broadcastMessage(String message) {
    message = ChatColor.translateAlternateColorCodes('&', message);
    for (Player player : Bukkit.getOnlinePlayers()) {
      player.sendMessage(message);
    }
  }

  public static boolean isNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    try {
      int i = Integer.parseInt(strNum);
    } catch (NumberFormatException nfe) {
      return false;
    }
    return true;
  }

}
