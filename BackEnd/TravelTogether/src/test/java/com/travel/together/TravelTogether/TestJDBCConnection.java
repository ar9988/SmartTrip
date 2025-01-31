package com.travel.together.TravelTogether;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestJDBCConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://43.202.51.112:3306/traveltogetherdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul";
        String user = "root";
        String password = "ssafy1234";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ MySQL 연결 성공!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
