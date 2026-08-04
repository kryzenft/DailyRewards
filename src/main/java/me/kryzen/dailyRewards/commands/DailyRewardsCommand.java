package me.kryzen.dailyRewards.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.kryzen.dailyRewards.DailyGUI;
import me.kryzen.dailyRewards.DailyRewards;
import me.kryzen.dailyRewards.data.PlayerData;
import me.kryzen.dailyRewards.managers.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class DailyRewardsCommand {

    public static LiteralCommandNode<CommandSourceStack> create(DailyRewards dailyRewards, DatabaseManager databaseManager) {

        return Commands.literal("daily")
                .requires(src -> src.getSender() instanceof Player && src.getSender().hasPermission("dailyrewards.daily"))
                .executes(ctx -> {

                    Player player = (Player) ctx.getSource().getSender();

                    player.sendMessage(Component.text("Opening menu...").color(NamedTextColor.YELLOW));

                    Bukkit.getAsyncScheduler().runNow(dailyRewards, task -> {
                        try {
                            PlayerData data = databaseManager.getPlayerData(player.getUniqueId());

                            Bukkit.getGlobalRegionScheduler().run(dailyRewards, task1 -> {
                                DailyGUI dailyGUI = new DailyGUI(data);
                                player.openInventory(dailyGUI.getInventory());
                            });

                        } catch (SQLException e) {
                            player.sendMessage(Component.text("Something went wrong at commands").color(NamedTextColor.RED));
                        }
                    });

                    return Command.SINGLE_SUCCESS;
                })
                .then(
                        Commands.literal("setstreak")
                                .requires(src -> src.getSender() instanceof Player && src.getSender().hasPermission("dailyrewards.daily.setstreak"))
                                .executes(ctx -> {

                                    Player player = (Player) ctx.getSource().getSender();

                                    player.sendMessage(Component.text("Usage: /daily setstreak <player> <amount>").color(NamedTextColor.RED));

                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(
                                        Commands.argument("target", ArgumentTypes.player())
                                                .then(
                                                        Commands.argument("amount", IntegerArgumentType.integer(0))
                                                                .executes(ctx -> {

                                                                    Player sender = (Player) ctx.getSource().getSender();

                                                                    PlayerSelectorArgumentResolver resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                                                                    Player target = resolver.resolve(ctx.getSource()).getFirst();

                                                                    int amount = IntegerArgumentType.getInteger(ctx, "amount");

                                                                    if (target == null) {
                                                                        sender.sendMessage(Component.text("Player not found or offline!").color(NamedTextColor.RED));
                                                                    }

                                                                    Bukkit.getAsyncScheduler().runNow(dailyRewards, task -> {

                                                                        try {
                                                                            PlayerData oldData = databaseManager.getPlayerData(target.getUniqueId());

                                                                            databaseManager.updatePlayerData(target.getUniqueId(), oldData.lastClaim(), amount);

                                                                            sender.sendMessage(Component.text("Successfully set " + target.getName() + "'s streak to " + amount + "!").color(NamedTextColor.GREEN));
                                                                        } catch (SQLException e) {
                                                                            sender.sendMessage(Component.text("Something went wrong while setting streak").color(NamedTextColor.RED));
                                                                        }

                                                                    });

                                                                    return Command.SINGLE_SUCCESS;
                                                                })
                                                )
                                )
                ).build();

    }

}
