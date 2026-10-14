package com.yr.seckilldemo.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public final class UserUtils {

    private static final int DEFAULT_USER_COUNT = 5000;
    private static final long DEFAULT_START_ID = 13000000000L;
    private static final String DEFAULT_PASSWORD = "123456";
    private static final String DEFAULT_SALT = "123456";

    private static final String DB_URL = System.getProperty(
            "seckill.db.url",
            "jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"
    );
    private static final String DB_USERNAME = System.getProperty("seckill.db.username", "root");
    private static final String DB_PASSWORD = System.getProperty("seckill.db.password", "123456");
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String LOGIN_URL = System.getProperty(
            "seckill.login.url",
            "http://localhost:8080/api/login"
    );
    private static final String TICKET_FILE = System.getProperty(
            "seckill.ticket.file",
            "F:/Miaosha/Config/config.txt"
    );

    private static final String INSERT_USER_SQL =
            "INSERT INTO t_user " +
            "(id, nickname, password, salt, regist_date, last_login_date, login_count) " +
            "VALUES (?, ?, ?, ?, ?, NULL, 0) " +
            "ON DUPLICATE KEY UPDATE " +
            "nickname = VALUES(nickname), " +
            "password = VALUES(password), " +
            "salt = VALUES(salt), " +
            "regist_date = VALUES(regist_date), " +
            "last_login_date = NULL, " +
            "login_count = 0";

    private UserUtils() {
    }

    public static void main(String[] args) throws Exception {
        int userCount = args.length > 0
                ? Integer.parseInt(args[0])
                : Integer.getInteger("seckill.user.count", DEFAULT_USER_COUNT);
        long startId = args.length > 1
                ? Long.parseLong(args[1])
                : Long.getLong("seckill.user.start-id", DEFAULT_START_ID);

        if (userCount <= 0) {
            throw new IllegalArgumentException("User count must be greater than zero");
        }

        createUsers(userCount, startId);
        loginAndWriteTickets(userCount, startId);
    }

    private static void createUsers(int userCount, long startId) throws Exception {
        Timestamp registerDate = new Timestamp(System.currentTimeMillis());

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(INSERT_USER_SQL)) {
                for (int i = 0; i < userCount; i++) {
                    long userId = startId + i;

                    statement.setLong(1, userId);
                    statement.setString(2, "user" + i);
                    statement.setString(3, MD5Utils.endpass(DEFAULT_PASSWORD, DEFAULT_SALT));
                    statement.setString(4, DEFAULT_SALT);
                    statement.setTimestamp(5, registerDate);
                    statement.addBatch();

                    if ((i + 1) % 500 == 0) {
                        statement.executeBatch();
                    }
                }

                statement.executeBatch();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }

        System.out.printf("Created or updated %d users, starting from %d%n", userCount, startId);
    }

    private static Connection openConnection() throws Exception {
        Class.forName(DB_DRIVER);
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    private static void loginAndWriteTickets(int userCount, long startId) throws Exception {
        Path outputPath = Path.of(TICKET_FILE).toAbsolutePath();
        Path parent = outputPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            for (int i = 0; i < userCount; i++) {
                long userId = startId + i;
                String userTicket = login(userId);

                writer.write(userId + "," + userTicket);
                writer.newLine();
            }
        }

        System.out.printf("Wrote %d user tickets to %s%n", userCount, outputPath);
    }

    private static String login(long userId) throws IOException {
        String form = "id=" + URLEncoder.encode(Long.toString(userId), StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(DEFAULT_PASSWORD, StandardCharsets.UTF_8);
        byte[] requestBody = form.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection connection = (HttpURLConnection) new URL(LOGIN_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        connection.setFixedLengthStreamingMode(requestBody.length);

        try {
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody);
            }

            int status = connection.getResponseCode();
            String userTicket = findCookie(connection, "userTicket");
            if (status < 200 || status >= 300 || userTicket == null || userTicket.isBlank()) {
                String responseBody = readBody(connection, status);
                throw new IllegalStateException(
                        "Login failed for user " + userId + ": HTTP " + status + ", response=" + responseBody
                );
            }
            return userTicket;
        } finally {
            connection.disconnect();
        }
    }

    private static String findCookie(HttpURLConnection connection, String cookieName) {
        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
            if (entry.getKey() == null || !"Set-Cookie".equalsIgnoreCase(entry.getKey())) {
                continue;
            }

            for (String headerValue : entry.getValue()) {
                String cookieValue = getCookieValue(headerValue, cookieName);
                if (cookieValue != null) {
                    return cookieValue;
                }
            }
        }
        return null;
    }

    private static String getCookieValue(String setCookieHeader, String cookieName) {
        for (String part : setCookieHeader.split(";")) {
            int separator = part.indexOf('=');
            if (separator <= 0) {
                continue;
            }

            String name = part.substring(0, separator).trim();
            if (cookieName.equals(name)) {
                return part.substring(separator + 1).trim();
            }
        }
        return null;
    }

    private static String readBody(HttpURLConnection connection, int status) throws IOException {
        InputStream inputStream = status >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
        if (inputStream == null) {
            return "";
        }

        try (InputStream stream = inputStream) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
