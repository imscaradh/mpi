package ch.bfh.zinggpa.mpi;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zinggpa
 */
public class Puzzle {
    public static final int LIMIT = 10;
    private final int width;

    private int puzzleSize;
    private int[] puzzle;
    private Map<Integer, List<Integer>> tree;

    public Puzzle(int[] puzzle) {
        this.puzzle = puzzle;
        this.puzzleSize = puzzle.length;
        this.width = this.puzzleSize = (int) Math.sqrt(puzzle.length);
    }

    public void solve() {
        Node parent = new Node();
        parent.setNumber(1);
        parent.setLastPos(8);
        dfs(parent, LIMIT, 0);
    }

    public Node dfs(Node actualNode, int limit, int counter) {
        if (counter == limit) {
            //int actualProc = comm.Get_rank();
            //screamForWork(actualProc);
            return actualNode;
        } else {
            int direction_possibilities = getDirections(actualNode);
            int xPos = puzzleSize % width;

            perform_move(actualNode, Direction.UP);

            for (int i = 0; i < direction_possibilities; i++) {
                Puzzle.Node child_node = new Node();
                child_node.setParent(actualNode);
                //child_node.setMovDirection(direction_possibilities.get(i));
                child_node.number = i + 1 + actualNode.number;
                if (tree.get(actualNode.parent.number) == null) {
                    tree.put(actualNode.parent.number, new ArrayList<>());
                }
                List<Integer> childs = tree.get(actualNode.parent.number);
                childs.add(child_node.number);
                return dfs(child_node, limit, counter + 1);
            }
        }
        return null;
    }

    private void perform_move(Node actualNode, Direction direction) {
        switch (direction) {
            case UP:

                break;
            case DOWN:
                break;
            case LEFT:
                break;
            case RIGHT:
                break;
        }
    }

    public void screamForWork(int proc) {

    }

    private List<Integer> getNeighbours(int pos) {
        return null;
    }

    public int getDirections(Node node) {

        getNeighbours(node.getLastPos());
        List<Direction> result = new ArrayList<>();

        throw new NotImplementedException();
    }

    enum Direction {
        NA, UP, DOWN, LEFT, RIGHT
    }

    class Node {
        private int number = 0;
        private Direction movDirection = Direction.NA;
        private int lastPos = 0;
        private int puzzleSize = 9;
        private Node parent = null;

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public Direction getMovDirection() {
            return movDirection;
        }

        public void setMovDirection(Direction movDirection) {
            this.movDirection = movDirection;
        }

        public int getLastPos() {
            return lastPos;
        }

        public void setLastPos(int lastPos) {
            this.lastPos = lastPos;
        }

        public int getPuzzleSize() {
            return puzzleSize;
        }

        public void setPuzzleSize(int puzzleSize) {
            this.puzzleSize = puzzleSize;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }
    }
}
