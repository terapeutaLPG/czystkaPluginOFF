package pl.czystkaplugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public final class PlayerLifecycleListener implements Listener {

    private static final SimpleDateFormat DATE_FORMAT = createDateFormat();

    private final ClearStorage storage;

    public PlayerLifecycleListener(ClearStorage storage) {
        this.storage = storage;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PendingClear pendingClear = storage.findPendingClear(player);
        if (pendingClear != null) {
            executePendingClear(null, storage, player, pendingClear);
        }

        List<ClearNotification> notifications = storage.getAndRemoveNotificationsForExecutor(player.getUniqueId().toString(), player.getName());
        if (!notifications.isEmpty()) {
            for (ClearNotification notification : notifications) {
                player.sendMessage("§aTwoj zapisany clear zostal wykonany dla §f" + notification.getTargetName()
                        + "§a. Data: §f" + formatDate(notification.getExecutedAt()) + "§a.");
            }
            storage.save();
        }
    }

    static void executePendingClear(CzystkaPlugin plugin, ClearStorage storage, Player target, PendingClear pendingClear) {
        target.getInventory().clear();
        target.getInventory().setArmorContents(new ItemStack[4]);
        target.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        target.updateInventory();

        storage.removePendingClear(pendingClear);

        long executedAt = System.currentTimeMillis();
        ClearNotification notification = new ClearNotification(
                pendingClear.getTargetName(),
                pendingClear.getExecutorName(),
                pendingClear.getExecutorUuid(),
                pendingClear.getRequestedAt(),
                executedAt
        );

        target.sendMessage("§cTwoje inventory zostalo wyczyszczone na stale jednorazowa komenda.");
        target.sendMessage("§7Wykonal: §f" + pendingClear.getExecutorName() + "§7. Data: §f" + formatDate(executedAt));

        boolean executorNotifiedNow = false;

        if (pendingClear.getExecutorUuid() != null) {
            Player executor = Bukkit.getPlayer(java.util.UUID.fromString(pendingClear.getExecutorUuid()));
            if (executor != null && executor.isOnline()) {
                executor.sendMessage("§aWyczyszczono inventory gracza §f" + pendingClear.getTargetName()
                        + "§a. Data: §f" + formatDate(executedAt) + "§a.");
                executorNotifiedNow = true;
            }
        }

        if (!executorNotifiedNow) {
            Player executorByName = Bukkit.getPlayerExact(pendingClear.getExecutorName());
            if (executorByName != null && executorByName.isOnline()) {
                executorByName.sendMessage("§aWyczyszczono inventory gracza §f" + pendingClear.getTargetName()
                        + "§a. Data: §f" + formatDate(executedAt) + "§a.");
                executorNotifiedNow = true;
            }
        }

        if (!executorNotifiedNow && pendingClear.getExecutorUuid() != null) {
            storage.addNotification(notification);
        }

        storage.save();

        if (plugin != null) {
            plugin.getLogger().info("Wyczyszczono inventory gracza " + pendingClear.getTargetName()
                    + " przez " + pendingClear.getExecutorName() + " dnia " + formatDate(executedAt));
        }
    }

    private static String formatDate(long timestamp) {
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(new Date(timestamp));
        }
    }

    private static SimpleDateFormat createDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.forLanguageTag("pl-PL"));
        format.setTimeZone(TimeZone.getDefault());
        return format;
    }
}
