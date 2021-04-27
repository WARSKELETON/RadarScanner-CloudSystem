package BIT;

import BIT.highBIT.*;
import java.io.*;
import java.util.*;

public class DynMCount {
    private static PrintStream out = null;
    private static int m_count = 0;

    /* main reads in all the files class files present in the input directory,
     * instruments them, and outputs them to the specified output directory.
     */
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
                    routine.addBefore("BIT/DynMCount", "mcount", new Integer(1));
                }

                ci.addAfter("BIT/DynMCount", "printMCount", ci.getClassName());
                ci.write(argv[1] + System.getProperty("file.separator") + infilename);
            }
        }
    }

    public static synchronized void printMCount(String foo) {
        System.out.println("Dynamic information summary:");
        System.out.println("Number of methods: " + m_count);
    }

    public static synchronized void mcount(int incr) {
        m_count++;
    }
}