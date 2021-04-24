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


public class ICount {
    private static PrintStream out = null;
    private static int i_count = 0;

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
    public static void main(String argv[]) {
        File file_in = new File(argv[0]);
        String infilenames[] = file_in.list();

        for (int i = 0; i < infilenames.length; i++) {
            String infilename = infilenames[i];
            if (infilename.endsWith("Solver.class") || infilename.equals("SolverArgumentParser.class") || infilename.equals("Solver$Viewport.class") || infilename.equals("SolverArgumentParser$SolverParameters.class") || infilename.equals("SolverMain.class")) {
                // create class info object
                ClassInfo ci = new ClassInfo(argv[0] + System.getProperty("file.separator") + infilename);

                // loop through all the routines
                // see java.util.Enumeration for more information on Enumeration class
                for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    routine.addBefore("pt/ulisboa/tecnico/cnv/server/WebServer", "count", new Integer(routine.getInstructionCount()));

                    if (routine.getMethodName().equals("solveImage")) {
                        routine.addAfter("pt/ulisboa/tecnico/cnv/server/WebServer", "printThreadIcount", 0);
                    }
                }
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }

    public static synchronized void printICount(String foo) {
        System.out.println(i_count + " instructions");
    }

    public static synchronized void count(int incr) {
        i_count += incr;
    }

    public static synchronized int getCount() {
        return i_count;
    }
}
