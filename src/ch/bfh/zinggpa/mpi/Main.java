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
    public static final boolean DEBUG = false;

    public static void main(String[] args) {
        MPI.Init(args);
        int[] puzzleRepresentation = new int[]{0, 5, 2, 1, 8, 3, 4, 7, 6};
        //int[] puzzleRepresentation = new int[]{0, 10, 4, 6, 1, 7, 11, 2, 13, 3, 9, 8, 5, 15, 14, 12};
        //int[] puzzleRepresentation = new int[]{0, 7, 4, 15, 5, 14, 18, 8, 3, 9, 23, 16, 11, 1, 10, 6, 22, 17, 12, 20, 2, 21, 13, 19, 24};

        long start = System.nanoTime();
        Puzzle p = new Puzzle(puzzleRepresentation, 3, 3);
        boolean solved = p.parallelSolve();

        long end = System.nanoTime();

        System.out.printf("\n---------- Puzzle %s solved in %.3f ms --------------\n", (solved ? "was" : "was NOT"), (end - start) * 10e-6);


        MPI.Finalize();
    }
}