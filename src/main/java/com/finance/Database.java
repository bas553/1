package com.finance;

import java.sql.*;
import java.util.*;

public class Database {
    private static final String URL = "jdbc:sqlite:finance.db";

    public void init() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT, type TEXT, account TEXT, category TEXT, amount REAL, description TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS notes (" +
                    "id INTEGER PRIMARY KEY, content TEXT)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addTransaction(String date, String type, String account, String category, double amount, String desc) {
        String sql = "INSERT INTO transactions(date, type, account, category, amount, description) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setString(2, type);
            pstmt.setString(3, account);
            pstmt.setString(4, category);
            pstmt.setDouble(5, amount);
            pstmt.setString(6, desc);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Transaction> transactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC, id DESC";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getString("date"),
                        rs.getString("type"),
                        rs.getString("account"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double[] summary() {
        double income = 0, expense = 0, debt = 0;
        String sql = "SELECT type, account, amount FROM transactions";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String type = rs.getString("type");
                String account = rs.getString("account");
                double amt = rs.getDouble("amount");

                if ("รายรับ".equals(type)) income += amt;
                if ("รายจ่าย".equals(type)) expense += amt;
                if ("บัตรเครดิต".equals(account) || "สินเชื่อ".equals(account)) debt += amt;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new double[]{income, expense, debt};
    }

    public Map<String, Double> getExpenseByCategory() {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT category, SUM(amount) as total FROM transactions WHERE type='รายจ่าย' GROUP BY category ORDER BY total DESC";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Map<String, Double> getTopExpenses(int limit) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT category, SUM(amount) as total FROM transactions WHERE type='รายจ่าย' GROUP BY category ORDER BY total DESC LIMIT " + limit;
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public void saveNote(String text) {
        String sql = "INSERT OR REPLACE INTO notes (id, content) VALUES (1, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, text);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String loadNote() {
        String sql = "SELECT content FROM notes WHERE id = 1";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getString("content");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getFinancialContext() {
        double[] s = summary();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("สรุปทางการเงินปัจจุบัน: รายรับรวม = ฿%.2f, รายจ่ายรวม = ฿%.2f, หนี้สินรวม = ฿%.2f. ", s[0], s[1], s[2]));
        sb.append("หมวดหมู่ค่าใช้จ่ายหลัก: ");
        getExpenseByCategory().forEach((cat, amt) -> sb.append(String.format("%s: ฿%.2f, ", cat, amt)));
        return sb.toString();
    }
}