/*
 * Copyright (c) 2015 Berner Fachhochschule, Switzerland.
 *
 * Project Smart Reservation System.
 *
 * Distributable under GPL license. See terms of license at gnu.org.
 */
package ch.bfh.zinggpa.mpi;

public class Main {

    public static void main(String[] args) {
        int[] puzzleRepresentation = new int[]{0, 5, 2, 1, 8, 3, 4, 7, 6};

        long start = System.nanoTime();
        Puzzle p = new Puzzle(puzzleRepresentation, 3, 3);
        p.solve();

        long end = System.nanoTime();
        System.out.printf("\n---------- Puzzle solved in %.3f ms --------------\n", (end - start) * 10e-6);
    }
}