import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * The JUnit test, to see that the implemented Sets are compliant.
 * 
 * @author Volker Berlin
 */
@RunWith( Parameterized.class )
public class JunitTests {

    @Parameters(name="{0}")
    public static Collection<Object[]> data() {
        ArrayList<Object[]> list = new ArrayList<>();
        for( Class<? extends Set<Integer>> setClass : TestIntegerSet.classes ) {
            list.add( new Object[] { setClass } );
        }
        return list;
    }

    private Class<? extends Set<Integer>> setClass;

    private Set<Integer>                  set;

    public JunitTests( Class<? extends Set<Integer>> setClass ) {
        this.setClass = setClass;
    }

    @Before
    public void before() throws Exception {
        set = setClass.newInstance();
    }

    @Test
    public void simple() {
        assertEquals( 0, set.size() );
        set.add( 42 );
        assertEquals( 1, set.size() );
        assertTrue( set.contains( 42 ) );
        assertFalse( set.contains( 37 ) );
        set.add( 42 ); // add the same value should not change any
        assertEquals( 1, set.size() );
        assertTrue( set.contains( 42 ) );
        assertFalse( set.contains( 37 ) );
        assertFalse( set.remove( 41 ) );
        assertTrue( set.remove( 42 ) );
        assertEquals( 0, set.size() );
        assertFalse( set.contains( 42 ) );
        assertFalse( set.remove( 42 ) ); // remove the same value should not change any
        assertEquals( 0, set.size() );
        assertFalse( set.contains( 42 ) );
    }

    @Test
    public void notSetValueOfIntCompactSet() {
        assertEquals( 0, set.size() );
        set.add( 42 );
        set.add( Integer.MAX_VALUE - 43 );
        assertFalse( set.contains( Integer.MAX_VALUE - 42 ) );
        assertTrue( set.contains( 42 ) );
        assertTrue( set.contains( Integer.MAX_VALUE - 43 ) );
        set.add( Integer.MAX_VALUE - 42 );
        assertTrue( set.contains( Integer.MAX_VALUE - 42 ) );
        assertTrue( set.contains( 42 ) );
        assertTrue( set.contains( Integer.MAX_VALUE - 43 ) );
        assertEquals( 3, set.size() );
    }

    @Test
    public void onePage() {
        // add a full page of values
        for( int i = 0; i < 0x10000; i++ ) {
            assertFalse( set.contains( i ) );
            assertTrue( set.add( i ) );
            assertEquals( i + 1, set.size() );
            assertTrue( set.contains( i ) );
        }
        // add the same values again
        for( int i = 0; i < 0x10000; i++ ) {
            assertTrue( set.contains( i ) );
            assertFalse( set.add( i ) );
            assertEquals( 0x10000, set.size() );
            assertTrue( set.contains( i ) );
        }
        // remove the values
        for( int i = 0; i < 0x10000; i++ ) {
            assertTrue( set.contains( i ) );
            set.remove( i );
            assertEquals( 0xFFFF - i, set.size() );
            assertFalse( set.contains( i ) );
        }
    }

    @Test
    public void reverseOnePage() {
        for( int i = 0xFFFF; i >= 0; i-- ) {
            assertFalse( set.contains( i ) );
            set.add( i );
            assertEquals( 0x10000 - i, set.size() );
            assertTrue( set.contains( i ) );
        }
        for( int i = 0xFFFF; i >= 0; i-- ) {
            assertTrue( set.contains( i ) );
            set.remove( i );
            assertEquals( i, set.size() );
            assertFalse( set.contains( i ) );
        }
    }

    @Test
    public void collision() {
        // initial size is 3 of CompactSet and IntCompactSet
        set.add( 3 );
        set.add( 6 ); // a collision 
        set.remove( 3 );
        assertTrue( set.contains( 6 ) );
        assertFalse( set.contains( 3 ) );
    }

    @Test
    public void iterator() {
        assertFalse( set.iterator().hasNext() );
        set.add( 42 );
        set.add( 123 );
        set.add( 12345678 );

        int count = 0;
        Iterator iterator = set.iterator();
        for( ; iterator.hasNext(); ) {
            iterator.next();
            count++;
        }
        assertEquals( 3, count );
        
        try {
            iterator.next();
            fail("NoSuchElementException expected");
        } catch( NoSuchElementException e ) {
            // expected;
        }

        // call next without hasNext() before
        iterator = set.iterator();
        iterator.next();
        iterator.next();
        iterator.next();
        try {
            iterator.next();
            fail("NoSuchElementException expected");
        } catch( NoSuchElementException e ) {
            // expected;
        }
    }

}
