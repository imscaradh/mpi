package ch.bfh.zinggpa.mpi;

public class Main {

    public static void main(String[] args) {
        long start = System.nanoTime();
        int[] puzzleRepresentation = new int[]{0, 5, 2, 1, 8, 3, 4, 7, 6};
        Puzzle p = new Puzzle(puzzleRepresentation, 3, 3);
        boolean solved = p.solve();

        if (solved) {
            long end = System.nanoTime();
            System.out.printf("\n------------- Puzzle solved in %.3f ms --------------\n", (end - start) * 10e-6);
        } else {
            System.out.println("Puzzle not solved!");
        }
    }
}