package me.kryzen.dailyRewards.managers;

import me.kryzen.dailyRewards.DailyRewards;
import me.kryzen.dailyRewards.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class RewardManager {

    private final DailyRewards dailyRewards;
    private final DatabaseManager databaseManager;

    public RewardManager(DailyRewards dailyRewards, DatabaseManager databaseManager) {
        this.dailyRewards = dailyRewards;
        this.databaseManager = databaseManager;
    }

    public void processClaim(Player player, PlayerData data) {
        long currentTime = System.currentTimeMillis();
        long difference = currentTime - data.lastClaim();

        if (data.lastClaim() != 0 && difference < 86400000) {
            long remainingTime = 86400000 - difference;
            long hours = TimeUnit.MILLISECONDS.toHours(remainingTime);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime);

            player.sendMessage(Component.text("Please wait " + hours + "h" + minutes + "m to claim again!!").color(NamedTextColor.RED));
        }

        int newStreak;
        if (data.lastClaim() == 0 || difference > 172800000) {
            newStreak = 1;
            if (data.lastClaim() != 0) {
                player.sendMessage(Component.text("You missed a day! Streak reset to 1").color(NamedTextColor.RED));
            }
        } else {
            newStreak = data.streak() + 1;
        }

        giveReward(player, newStreak);

        Bukkit.getAsyncScheduler().runNow(dailyRewards, task -> {

            try {
                databaseManager.updatePlayerData(player.getUniqueId(), currentTime, newStreak);
            } catch (SQLException e) {
                player.sendMessage("Something went wrong while processing claim!");
            }

        });
    }

    private void giveReward(Player player, int streak) {
        switch (streak) {
            case 1,2 -> {
                player.getInventory().addItem(ItemStack.of(Material.BREAD, 16));
                player.sendMessage(Component.text("Claimed! ").color(NamedTextColor.GREEN)
                        .append(Component.text("Bread x16 ").color(NamedTextColor.YELLOW))
                        .append(Component.text("(Streak: " + streak + ")").color(NamedTextColor.LIGHT_PURPLE)));
            }
            case 3,4,5,6 -> {
                player.getInventory().addItem(ItemStack.of(Material.IRON_INGOT, 12));
                player.sendMessage(Component.text("Claimed! ").color(NamedTextColor.GREEN)
                        .append(Component.text("Iron Ingot x16 ").color(NamedTextColor.WHITE))
                        .append(Component.text("(Streak: " + streak + ")").color(NamedTextColor.LIGHT_PURPLE)));
            }
            default -> {
                player.getInventory().addItem(ItemStack.of(Material.DIAMOND, 8));
                player.sendMessage(Component.text("Claimed! ").color(NamedTextColor.GREEN)
                        .append(Component.text("Diamond x8 ").color(NamedTextColor.AQUA))
                        .append(Component.text("(Streak: " + streak + ")").color(NamedTextColor.LIGHT_PURPLE)));
            }
        }

        if (streak == 7 || streak == 30 || streak == 100) {
            Bukkit.getServer().sendMessage(Component.text(player.getName())
                    .append(Component.text(" just hit a ").color(NamedTextColor.GREEN))
                    .append(Component.text(streak).color(NamedTextColor.YELLOW))
                    .append(Component.text(" day streak!").color(NamedTextColor.GREEN)));
        }

    }

}
