
package com.github.gkane1234;

public class CustomFloatHashSet {
    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private static final double CENTER = 0.5;
    private static final double MAX_EXPECTED = Math.pow(2, 10);

    private float[] table;
    private boolean[] used;
    private int size;

    public CustomFloatHashSet() {
        table = new float[DEFAULT_CAPACITY];
        used = new boolean[DEFAULT_CAPACITY];
        size = 0;
    }

    private int computeHashCode(float value) {
        // Step 1: Center around 0.5 and calculate absolute distance
        double distanceFromCenter = Math.abs(value - CENTER); // Distance from 0.5
        double scaledValue = distanceFromCenter / MAX_EXPECTED;

        // Step 2: Apply log transformation for compression
        float compressedValue = (float)Math.log1p(scaledValue); // log1p(x) = log(1 + x)

        // Step 3: Convert to hash, including original sign to distinguish sides of 0.5
        int bits = Float.floatToIntBits(compressedValue); // Get raw bits of compressed value
        int hash = (int)(bits ^ (bits >>> 32)); // XOR upper and lower halves for better mixing

        // Step 4: Add sign bit information
        if (value < CENTER) {
            hash = ~hash; // Flip bits if value is on the negative side of the center
        }

        // Additional mixing of the hash
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b; // Multiply by a prime for further mixing
        return hash;
    }

    private int indexForHash(int hash) {
        return hash & (table.length - 1);
    }

    public boolean add(float value) {
        if (size >= table.length * LOAD_FACTOR) {
            resize();
        }

        int hash = computeHashCode(value);
        int index = indexForHash(hash);

        while (used[index]) {
            if (Float.compare(table[index], value) == 0) {
                return false;  // Value already in the set
            }
            index = (index + 1) % table.length;
        }

        table[index] = value;
        used[index] = true;
        size++;
        return true;
    }

    public boolean contains(float value) {
        int hash = computeHashCode(value);
        int index = indexForHash(hash);

        while (used[index]) {
            if (Float.compare(table[index], value) == 0) {
                return true;
            }
            index = (index + 1) % table.length;
        }

        return false;
    }

    private void resize() {
        float[] oldTable = table;
        boolean[] oldUsed = used;
        table = new float[oldTable.length * 2];
        used = new boolean[oldTable.length * 2];
        size = 0;

        for (int i = 0; i < oldTable.length; i++) {
            if (oldUsed[i]) {
                add(oldTable[i]);
            }
        }
    }
}
