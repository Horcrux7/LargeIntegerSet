package sets;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A low memory set. The values are saved in a Object array. 
 * If 2 values need to put on the same place in the array it will be put on the next free position of the cluster.  
 */
public class CompactSet extends AbstractSet<Object> {

    private static final float LOAD_FACTOR   = 0.75f;

    private static final float RESIZE_FACTOR = 1.5f;

    private Object[]           elements      = new Object[3];

    private int                size          = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add( Object obj ) {
        int slot = findSlot( obj );
        if( elements[slot] != null ) {
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
        int slot = findSlot( obj );
        if( elements[slot] == null ) {
            return false;
        }
        elements[slot] = null;
        tampCollisions( slot );
        size--;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains( Object o ) {
        int slot = findSlot( o );
        return elements[slot] != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Remove possible collisions after a delete
     * 
     * @param index the start index (position of delete)
     */
    private void tampCollisions( int index ) {
        for( int i = nextIndex( index ); elements[i] != null; i = nextIndex( i ) ) {
            int slot = findSlot( elements[i] );
            if( slot != i ) {
                elements[slot] = elements[i];
                elements[i] = null;
            }
        }
    }

    /**
     * Resize the the size of the table.
     */
    private void resize() {
        Object[] oldElements = elements;
        int capacity = java.lang.Math.max( (int)(RESIZE_FACTOR * elements.length), elements.length + 1 );
        elements = new Object[capacity];
        size = 0;
        for( int i = 0; i < oldElements.length; ++i )
            if( oldElements[i] != null ) {
                add( oldElements[i] );
            }
    }

    /**
     * Find the object or free place.
     * 
     * @param obj the new object
     * @return the index to the obj or null
     */
    private int findSlot( Object obj ) {
        // We multiply by 3 to add a gap to avoid large clusters if there continue values
        for( int i = java.lang.Math.abs( obj.hashCode() * 3 ) % elements.length;; i = nextIndex( i ) ) {
            if( elements[i] == null || elements[i].equals( obj ) ) {
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
    public Iterator<Object> iterator() {
        ArrayList<Object> list = new ArrayList<>(size);
        for( Object obj : elements ) {
            if( obj != null ) {
                list.add( obj );
            }
        }
        return list.iterator();
    }
}
