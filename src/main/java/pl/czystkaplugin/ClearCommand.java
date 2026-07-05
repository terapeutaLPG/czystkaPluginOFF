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

public final class ClearCommand implements CommandExecutor, TabCompleter {

    private final CzystkaPlugin plugin;
    private final ClearStorage storage;

    public ClearCommand(CzystkaPlugin plugin, ClearStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("czystka.clear")) {
            sender.sendMessage("§cBrak uprawnien do uzycia tej komendy.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§eUzycie: /" + label + " <nick>");
            return true;
        }

        String targetName = args[0];
        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        String executorName = sender.getName();
        String executorUuid = sender instanceof Player ? ((Player) sender).getUniqueId().toString() : null;

        PendingClear pendingClear = new PendingClear(
                targetName,
                onlineTarget != null ? onlineTarget.getUniqueId().toString() : null,
                executorName,
                executorUuid,
                System.currentTimeMillis()
        );

        storage.addPendingClear(pendingClear);
        storage.save();

        if (onlineTarget != null) {
            PlayerLifecycleListener.executePendingClear(plugin, storage, onlineTarget, pendingClear);
            sender.sendMessage("§aWyczyszczono inventory gracza §f" + onlineTarget.getName() + "§a.");
            return true;
        }

        sender.sendMessage("§aGracz §f" + targetName + "§a zostal zapisany do jednorazowego czyszczenia przy nastepnym wejsciu.");
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
