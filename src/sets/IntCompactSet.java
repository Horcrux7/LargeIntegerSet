package sets;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A low memory set for integer values. The values are saved in a int array. 
 * If 2 values need to put on the same place in the array it will be put on the next free position of the cluster.
 */
public class IntCompactSet extends AbstractSet<Integer> {

    private static final float LOAD_FACTOR   = 0.75f;

    private static final float RESIZE_FACTOR = 1.5f;

    private int[]              elements      = new int[3];

    private int                size;

    private int                notSetValue   = Integer.MAX_VALUE - 42;

    /**
     * Create a new instance.
     */
    public IntCompactSet() {
        Arrays.fill( elements, notSetValue );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add( Integer obj ) {
        if( obj == notSetValue ) {
            // collision
            int newNotSet = notSetValue-1;
            while( contains( newNotSet) ) newNotSet--;
            for( int i = 0; i < elements.length; i++ ) {
                if( elements[i] == notSetValue ) {
                    elements[i] = newNotSet;
                }
            }
            notSetValue = newNotSet;
        }
        int slot = findSlot( obj );
        if( elements[slot] != notSetValue ) {
            return false;
        }
        if( (size + 1) >= (LOAD_FACTOR * elements.length) ) {
            resize();
            slot = findSlot( obj );
        }
        elements[slot] = obj;
        size++;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove( Object obj ) {
        int slot = findSlot( (Integer)obj );
        if( elements[slot] == notSetValue ) {
            return false;
        }
        elements[slot] = notSetValue;
        tampCollisions( slot );
        size--;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains( Object o ) {
        int slot = findSlot( (Integer)o );
        return elements[slot] != notSetValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Remove possible collisions after a delete.
     * 
     * @param index the start index (position of delete)
     */
    private void tampCollisions( int index ) {
        for( int i = nextIndex( index ); elements[i] != notSetValue; i = nextIndex( i ) ) {
            int slot = findSlot( elements[i] );
            if( slot != i ) {
                elements[slot] = elements[i];
                elements[i] = notSetValue;
            }
        }
    }

    /**
     * Resize the the size of the table.
     */
    private void resize() {
        int[] oldElements = elements;
        int capacity = java.lang.Math.max( (int)(RESIZE_FACTOR * elements.length), elements.length + 1 );
        elements = new int[capacity];
        Arrays.fill( elements, notSetValue );
        size = 0;
        for( int i = 0; i < oldElements.length; ++i )
            if( oldElements[i] != notSetValue ) {
                add( oldElements[i] );
            }
    }

    /**
     * Find the object or a free place.
     * 
     * @param obj the new object
     * @return the index to the obj or null
     */
    private int findSlot( int obj ) {
        // We multiply by 2 to add a gap to avoid large clusters if there continue values
        for( int i = java.lang.Math.abs( obj * 3  ) % elements.length;; i = nextIndex( i ) ) {
            if( elements[i] == notSetValue || elements[i] == obj ) {
                return i;
            }
        }
    }

    /**
     * The next index rotating
     * 
     * @param index current index
     * @return the next index
     */
    private int nextIndex( int index ) {
        return (index + 1) % elements.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Integer> iterator() {
        ArrayList<Integer> list = new ArrayList<>(size);
        for( int obj : elements ) {
            if( obj != notSetValue ) {
                list.add( obj );
            }
        }
        return list.iterator();
    }
}
