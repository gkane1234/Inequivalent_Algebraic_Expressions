
package com.github.gkane1234.unused;
import gnu.trove.impl.HashFunctions;
import gnu.trove.set.hash.TFloatHashSet;
import gnu.trove.impl.hash.TFloatHash;


// UNUSED

/**
    A custom float hash set that uses a custom hash function to avoid excessive hash collisions.

    Uses code from gnu.trove.set.hash.TFloatHashSet and gnu.trove.impl.hash.TFloatHash.
*/
public class CustomFloatHashSet extends TFloatHashSet {
    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private static final double CENTER = 0.5;
    private static final double MAX_EXPECTED = Math.pow(2, 10);

    private float[] table;


    public CustomFloatHashSet() {
        super(DEFAULT_CAPACITY, LOAD_FACTOR);
    }

    /**
     * Returns a hashcode for the specified value.
     *
     * @return  a hash code value for the specified value.
     */
    public static int hash(float value) {
        assert !Float.isNaN(value) : "Values of NaN are not supported.";
        //float compressedValue = (float)Math.log1p(Math.abs(value) / MAX_EXPECTED);
        return Float.floatToIntBits(value*663608941.737f);
        // this avoids excessive hashCollisions in the case values are
        // of the form (1.0, 2.0, 3.0, ...)
    }


    @Override
    public boolean add(float value) {
        int index = insertKey(value);

        if (index >= 0) {
            return false;
        }
        postInsertHook(consumeFreeSlot);

        return true;
    }

        /**
     * Locates the index of <tt>val</tt>.
     *
     * @param val an <code>float</code> value
     * @return the index of <tt>val</tt> or -1 if it isn't in the set.
     */
    protected int index( float val ) {
        int hash, probe, index, length;

        final byte[] states = _states;
        final float[] set = _set;
        length = states.length;
        hash = hash( val ) & 0x7fffffff;
        index = hash % length;
        byte state = states[index];

        if (state == FREE)
            return -1;

        if (state == FULL && set[index] == val)
            return index;

        return indexRehashed(val, index, hash, state);
    }

    int indexRehashed(float key, int index, int hash, byte state) {
        // see Knuth, p. 529
        int length = _set.length;
        int probe = 1 + (hash % (length - 2));
        final int loopIndex = index;

        do {
            index -= probe;
            if (index < 0) {
                index += length;
            }
            state = _states[index];
            //
            if (state == FREE)
                return -1;

            //
            if (key == _set[index] && state != REMOVED)
                return index;
        } while (index != loopIndex);

        return -1;
    }


    /**
     * Locates the index at which <tt>val</tt> can be inserted.  if
     * there is already a value equal()ing <tt>val</tt> in the set,
     * returns that value as a negative integer.
     *
     * @param val an <code>float</code> value
     * @return an <code>int</code> value
     */
    protected int insertKey( float val ) {
        int hash, index;

        hash = hash(val) & 0x7fffffff;
        index = hash % _states.length;
        byte state = _states[index];

        consumeFreeSlot = false;

        if (state == FREE) {
            consumeFreeSlot = true;
            insertKeyAt(index, val);

            return index;       // empty, all done
        }

        if (state == FULL && _set[index] == val) {
            return -index - 1;   // already stored
        }

        // already FULL or REMOVED, must probe
        return insertKeyRehash(val, index, hash, state);
    }

    int insertKeyRehash(float val, int index, int hash, byte state) {
        // compute the double hash
        final int length = _set.length;
        int probe = 1 + (hash % (length - 2));
        final int loopIndex = index;
        int firstRemoved = -1;

        /**
         * Look until FREE slot or we start to loop
         */
        do {
            // Identify first removed slot
            if (state == REMOVED && firstRemoved == -1)
                firstRemoved = index;

            index -= probe;
            if (index < 0) {
                index += length;
            }
            state = _states[index];

            // A FREE slot stops the search
            if (state == FREE) {
                if (firstRemoved != -1) {
                    insertKeyAt(firstRemoved, val);
                    return firstRemoved;
                } else {
                    consumeFreeSlot = true;
                    insertKeyAt(index, val);
                    return index;
                }
            }

            if (state == FULL && _set[index] == val) {
                return -index - 1;
            }

            // Detect loop
        } while (index != loopIndex);

        // We inspected all reachable slots and did not find a FREE one
        // If we found a REMOVED slot we return the first one found
        if (firstRemoved != -1) {
            insertKeyAt(firstRemoved, val);
            return firstRemoved;
        }

        // Can a resizing strategy be found that resizes the set?
        throw new IllegalStateException("No free or removed slots available. Key set full?!!");
    }

    void insertKeyAt(int index, float val) {
        _set[index] = val;  // insert value
        _states[index] = FULL;
    }

}
