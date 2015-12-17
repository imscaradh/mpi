package ch.bfh.zinggpa.mpi;

public class Main {

    public static void main(String[] args) {
        /*
            [7, 2, 3]
            [4, 6, 5]
            [1, 8, 0]
        */
        int[] puzzleRepresentation = new int[]{7, 2, 3, 4, 6, 5, 1, 8, 0};

        Puzzle p = new Puzzle(puzzleRepresentation);
        p.solve();
    }
}