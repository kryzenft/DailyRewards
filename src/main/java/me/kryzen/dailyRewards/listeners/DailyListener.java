package me.kryzen.dailyRewards.listeners;

import me.kryzen.dailyRewards.DailyGUI;
import me.kryzen.dailyRewards.DailyRewards;
import me.kryzen.dailyRewards.data.PlayerData;
import me.kryzen.dailyRewards.managers.DatabaseManager;
import me.kryzen.dailyRewards.managers.RewardManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.sql.SQLException;

public class DailyListener implements Listener {

    private final DailyRewards dailyRewards;
    private final DatabaseManager databaseManager;
    private final RewardManager rewardManager;

    public DailyListener(DailyRewards dailyRewards, DatabaseManager databaseManager, RewardManager rewardManager) {
        this.dailyRewards = dailyRewards;
        this.databaseManager = databaseManager;
        this.rewardManager = rewardManager;
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {

        if (!(event.getInventory().getHolder() instanceof DailyGUI)) {
            return;
        }

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) {
            return;
        }

        if (!clickedInventory.equals(event.getInventory())) {
            return;
        }

        if (clicked == null) {
            return;
        }

        String key = clicked.getPersistentDataContainer().get(DailyGUI.rewardKey, PersistentDataType.STRING);

        if ("claim".equals(key)) {

            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
            Bukkit.getAsyncScheduler().runNow(dailyRewards, task -> {

                try {
                    PlayerData data = databaseManager.getPlayerData(player.getUniqueId());

                    Bukkit.getGlobalRegionScheduler().run(dailyRewards, task1 -> {
                        rewardManager.processClaim(player, data);
                    });
                } catch (SQLException e) {
                    System.out.println("Something went wrong at clicking");
                }

            });

        } else if ("cooldown".equals(key)) {

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_NODAMAGE, SoundCategory.PLAYERS, 1.0f, 1.0f);

        }

    }

}
