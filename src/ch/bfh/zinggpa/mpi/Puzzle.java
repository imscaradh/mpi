/*
 * Copyright (c) 2015 Berner Fachhochschule, Switzerland.
 *
 * Project Smart Reservation System.
 *
 * Distributable under GPL license. See terms of license at gnu.org.
 */
package ch.bfh.zinggpa.mpi;


import mpi.MPI;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Stack;

public class Puzzle implements Serializable {
    public static final int WORK_REQUEST = 444;
    public static final int NODE_ADD_REQUEST = 445;
    private final int rows;
    private final int cols;
    private boolean solved;

    private int procSize;
    private int procRank;

    private Stack<Node> stack;
    private Node root;

    private Node actNode;

    public Puzzle(int[] puzzleRepresentation, int rows, int cols) {
        this.root = new Node(puzzleRepresentation, new int[]{0, 0});
        this.rows = rows;
        this.cols = cols;
        stack = new Stack<>();

        // MPJ stuff
        if (Main.PARALLEL) {
            procRank = MPI.COMM_WORLD.Rank();
            procSize = MPI.COMM_WORLD.Size();
        }
    }


    public boolean sequentialSolve() {
        try {
            int bound = root.getManhattanDistance();
            int maxBound = bound * 10;
            int result = 0;

            while (result != -1) {
                result = solve(root, bound);
                if (result >= maxBound) break;
                bound = result;

                while (!stack.isEmpty() && result != -1) {
                    Node nextCandidate = stack.pop();
                    LinkedList<Direction> unexplored = nextCandidate.getUnexplored();
                    for (Direction direction : unexplored) {
                        Node clone = nextCandidate.clone();
                        clone.setDir(direction);
                        clone.setParent(nextCandidate);
                        clone.incMoves();
                        result = solve(clone, clone.bound);
                        if (result >= maxBound) break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return solved;
    }

    public boolean parallelSolve() {
        try {
            int maxBound = 0;
            int bound = 0;
            int result = 0;

            // First round
            if (procRank == 0) {
                bound = root.getManhattanDistance();
                maxBound = bound * 10;
                result = solve(root, bound);
                bound = result;

                actNode = stack.pop();
            }

            while (!solved) {
                // Coordinator
                if (procRank == 0) {

                    //FIXME: That's wrong because we are using blocking operations!
                    for (int i = 1; i < procSize; i++) {
                        Object[] receive = receive(i);

                        int requestType = (int) receive[0];
                        if (Main.DEBUG) System.out.printf("Got Request Type %s from proc %d\n", requestType, i);

                        if (requestType == WORK_REQUEST) {
                            int tmpResult = (int) receive[1];
                            solved = (boolean) receive[2];
                            if (tmpResult != 0) result = tmpResult;
                            if (result >= maxBound) return false;

                            Node nodeForProc;

                            //FIXME: Proc 0 shouldn't do this work here..
                            while ((nodeForProc = getNextNode()) == null) {
                                result = solve(root, bound);
                                if (result >= maxBound) return false;
                                bound = result;
                            }

                            send(i, nodeForProc, nodeForProc.bound, solved);
                        }

                        if (requestType == NODE_ADD_REQUEST) {
                            Node nodeToAdd = (Node) receive[1];
                            stack.push(nodeToAdd);
                            if (Main.DEBUG) System.out.printf("Stack has now %d elements\n", stack.size());
                        }
                    }
                } else {
                    // Processor is ready for new work
                    send(0, WORK_REQUEST, result, solved);

                    Object[] receive = receive(0);
                    Node node = (Node) receive[0];
                    bound = (int) receive[1];
                    solved = (boolean) receive[2];

                    if (Main.DEBUG) System.out.printf("Proc %d: Got Node=[%s] \n", procRank, node.toString());
                    result = solve(node, bound);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return solved;
    }


    private Node getNextNode() throws CloneNotSupportedException {
        while (actNode.getUnexplored().isEmpty()) {
            if (stack.isEmpty()) {
                return null;
            }
            actNode = stack.pop();
        }

        Direction direction = actNode.getUnexplored().pop();

        Node clone = actNode.clone();
        clone.setDir(direction);
        clone.setParent(actNode);
        clone.incMoves();
        return clone;
    }

    private void send(int proc, Object... objects) {
        int count[] = new int[]{objects.length};
        MPI.COMM_WORLD.Send(count, 0, 1, MPI.INT, proc, 42);
        MPI.COMM_WORLD.Send(objects, 0, objects.length, MPI.OBJECT, proc, 43);
    }

    private Object[] receive(int proc) {
        int count[] = new int[1];
        MPI.COMM_WORLD.Recv(count, 0, 1, MPI.INT, proc, 42);
        Object objects[] = new Object[count[0]];
        MPI.COMM_WORLD.Recv(objects, 0, count[0], MPI.OBJECT, proc, 43);
        return objects;
    }

    private int solve(Node node, int bound) throws Exception {
        // Check if result reached
        node.bound = bound;
        if (node.isSorted()) {
            System.out.println("Manhattan-Distance: " + node.getManhattanDistance());
            Node traverse = node;
            int counter = 0;
            StringBuilder sb = new StringBuilder();
            while (traverse.getParent() != null) {
                sb.insert(0, String.format("-> %s ", traverse.dir));
                traverse = traverse.getParent();
                counter++;
            }
            System.out.println(counter - 1 + " moves:");
            System.out.println(sb.toString());
            System.out.println(node.toString());
            this.solved = true;
            return -1;
        }

        int f = node.getMoves() + node.getManhattanDistance();

        if (f > bound) return f;
        if (node.getDir() != null) node.move();

        LinkedList<Direction> possibleDirections = node.getPossibleDirections();
        Direction candidate = possibleDirections.remove(0);
        node.setUnexplored(possibleDirections);
        Node newNode = node.clone();
        newNode.incMoves();
        newNode.setParent(node);
        newNode.setDir(candidate);

        if (procRank > 0) send(0, NODE_ADD_REQUEST, node);
        else stack.push(node);

        int solve = solve(newNode, bound);
        if (solve == -1 || solve < Integer.MAX_VALUE) return solve;
        else throw new Exception("Max integer space reached");
    }

    enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    class Node implements Cloneable, Serializable {
        private Node parent;
        private int[] puzzle;
        private int[] spacerPos;
        private int moves;
        private Direction dir;
        private int bound;
        private LinkedList<Direction> unexplored;

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

            int tmp = puzzle[spacerPos[1] * cols + spacerPos[0]];
            puzzle[spacerPos[1] * cols + spacerPos[0]] = puzzle[newPos[1] * cols + newPos[0]];
            puzzle[newPos[1] * cols + newPos[0]] = tmp;
            spacerPos = newPos;
        }

        private int getManhattanDistanceForPosition(int[] pos) {
            int number = puzzle[pos[1] * cols + pos[0]];
            int destIdx = (number == 0) ? puzzle.length - 1 : number - 1;
            int destX = destIdx % rows;
            int destY = destIdx / rows;
            return Math.abs(destX - pos[0]) + Math.abs(destY - pos[1]);
        }

        public int getManhattanDistance() {
            int result = 0;
            for (int i = 0; i < cols; i++) {
                for (int j = 0; j < rows; j++) {
                    if (puzzle[j * cols + i] != 0) result += getManhattanDistanceForPosition(new int[]{i, j});
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
            //TODO: Outsource into a lookup table
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

        public LinkedList<Direction> getUnexplored() {
            return unexplored;
        }

        public void setUnexplored(LinkedList<Direction> unexplored) {
            this.unexplored = unexplored;
        }

        @Override
        public String toString() {
            return Arrays.toString(puzzle);
        }
    }
}
