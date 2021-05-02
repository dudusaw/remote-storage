package org.example.service;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBService {
    void connectDBFromConfig();

    Connection getConnection();

    void createUser(String login, String password) throws SQLException;

    void updateUserPassword(String login, String password, String newPassword) throws SQLException;

    void updateUserLogin(String login, String password, String newLogin) throws SQLException;

    boolean tryLogin(String login, String password) throws SQLException;

    void closeConnection();
}
