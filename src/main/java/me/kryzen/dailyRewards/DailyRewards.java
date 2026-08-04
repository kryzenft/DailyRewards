package me.kryzen.dailyRewards;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.kryzen.dailyRewards.commands.DailyRewardsCommand;
import me.kryzen.dailyRewards.listeners.DailyListener;
import me.kryzen.dailyRewards.managers.DatabaseManager;
import me.kryzen.dailyRewards.managers.RewardManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class DailyRewards extends JavaPlugin {

    // DatabaseManager
    private final DatabaseManager databaseManager = new DatabaseManager();

    // RewardManager
    private final RewardManager rewardManager = new RewardManager(this, databaseManager);

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("Daily Rewards plugin has been enabled :)");

        // Database connect
        try {
            databaseManager.connect(getDataFolder());
            databaseManager.createTable();
        } catch (SQLException e) {
            getLogger().severe("Couldn't connect to database! Disabling plugin");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Commands
        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands command = event.registrar();

            command.register(DailyRewardsCommand.create(this, databaseManager), "The main command for daily rewards");

        });

        // Events
        getServer().getPluginManager().registerEvents(new DailyListener(this, databaseManager, rewardManager), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Daily Rewards plugin has been disabled");
    }
}
