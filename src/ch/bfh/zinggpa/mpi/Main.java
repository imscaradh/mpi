/*
 * Copyright (c) 2015 Berner Fachhochschule, Switzerland.
 *
 * Project Smart Reservation System.
 *
 * Distributable under GPL license. See terms of license at gnu.org.
 */
package ch.bfh.zinggpa.mpi;

import mpi.MPI;

public class Main {
    public static final boolean DEBUG = true;
    public static final boolean PARALLEL = true;

    public static void main(String[] args) {
        if (PARALLEL) MPI.Init(args);
        int[] puzzleRepresentation = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}; // 3x3 config
        //int[] puzzleRepresentation = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 15, 14}; // 4x4 config
        //int[] puzzleRepresentation = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19}; // 5x4 config

        long start = System.currentTimeMillis();
        Puzzle p = new Puzzle(puzzleRepresentation, 3, 3);
        boolean solved = (PARALLEL) ? p.parallelSolve() : p.sequentialSolve();

        double end = System.currentTimeMillis();

        System.out.printf("\n---------- Puzzle %s solved in %f ms --------------\n", (solved ? "was" : "was NOT"), (end - start));

        if (PARALLEL) MPI.Finalize();
    }
}