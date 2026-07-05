package pl.czystkaplugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public final class ClearStorage {

    private final CzystkaPlugin plugin;
    private final File file;
    private final Map<String, PendingClear> pendingClears = new LinkedHashMap<>();
    private final Map<String, List<ClearNotification>> notificationsByExecutor = new LinkedHashMap<>();

    public ClearStorage(CzystkaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        if (!file.exists()) {
            plugin.getDataFolder().mkdirs();
            save();
            return;
        }

        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection pendingSection = configuration.getConfigurationSection("pending");
        if (pendingSection != null) {
            for (String key : pendingSection.getKeys(false)) {
                ConfigurationSection entry = pendingSection.getConfigurationSection(key);
                if (entry == null) {
                    continue;
                }
                PendingClear pendingClear = new PendingClear(
                        entry.getString("targetName", key),
                        entry.getString("targetUuid"),
                        entry.getString("executorName", "Console"),
                        entry.getString("executorUuid"),
                        entry.getLong("requestedAt", System.currentTimeMillis())
                );
                pendingClears.put(normalizeName(pendingClear.getTargetName()), pendingClear);
            }
        }

        ConfigurationSection notificationSection = configuration.getConfigurationSection("notifications");
        if (notificationSection != null) {
            for (String executorKey : notificationSection.getKeys(false)) {
                ConfigurationSection executorSection = notificationSection.getConfigurationSection(executorKey);
                if (executorSection == null) {
                    continue;
                }
                List<ClearNotification> notifications = new ArrayList<>();
                for (String notificationKey : executorSection.getKeys(false)) {
                    ConfigurationSection entry = executorSection.getConfigurationSection(notificationKey);
                    if (entry == null) {
                        continue;
                    }
                    notifications.add(new ClearNotification(
                            entry.getString("targetName", "unknown"),
                            entry.getString("executorName", "unknown"),
                            entry.getString("executorUuid"),
                            entry.getLong("requestedAt", 0L),
                            entry.getLong("executedAt", 0L)
                    ));
                }
                notificationsByExecutor.put(executorKey, notifications);
            }
        }
    }

    public void save() {
        FileConfiguration configuration = new YamlConfiguration();

        for (PendingClear pendingClear : pendingClears.values()) {
            String key = normalizeName(pendingClear.getTargetName());
            configuration.set("pending." + key + ".targetName", pendingClear.getTargetName());
            configuration.set("pending." + key + ".targetUuid", pendingClear.getTargetUuid());
            configuration.set("pending." + key + ".executorName", pendingClear.getExecutorName());
            configuration.set("pending." + key + ".executorUuid", pendingClear.getExecutorUuid());
            configuration.set("pending." + key + ".requestedAt", pendingClear.getRequestedAt());
        }

        for (Map.Entry<String, List<ClearNotification>> entry : notificationsByExecutor.entrySet()) {
            int index = 0;
            for (ClearNotification notification : entry.getValue()) {
                String path = "notifications." + entry.getKey() + "." + index;
                configuration.set(path + ".targetName", notification.getTargetName());
                configuration.set(path + ".executorName", notification.getExecutorName());
                configuration.set(path + ".executorUuid", notification.getExecutorUuid());
                configuration.set(path + ".requestedAt", notification.getRequestedAt());
                configuration.set(path + ".executedAt", notification.getExecutedAt());
                index++;
            }
        }

        try {
            plugin.getDataFolder().mkdirs();
            configuration.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe("Nie udalo sie zapisac data.yml: " + exception.getMessage());
        }
    }

    public void addPendingClear(PendingClear pendingClear) {
        pendingClears.put(normalizeName(pendingClear.getTargetName()), pendingClear);
    }

    public PendingClear findPendingClear(Player player) {
        for (PendingClear pendingClear : pendingClears.values()) {
            if (pendingClear.getTargetUuid() != null && pendingClear.getTargetUuid().equalsIgnoreCase(player.getUniqueId().toString())) {
                return pendingClear;
            }
        }
        return pendingClears.get(normalizeName(player.getName()));
    }

    public void removePendingClear(PendingClear pendingClear) {
        pendingClears.remove(normalizeName(pendingClear.getTargetName()));
    }

    public PendingClear removePendingClear(String targetName) {
        if (targetName == null) {
            return null;
        }
        return pendingClears.remove(normalizeName(targetName));
    }

    public void addNotification(ClearNotification notification) {
        String executorKey = normalizeExecutorKey(notification.getExecutorUuid(), notification.getExecutorName());
        notificationsByExecutor.computeIfAbsent(executorKey, key -> new ArrayList<>()).add(notification);
    }

    public List<ClearNotification> getAndRemoveNotificationsForExecutor(String executorUuid, String executorName) {
        String executorKey = normalizeExecutorKey(executorUuid, executorName);
        List<ClearNotification> notifications = notificationsByExecutor.remove(executorKey);
        if (notifications == null) {
            return java.util.Collections.emptyList();
        }
        return notifications;
    }

    private static String normalizeName(String name) {
        return name == null ? "unknown" : name.toLowerCase(Locale.ROOT);
    }

    private static String normalizeExecutorKey(String executorUuid, String executorName) {
        if (executorUuid != null && !executorUuid.trim().isEmpty()) {
            return executorUuid.toLowerCase(Locale.ROOT);
        }
        return normalizeName(executorName);
    }
}
