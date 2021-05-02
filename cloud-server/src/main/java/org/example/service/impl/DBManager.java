package org.example.service.impl;

import org.example.factory.Factory;
import org.example.service.DBService;
import org.example.service.ServerService;

import java.sql.*;
import java.util.Properties;

public class DBManager implements DBService {


    private DBManager() {}

    public static DBService Instance = new DBManager();

    private final ServerService serverService = Factory.getServerService();
    private Connection connection;
    private PreparedStatement insertUser;
    private PreparedStatement updateUser;
    private PreparedStatement queryUser;

    @Override
    public void connectDBFromConfig() {
        try {
            Properties prop = Factory.getConfigProperties();
            String url = prop.getProperty("dbUrl");
            String user = prop.getProperty("dbUser");
            String password = prop.getProperty("dbPassword");
            connection = DriverManager.getConnection(url, user, password);
            insertUser = connection.prepareStatement("INSERT INTO accounts(login, password) VALUES(?, ?);");
            updateUser = connection.prepareStatement("UPDATE accounts SET (login = ?, password = ?) WHERE login = ?;");
            queryUser = connection.prepareStatement("SELECT count(*) FROM accounts WHERE login = ? AND password = ?;");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void createUser(String login, String password) throws SQLException {
        checkConnection();
        insertUser.setString(1, login);
        insertUser.setString(2, password);
        insertUser.execute();
    }

    private void checkConnection() {
        if (connection == null) {
            throw new IllegalStateException("no connection");
        }
    }

    @Override
    public void updateUserPassword(String login, String password, String newPassword) throws SQLException {
        checkConnection();
        if (tryLogin(login, password)) {
            updateUser.setString(1, login);
            updateUser.setString(2, newPassword);
            updateUser.setString(3, login);
        }
    }

    @Override
    public void updateUserLogin(String login, String password, String newLogin) throws SQLException {
        checkConnection();
        if (tryLogin(login, password)) {
            updateUser.setString(1, newLogin);
            updateUser.setString(2, password);
            updateUser.setString(3, login);
        }
    }

    @Override
    public boolean tryLogin(String login, String password) throws SQLException {
        checkConnection();
        queryUser.setString(1, login);
        queryUser.setString(2, password);
        ResultSet resultSet = queryUser.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) > 0;
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
