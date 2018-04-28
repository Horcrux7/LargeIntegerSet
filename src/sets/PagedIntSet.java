package sets;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

/**
 * A very low memory set for integer values. The values are saved in a pages of char arrays. 
 */
public class PagedIntSet extends AbstractSet<Integer> {

    private HashMap<Integer, Page> pages = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add( Integer e ) {
        int value = e.intValue();
        Integer pageID = Integer.valueOf( value >> 16 );
        Page page = pages.get( pageID );
        if( page == null ) {
            page = new Page();
            pages.put( pageID, page );
        }
        return page.add( (char)value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove( Object o ) {
        int value = ((Integer)o).intValue();
        Integer pageID = Integer.valueOf( value >> 16 );
        Page page = pages.get( pageID );
        if( page == null ) {
            return false;
        }
        if( page.remove( (char)value ) ) {
            if( page.size() == 0 ) {
                pages.remove( pageID );
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains( Object o ) {
        int value = ((Integer)o).intValue();
        Integer pageID = Integer.valueOf( value >> 16 );
        Page page = pages.get( pageID );
        if( page == null ) {
            return false;
        }
        return page.contains( (char)value );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {

            private Iterator<Entry<Integer, Page>> entries = pages.entrySet().iterator();

            private Page          page;

            private int              high;

            private int              idx;

            @Override
            public boolean hasNext() {
                if( page == null ) {
                    if( entries.hasNext() ) {
                        Entry<Integer, Page> entry = entries.next();
                        high = entry.getKey() << 16;
                        page = entry.getValue();
                        idx = 0;
                    } else {
                        return false;
                    }
                }
                int value =  findNextIdx();
                if( value >= 0 ) {
                    return true;
                }
                page = null;
                return hasNext();
            }

            @Override
            public Integer next() {
                if( page == null ) {
                    if( !hasNext() ) {
                        throw new NoSuchElementException();
                    }
                }
                int value =  findNextIdx();
                if( value >= 0 ) {
                    idx++;
                    return Integer.valueOf( high | value );
                }
                page = null;
                return next();
            }
            
            private int findNextIdx() {
                char[] values = page.elements;
                while( idx < values.length ) {
                    if( values[idx] != page.notSetValue ) {
                        return values[idx];
                    }
                    idx++;
                }
                return -1;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        int count = 0;
        for( Page page : pages.values() ) {
            count += page.size;
        }
        return count;
    }

    /**
     * A page that save until 65656 values.
     */
    private static class Page {

        private static final float LOAD_FACTOR   = 0.75f;

        private static final float RESIZE_FACTOR = 1.5f;

        private char[]              elements      = new char[3];

        private int                size;

        private char              notSetValue   = Character.MAX_VALUE;

        /**
         * Create a new instance.
         */
        Page() {
            Arrays.fill( elements, notSetValue );
        }

        boolean add( char obj ) {
            if( obj == notSetValue ) {
                // collision
                switch( size ) {
                    case Character.MAX_VALUE:
                        size++;
                        return true; // all possible values are set now
                    case Character.MAX_VALUE + 1:
                        return false; // all possible values are set already
                    default:
                        char newNotSet = (char)(notSetValue-1);
                        while( contains( newNotSet) ) newNotSet--;
                        for( int i = 0; i < elements.length; i++ ) {
                            if( elements[i] == notSetValue ) {
                                elements[i] = newNotSet;
                            }
                        }
                        notSetValue = newNotSet;
                }
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

        boolean remove( char obj ) {
            if( size > Character.MAX_VALUE ) { // all possible values are set
                size--;
                notSetValue = obj;
                return true;
            }
            int slot = findSlot( obj );
            if( elements[slot] == notSetValue ) {
                return false;
            }
            elements[slot] = notSetValue;
            tampCollisions( slot );
            size--;
            return true;
        }

        boolean contains( char o ) {
            if( size > Character.MAX_VALUE ) {
                return true; // all possible values are set
            }
            int slot = findSlot( o );
            return elements[slot] != notSetValue;
        }

        int size() {
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
            char[] oldElements = elements;
            int capacity = java.lang.Math.max( (int)(RESIZE_FACTOR * elements.length), elements.length + 1 );
            elements = new char[capacity];
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
    }

}
