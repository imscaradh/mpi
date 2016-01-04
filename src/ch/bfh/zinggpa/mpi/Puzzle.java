/*
 * Copyright (c) 2015 Berner Fachhochschule, Switzerland.
 *
 * Project Smart Reservation System.
 *
 * Distributable under GPL license. See terms of license at gnu.org.
 */
package ch.bfh.zinggpa.mpi;


import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class Puzzle {
    private int rows;
    private int cols;
    private Node root;
    private Stack<StackElement> stack;

    public Puzzle(int[] puzzleRepresentation, int rows, int cols) {
        this.root = new Node(puzzleRepresentation, new int[]{0, 0});
        this.rows = rows;
        this.cols = cols;
        this.stack = new Stack<>();
    }

    public void solve() {
        try {
            int bound = root.getTotalManhattanDistance();
            int maxBound = bound * 10;
            int result = 0;
            while (result != -1) {
                result = solve(root, bound);
                if (result >= maxBound) break;
                bound = result;
                // Now lets explore the unexplored paths :-)
                while (!stack.isEmpty() && result != -1) {
                    StackElement nextCandidate = stack.pop();
                    for (Direction direction : nextCandidate.getUnexplored()) {
                        Node parent = nextCandidate.getParent();
                        Node clone = parent.clone();
                        clone.setDir(direction);
                        clone.setParent(parent);
                        clone.incMoves();
                        result = solve(clone, clone.bound);
                        if (result >= maxBound) break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int solve(Node node, int bound) throws Exception {
        // Check if result reached
        node.bound = bound;
        if (node.isSorted()) {
            Node traverse = node;
            int counter = 0;
            StringBuilder sb = new StringBuilder();
            while (traverse.getParent() != null) {
                sb.insert(0, String.format("-> %s ", traverse.dir));
                traverse = traverse.getParent();
                counter++;
            }
            System.out.println(counter + " moves:");
            System.out.println(sb.toString());
            System.out.println(node.toString());

            return -1;
        }

        int f = node.getMoves() + node.getTotalManhattanDistance();

        if (f > bound) return f;
        if (node.getDir() != null) node.move();

        LinkedList<Direction> possibleDirections = node.getPossibleDirections();
        Direction candidate = possibleDirections.remove(0);
        Node newNode = node.clone();
        newNode.incMoves();
        newNode.setParent(node);
        newNode.setDir(candidate);

        stack.push(new StackElement(node, possibleDirections));

        int solve = solve(newNode, bound);
        if (solve == -1) return -1;
        if (solve < Integer.MAX_VALUE) return solve;
        else throw new Exception("Max integer space reached");
    }


    enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    class Node implements Cloneable {
        private Node parent;
        private int[] puzzle;
        private int[] spacerPos;
        private int moves;
        private Direction dir;
        private int bound;

        public Node(int[] puzzle, int[] spacerPos) {
            this.puzzle = puzzle;
            this.spacerPos = spacerPos;
            this.moves = 0;
            this.dir = null;
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

        public void move() {
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

        public int getTotalManhattanDistance() {
            int result = 0;
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    if (puzzle[j * rows + i] != 0) result += getManhattanDistance(new int[]{i, j});
                }
            }
            return result;
        }

        public LinkedList<Direction> getPossibleDirections() {
            LinkedList<Direction> possibleDirections = new LinkedList<>(Arrays.asList(Direction.values()));

            // No "reverse" move
            if (dir == Direction.UP) possibleDirections.remove(Direction.DOWN);
            if (dir == Direction.DOWN) possibleDirections.remove(Direction.UP);
            if (dir == Direction.LEFT) possibleDirections.remove(Direction.RIGHT);
            if (dir == Direction.RIGHT) possibleDirections.remove(Direction.LEFT);

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
            n.parent = null;
            return n;
        }

        public void incMoves() {
            moves++;
        }

        public int getMoves() {
            return moves;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Direction getDir() {
            return dir;
        }

        public void setDir(Direction dir) {
            this.dir = dir;
        }

        @Override
        public String toString() {
            return Arrays.toString(puzzle);
        }
    }

    class StackElement {
        private Node parent;
        private LinkedList<Direction> unexplored;

        public StackElement(Node parent, LinkedList<Direction> unexplored) {
            this.parent = parent;
            this.unexplored = unexplored;
        }

        public Node getParent() {
            return parent;
        }

        public LinkedList<Direction> getUnexplored() {
            return unexplored;
        }
    }
}
