/* ICount.java
 * Sample program using BIT -- counts the number of instructions executed.
 *
 * Copyright (c) 1997, The Regents of the University of Colorado. All
 * Rights Reserved.
 *
 * Permission to use and copy this software and its documentation for
 * NON-COMMERCIAL purposes and without fee is hereby granted provided
 * that this copyright notice appears in all copies. If you wish to use
 * or wish to have others use BIT for commercial purposes please contact,
 * Stephen V. O'Neil, Director, Office of Technology Transfer at the
 * University of Colorado at Boulder (303) 492-5647.
 */
package BIT;

import BIT.highBIT.*;
import java.io.*;
import java.util.*;

public class DynICount {
    private static PrintStream out = null;
    private static long i_count = 0;

    public static void main(String argv[]) {
        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();

        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith(".class")) {
                // create class info object
                ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);

                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();

                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        //bb.addBefore("BIT/DynICount", "dynInstrCount", new Integer(bb.size()));
                        bb.addBefore("pt/ulisboa/tecnico/cnv/server/WebServer", "count", new Integer(bb.size()));
                    }

                    // Up until the completion of the solveImage method resides the predominant cost of the request
                    if (routine.getMethodName().equals("solveImage")) {
                        routine.addAfter("pt/ulisboa/tecnico/cnv/server/WebServer", "printThreadIcount", 0);
                        //routine.addAfter("BIT/DynICount", "printDynamic", "foo");
                    }
                }
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }

    public static synchronized void dynInstrCount(int incr) {
        i_count += incr;
    }

    public static synchronized void printDynamic(String foo) {
        System.out.println("Dynamic information summary:");
        System.out.println("Number of instructions: " + i_count);
    }
}
