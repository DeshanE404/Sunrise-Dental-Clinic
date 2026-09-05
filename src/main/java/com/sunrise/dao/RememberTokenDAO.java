package com.sunrise.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Stores and validates persistent "remember me" login tokens. Only a SHA-256
 * hash of the cookie token is ever persisted, and tokens automatically expire.
 */
public class RememberTokenDAO {

    public boolean saveToken(int userId, String tokenHash, Timestamp expiresAt) {
        String query = "INSERT INTO remember_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, tokenHash);
            preparedStatement.setTimestamp(3, expiresAt);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Returns the user id for a valid (unexpired) token, or null. */
    public Integer findUserIdByTokenHash(String tokenHash) {
        String query = "SELECT user_id FROM remember_tokens "
                + "WHERE token_hash = ? AND expires_at > CURRENT_TIMESTAMP";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, tokenHash);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteToken(String tokenHash) {
        String query = "DELETE FROM remember_tokens WHERE token_hash = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, tokenHash);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}