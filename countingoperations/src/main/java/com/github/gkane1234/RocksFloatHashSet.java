package com.github.gkane1234;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.nio.ByteBuffer;

/**
    A hash set implementation using RocksDB.
*/
public class RocksFloatHashSet {
    private final RocksDB db;
    private final String dbPath;

    /**
        Constructor for a RocksFloatHashSet.
        @param dbPath: a <code>String</code> representing the path to the RocksDB database.
        @throws RocksDBException: if there is an error opening the database.
    */
    public RocksFloatHashSet(String dbPath) throws RocksDBException {
        RocksDB.loadLibrary();
        Options options = new Options().setCreateIfMissing(true);
        this.db = RocksDB.open(options, dbPath);
        this.dbPath = dbPath;
    }

    /**
        Adds a float value to the set if it is not already present.
        @param value: a <code>float</code> representing the value to add.
        @return a <code>boolean</code> representing whether the value was added.
    */
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

    /**
        Checks if a float value exists in the set.
        @param value: a <code>float</code> representing the value to check.
        @return a <code>boolean</code> representing whether the value exists in the set.
    */
    public boolean contains(float value) {
        byte[] key = toBytes(value);
        try {
            return db.get(key) != null;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
        Removes a float value from the set.
        @param value: a <code>float</code> representing the value to remove.
    */
    public void remove(float value) {
        byte[] key = toBytes(value);
        try {
            db.delete(key);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    /**
        Converts a float to bytes.
        @param value: a <code>float</code> representing the value to convert.
        @return a <code>byte[]</code> representing the bytes of the float.
    */
    private byte[] toBytes(float value) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(value);
        return buffer.array();
    }

    /**
        Closes the RocksDB database.
    */
    public void close() {
        db.close();
    }

    /**
        Deletes the RocksDB database and all of its files.
    */
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

