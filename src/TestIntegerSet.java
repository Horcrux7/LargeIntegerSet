import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import sets.CompactSet;
import sets.IntCompactSet;
import sets.PagedIntSet;

/**
 * The performance test
 * 
 * @author Volker Berlin
 */
public class TestIntegerSet {

    public static Class<? extends Set<Integer>>[] classes =
                    new Class[] { PagedIntSet.class, IntCompactSet.class, CompactSet.class, HashSet.class, LinkedHashSet.class };

    public static void main( String[] args ) throws Exception {
        // heat up of the JVM
        byte[] dummyHeap = new byte[50_000_000];
        for( Class<? extends Set<Integer>> clazz : classes ) {
            if( dummyHeap != null )
                System.out.println( "Heat up of " + clazz.getName() );
            testSet( clazz, 1 );
            testSet( clazz, 10 );
            testSet( clazz, 100 );
            testSet( clazz, 1000 );
            testSet( clazz, 10000 );
            testSet( clazz, 100000 );
        }
        dummyHeap = null;

        // final test
        TestResult[] results = new TestResult[classes.length];
        for( int i = 0; i < classes.length; i++ ) {
            Class<? extends Set<Integer>> clazz = classes[i];
            TestResult result = testSet( clazz, 1000_000 );
            result.print();
            results[i] = result;
        }

        // print the test table
        System.out.println();
        System.out.println( "Type | Memory in MB | add() in ms | remove() in ms | contains() in ms | iterate() in ms" );
        System.out.println( "---- | ------------ | ----------- | -------------- | ---------------- | ---------------" );

        for( TestResult testResult : results ) {
            System.out.println( testResult.type + " | " //
                            + testResult.memoryInMB() + " | " //
                            + testResult.addTime + " | " //
                            + testResult.removeTime + " | " //
                            + testResult.containsTime + " | " //
                            + testResult.iterateTime );
        }
    }

    /**
     * Run the test
     * 
     * @param setClass
     *            the class of the Set
     * @param size
     *            the iteration size
     * @return a Test Result
     * @throws Exception
     *             if any go wrong, we are in a test, we does not need an error handling
     */
    static TestResult testSet( Class<? extends Set<Integer>> setClass, int size ) throws Exception {
        TestResult result = new TestResult();
        result.type = setClass.getName();

        // add test
        long baseMemoryUsage = memoryUsage();
        long time = System.currentTimeMillis();
        Set<Integer> set = setClass.newInstance();
        for( int i = 0; i < size; i++ ) {
            set.add( i * 3 );
        }
        result.addTime = System.currentTimeMillis() - time;
        result.memUsage = memoryUsage() - baseMemoryUsage;

        // iterator test
        time = System.currentTimeMillis();
        for( int i = 0; i < size; i++ ) {
            set.contains( i );
        }
        result.containsTime = System.currentTimeMillis() - time;

        // iterator test
        time = System.currentTimeMillis();
        for( Integer integer : set ) {
        }
        result.iterateTime = System.currentTimeMillis() - time;

        // remove test
        time = System.currentTimeMillis();
        for( int i = 0; i < size; i++ ) {
            set.remove( i * 3 );
        }
        result.removeTime = System.currentTimeMillis() - time;

        // cleanup for the next test
        set = null;
        memoryUsage();

        return result;
    }

    static class TestResult {
        String type;

        long   memUsage;

        long   addTime;

        long   removeTime;

        long   containsTime;

        long   iterateTime;

        void print() {
            System.out.println();
            System.out.println( type );
            System.out.println( "========================" );
            System.out.println( "Memory Usage:      " + memoryInMB() + " MB" );
            System.out.println( "Time for add:      " + addTime + " milliseconds" );
            System.out.println( "Time for remove:   " + removeTime + " milliseconds" );
            System.out.println( "Time for contains: " + containsTime + " milliseconds" );
            System.out.println( "Time for iterate:  " + iterateTime + " milliseconds" );
        }

        double memoryInMB() {
            double mem = memUsage / 1024.0 / 1024.0;
            return Math.round( mem * 100 ) / 100.0;
        }
    }

    /**
     * Run garbage collection and calculate the current used memory
     * 
     * @return the minimum used memory
     */
    static long memoryUsage() throws Exception {
        // trigger the GC for a define count 
        System.gc();
        Thread.sleep( 1 );
        System.gc();
        Thread.sleep( 1 );
        System.gc();
        Thread.sleep( 5 );
        long memory = Long.MAX_VALUE;
        int count = 0;
        do {
            Runtime runtime = Runtime.getRuntime();
            long used = runtime.totalMemory() - runtime.freeMemory();
            if( used >= memory ) {
                if( count <= 0 ) {
                    return memory;
                }
                count--;
            } else {
                count = 2;
                memory = used;
            }
            Thread.sleep( 5 ); // wait on the result of gc
        } while( true );
    }
}
