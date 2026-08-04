package me.kryzen.dailyRewards.managers;

import me.kryzen.dailyRewards.data.PlayerData;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {

    private Connection connection;

    public void connect(File dataFolder) throws SQLException {
        dataFolder.mkdirs();
        connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder.getAbsolutePath() + "/database.db");
    }

    public void createTable() throws SQLException {
        String run = "CREATE TABLE IF NOT EXISTS daily_rewards (uuid TEXT PRIMARY KEY, last_claim INTEGER, streak INTEGER)";
        Statement statement = connection.createStatement();
        statement.execute(run);
    }

    public PlayerData getPlayerData(UUID uuid) throws SQLException {
        String run = "SELECT last_claim, streak FROM daily_rewards WHERE uuid = ?";
        PreparedStatement statement = connection.prepareStatement(run);
        statement.setString(1, uuid.toString());

        ResultSet result = statement.executeQuery();
        if (result.next()) {

            long lastClaim = result.getLong("last_claim");
            int streak = result.getInt("streak");
            return new PlayerData(lastClaim, streak);
        }

        return new PlayerData(0, 0);

    }

    public void updatePlayerData(UUID uuid, long claimTime, int streak) throws SQLException {
        String run = "REPLACE INTO daily_rewards (uuid, last_claim, streak) VALUES (?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(run);
        statement.setString(1, uuid.toString());
        statement.setLong(2, claimTime);
        statement.setInt(3, streak);
        statement.executeUpdate();
    }

}
