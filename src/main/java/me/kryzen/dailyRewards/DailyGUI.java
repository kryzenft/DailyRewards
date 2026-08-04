package me.kryzen.dailyRewards;

import me.kryzen.dailyRewards.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DailyGUI implements InventoryHolder {

    public static final NamespacedKey rewardKey = new NamespacedKey("daily-rewards", "items");

    private final Inventory inventory;

    public DailyGUI(PlayerData data) {
        this.inventory = Bukkit.createInventory(this, 27, Component.text("Daily Reward").color(NamedTextColor.DARK_GRAY));

        long currentTime = System.currentTimeMillis();
        long difference = currentTime - data.lastClaim();

        if (data.lastClaim() == 0 || difference >= 86400000) {
            inventory.setItem(13, getClaimItem(data.streak()));
        } else {
            long remainingTime = 86400000 - difference;
            long hours = TimeUnit.MILLISECONDS.toHours(remainingTime);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remainingTime);
            inventory.setItem(13, getCooldownItem(hours, minutes));
        }

    }

    private ItemStack getClaimItem(int currentStreak) {
        ItemStack item = ItemStack.of(Material.CHEST_MINECART);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Claim Daily Reward!").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Current Streak: " + currentStreak + " \uD83D\uDD25").color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false),
                    Component.text(" "),
                    Component.text("Click to claim your reward!").color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false)
            ));
            meta.getPersistentDataContainer().set(rewardKey, PersistentDataType.STRING, "claim");
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });

        return item;
    }

    private ItemStack getCooldownItem(long hours, long minutes) {
        ItemStack item = ItemStack.of(Material.MINECART);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Already Claimed").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(
                    Component.text("Come back in: " + hours + "h " + minutes + "m").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
            ));
            meta.getPersistentDataContainer().set(rewardKey, PersistentDataType.STRING, "cooldown");
        });
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
