package com.github.gkane1234;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHashSet {
    private final Connection conn;
    private final String tableName;
    private final int batchSize = 10000;
    private final List<Double> batchBuffer;
    
    public DBHashSet(String dbPath, String tableName) throws SQLException {
        this.tableName = tableName;
        this.batchBuffer = new ArrayList<>(batchSize);
        String url = "jdbc:sqlite:" + dbPath;
        this.conn = DriverManager.getConnection(url);
        initTable();
        optimizeForBulkInserts();
    }

    private void optimizeForBulkInserts() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode = WAL");
            stmt.execute("PRAGMA synchronous = NORMAL");
            stmt.execute("PRAGMA cache_size = -2000000"); // 2GB cache
            stmt.execute("PRAGMA temp_store = MEMORY");
            stmt.execute("PRAGMA mmap_size = 30000000000"); // 30GB memory mapping
        }
    }

    private void initTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + tableName + 
                        " (value REAL PRIMARY KEY) WITHOUT ROWID");
        }
    }

    public void add(double value) throws SQLException {
        batchBuffer.add(value);
        if (batchBuffer.size() >= batchSize) {
            flushBatch();
        }
    }

    public void flushBatch() throws SQLException {
        if (batchBuffer.isEmpty()) return;

        conn.setAutoCommit(false);
        String sql = "INSERT OR IGNORE INTO " + tableName + " (value) VALUES (?)";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Double value : batchBuffer) {
                pstmt.setDouble(1, value);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            conn.commit();
        } finally {
            conn.setAutoCommit(true);
            batchBuffer.clear();
        }
    }

    public boolean contains(double value) throws SQLException {
        String sql = "SELECT 1 FROM " + tableName + " WHERE value = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, value);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long size() throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.getLong(1);
        }
    }

    public void close() throws SQLException {
        try {
            flushBatch();
        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
    }
} 