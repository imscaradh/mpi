package ch.bfh.zinggpa.mpi;

public class Main {

    public static void main(String[] args) {

        int[] puzzleRepresentation = new int[]{0, 5, 2, 1, 8, 3, 4, 7, 6};

        Puzzle p = new Puzzle(puzzleRepresentation, new int[]{0, 0}, 3, 3);
        p.solve();
    }
}