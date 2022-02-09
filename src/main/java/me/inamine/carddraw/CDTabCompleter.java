package me.inamine.carddraw;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CDTabCompleter implements TabCompleter {

    private final FileConfiguration config;
    private final CDFileManager fileManager;

    public CDTabCompleter(FileConfiguration config, CDFileManager fileManager) {
        this.config = config;
        this.fileManager = fileManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase(config.getString("commands.draw.command"))) {
            if (args.length == 1) {
                List<String> possibleCompletions = new ArrayList<>();
                YamlConfiguration decks = fileManager.getDecks();
                for (String deckName : decks.getKeys(false)) {
                    if (sender.hasPermission("carddraw.draw" + decks.getString(deckName + ".permission"))) {
                        possibleCompletions.add(deckName);
                    }
                }
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[0], possibleCompletions, completions);
                Collections.sort(completions);
                return completions;
            } else if (args.length == 2) {
                List<String> possibleCompletions = new ArrayList<>();
                YamlConfiguration decks = fileManager.getDecks();
                for (int i = 1; i < 10; i++) {
                    possibleCompletions.add(String.valueOf(i));
                }
                List<String> completions = new ArrayList<>();
                StringUtil.copyPartialMatches(args[1], possibleCompletions, completions);
                Collections.sort(completions);
                return completions;
            }
        } else if (command.getName().equalsIgnoreCase(config.getString("commands.shuffle.command"))) {
            List<String> possibleCompletions = new ArrayList<>();
            YamlConfiguration decks = fileManager.getDecks();
            for (String deckName : decks.getKeys(false)) {
                if ((sender.hasPermission("carddraw.draw" + decks.getString(deckName + ".permission")) && config.getBoolean("anyone-can-shuffle")) || sender.hasPermission("carddraw.shuffle" + decks.getString(deckName + ".permission"))) {
                    possibleCompletions.add(deckName);
                }
            }
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], possibleCompletions, completions);
            Collections.sort(completions);
            return completions;
        } else if (command.getName().equalsIgnoreCase(config.getString("commands.base.command"))) {
            List<String> possibleCompletions = new ArrayList<>();
            if (sender.hasPermission("carddraw.reload")) {
                possibleCompletions.add("reload");
            }
            if (sender.hasPermission("carddraw.help")) {
                possibleCompletions.add("help");
            }
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], possibleCompletions, completions);
            Collections.sort(completions);
            return completions;
        }
        return null;
    }
}
