/* GENERATED SOURCE. DO NOT MODIFY. */
/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.translit;

import java.util.ArrayList;

import android.icu.text.Transliterator;
import org.junit.runner.RunWith;
import android.icu.junit.IcuTestFmwkRunner;

// Test for ICU Ticket #7201.  With threading bugs in RuleBasedTransliterator, this
//   test would reliably crash.

@RunWith(IcuTestFmwkRunner.class)
public class ThreadTest extends TransliteratorTest {
    public static void main(String[] args) throws Exception {
        new ThreadTest().run(args);
    }
    
    private ArrayList<Worker> threads = new ArrayList<Worker>();
    private int iterationCount = 100000;
    
    public void TestThreads()  {
        if (getInclusion() >= 9) {
            // Exhaustive test.  Run longer.
            iterationCount = 1000000;
        }
        
        for (int i = 0; i < 8; i++) {
            Worker thread = new Worker();
            threads.add(thread);
            thread.start();
        }
        long expectedCount = 0;
        for (Worker thread: threads) {
            try {
                thread.join();
                if (expectedCount == 0) {
                    expectedCount = thread.count;
                } else {
                    if (expectedCount != thread.count) {
                        errln("Threads gave differing results.");
                    }
                }
            } catch (InterruptedException e) {
                errln(e.toString());
            }
        }
    }
    
    private static final String [] WORDS = {"edgar", "allen", "poe"};
   
    private class Worker extends Thread {   
        public long count = 0;
        public void run() {
            Transliterator tx = Transliterator.getInstance("Latin-Thai");        
            for (int loop = 0; loop < iterationCount; loop++) {
                for (String s : WORDS) {
                    count += tx.transliterate(s).length();
                }                
            }
        }
    }
    
    // Test for ticket #10673, race in cache code in AnyTransliterator.
    // It's difficult to make the original unsafe code actually fail, but
    // this test will fairly reliably take the code path for races in 
    // populating the cache.
    // 
    public void TestAnyTranslit() {
        final Transliterator tx = Transliterator.getInstance("Any-Latin");
        ArrayList<Thread> threads = new ArrayList<Thread>();
        for (int i=0; i<8; i++) {
            threads.add(new Thread() {
                public void run() {
                    tx.transliterate("??????????????????????????");
                }
            });
        }
        for (Thread th:threads) {
            th.start();
        }
        for (Thread th:threads) {
            try {
                th.join();
            } catch (InterruptedException e) {
                errln("Uexpected exception: " + e);
            }
        }
    }

}
