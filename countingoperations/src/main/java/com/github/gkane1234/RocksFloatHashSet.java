package com.github.gkane1234;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.nio.ByteBuffer;

public class RocksFloatHashSet {
    private final RocksDB db;
    private final String dbPath;

    // Constructor to initialize RocksDB
    public RocksFloatHashSet(String dbPath) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        this.db = RocksDB.open(options, dbPath);
        this.dbPath = dbPath;
    }

    // Method to add a float value
    public boolean add(float value) {
        byte[] key = toBytes(value);
        try {
            if (contains(value)) {
                return false;
            }
            db.put(key, new byte[0]); 
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to check if a float value exists
    public boolean contains(float value) {
        byte[] key = toBytes(value);
        try {
            return db.get(key) != null;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to remove a float value
    public void remove(float value) {
        byte[] key = toBytes(value);
        try {
            db.delete(key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    // Helper to convert float to bytes
    private byte[] toBytes(float value) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(value);
        return buffer.array();
    }

    // Helper to convert bytes to float (optional, for debugging or other use cases)
    private float fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getFloat();
    }

    // Close the database
    public void close() {
        db.close();
    }

    public void delete() {
        Options options = new Options();
        System.err.println("Deleting database at "+dbPath);
        db.close();
        try {
            RocksDB.destroyDB(dbPath, options);
            System.err.println("Deleted database at "+dbPath);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }


}

