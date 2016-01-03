/*
 * Copyright (c) 2015 Berner Fachhochschule, Switzerland.
 *
 * Project Smart Reservation System.
 *
 * Distributable under GPL license. See terms of license at gnu.org.
 */
package ch.bfh.zinggpa.mpi;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Puzzle {
    private int rows;
    private int cols;
    private Node root;

    public Puzzle(int[] puzzleRepresentation, int rows, int cols) {
        this.root = new Node(puzzleRepresentation, new int[]{0, 0});
        this.rows = rows;
        this.cols = cols;
    }

    public void solve() {
        int bound = root.getTotalManhattanDistance();
        int maxBound = bound * 10;

        int result = 0;
        try {
            while (result != -1) {
                result = solve(root, null, bound, 0);
                if (result >= maxBound) break;
                bound = result;
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private int solve(Node node, Direction actualDir, int bound, int moves) throws CloneNotSupportedException {
        // Check if result reached
        if (node.isSorted()) {
            System.out.println(node.toString());
            return -1;
        }

        int f = moves + node.getManhattanDistance();
        if (f > bound) {
            return f;
        }

        if (actualDir != null) {
            node.move(actualDir);
        }

        int min = Integer.MAX_VALUE;
        // Expand graph for each possible direction
        for (Direction dir : node.getPossibleDirections(actualDir)) {
            Node newNode = node.clone();
            node.addChild(newNode);
            int solve = solve(newNode, dir, bound, moves + 1);
            if (solve == -1) return -1;
            if (solve < min) min = solve;

        }
        return min;
    }


    enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    class Node implements Cloneable {
        private Collection<Node> children;
        private int[] puzzle;
        private int[] spacerPos;

        public Node(int[] puzzle, int[] spacerPos) {
            this.puzzle = puzzle;
            this.spacerPos = spacerPos;
            this.children = new ArrayList<>();
        }

        public boolean isSorted() {
            boolean sorted = true;
            if (puzzle[puzzle.length - 1] != 0) return false;
            for (int i = 0; i < puzzle.length - 1; i++) {
                if (puzzle[i + 1] != 0 && puzzle[i] > puzzle[i + 1]) {
                    sorted = false;
                    break;
                }
            }
            return sorted;
        }

        public void move(Direction dir) {
            int[] newPos = null;
            int x = spacerPos[0];
            int y = spacerPos[1];

            switch (dir) {
                case UP:
                    newPos = new int[]{x, y - 1};
                    break;
                case DOWN:
                    newPos = new int[]{x, y + 1};
                    break;
                case LEFT:
                    newPos = new int[]{x - 1, y};
                    break;
                case RIGHT:
                    newPos = new int[]{x + 1, y};
                    break;
            }

            int tmp = puzzle[spacerPos[1] * rows + spacerPos[0]];
            puzzle[spacerPos[1] * rows + spacerPos[0]] = puzzle[newPos[1] * rows + newPos[0]];
            puzzle[newPos[1] * rows + newPos[0]] = tmp;
            spacerPos = newPos;
        }


        private int getManhattanDistance(int[] pos) {
            int number = puzzle[pos[1] * rows + pos[0]];
            int destIdx = (number == 0) ? puzzle.length - 1 : number - 1;
            int destX = destIdx % rows;
            int destY = destIdx / rows;
            return Math.abs(destX - pos[0]) + Math.abs(destY - pos[1]);
        }

        public int getManhattanDistance() {
            return getManhattanDistance(spacerPos);
        }

        public int getTotalManhattanDistance() {
            int result = 0;
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    if (puzzle[j * rows + i] != 0) result += getManhattanDistance(new int[]{i, j});
                }
            }
            return result;
        }

        public List<Direction> getPossibleDirections(Direction actualDir) {
            List<Direction> possibleDirections = new ArrayList<>(Arrays.asList(Direction.values()));

            // No "reverse" move
            if (actualDir == Direction.UP) possibleDirections.remove(Direction.DOWN);
            if (actualDir == Direction.DOWN) possibleDirections.remove(Direction.UP);
            if (actualDir == Direction.LEFT) possibleDirections.remove(Direction.RIGHT);
            if (actualDir == Direction.RIGHT) possibleDirections.remove(Direction.LEFT);

            int x = spacerPos[0];
            int y = spacerPos[1];

            // Check if spacer is near a border
            if (x == 0) possibleDirections.remove(Direction.LEFT);
            if (y == 0) possibleDirections.remove(Direction.UP);
            if (x == cols - 1) possibleDirections.remove(Direction.RIGHT);
            if (y == rows - 1) possibleDirections.remove(Direction.DOWN);

            return possibleDirections;
        }

        @Override
        protected Node clone() throws CloneNotSupportedException {
            Node n;
            n = (Node) super.clone();
            n.puzzle = Arrays.copyOf(this.puzzle, puzzle.length);
            return n;
        }

        public void addChild(Node n) {
            this.children.add(n);
        }
        
        @Override
        public String toString() {
            return Arrays.toString(puzzle);
        }
    }
}
