package pl.czystkaplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CzystkaPlugin extends JavaPlugin {

    private ClearStorage storage;

    @Override
    public void onEnable() {
        this.storage = new ClearStorage(this);
        this.storage.load();

        ClearCommand clearCommand = new ClearCommand(this, storage);
        if (getCommand("wykonajclear") != null) {
            getCommand("wykonajclear").setExecutor(clearCommand);
            getCommand("wykonajclear").setTabCompleter(clearCommand);
        }

        ClearCancelCommand clearCancelCommand = new ClearCancelCommand(storage);
        if (getCommand("wykonajclearanuluj") != null) {
            getCommand("wykonajclearanuluj").setExecutor(clearCancelCommand);
            getCommand("wykonajclearanuluj").setTabCompleter(clearCancelCommand);
        }

        Bukkit.getPluginManager().registerEvents(new PlayerLifecycleListener(storage), this);
        getLogger().info("CzystkaPlugin zostal wlaczony.");
    }

    @Override
    public void onDisable() {
        if (storage != null) {
            storage.save();
        }
    }
}
