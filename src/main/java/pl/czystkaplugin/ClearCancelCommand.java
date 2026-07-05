package pl.czystkaplugin;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class ClearCancelCommand implements CommandExecutor, TabCompleter {

    private final ClearStorage storage;

    public ClearCancelCommand(ClearStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("czystka.clear.cancel")) {
            sender.sendMessage("§cBrak uprawnien do uzycia tej komendy.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§eUzycie: /" + label + " <nick>");
            return true;
        }

        String targetName = args[0];
        PendingClear pendingClear = storage.removePendingClear(targetName);

        if (pendingClear == null) {
            sender.sendMessage("§eNie znaleziono zapisanego cleara dla §f" + targetName + "§e.");
            return true;
        }

        storage.save();
        sender.sendMessage("§aAnulowano zaplanowane czyszczenie dla §f" + pendingClear.getTargetName() + "§a.");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(prefix))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
