package com.github.gkane1234.unused;
import gnu.trove.strategy.HashingStrategy;

// UNUSED


public class FloatHashingStrategy implements HashingStrategy<Float> {
    private static final double CENTER = 0.5;
    private static final double MAX_EXPECTED = Math.pow(2, 10); 
    public FloatHashingStrategy() {
        super(); // Default constructor
    }
    // Override the computeHashCode method with your custom hash function
    @Override
    public int computeHashCode(Float value) {
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
    @Override
    public boolean equals(Float o1, Float o2) {
        return o1.equals(o2);
    }

}