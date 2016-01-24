# Parallel Puzzle-Solver
This project should provide a puzzle solver which can solve NxM-Puzzles within a fair time. With MPJ the solver should work with multiple processors.

It's implemented with help of the IDA* algorithm, which computes in each iteration an heuristic. 
In this case we are using the Manhattan-Distance. It computes the sum of the ways to the desired location of each Puzzle tile.

## Configuration possiblities
* Sequential: Set the flag `Main.PARALLEL = false;`. There is a stack and the IDA* sequential algorithm to solve your input puzzle.
* Parallel: Set the flag `Main.PARALLEL = true;`. This implementation is still under construction.
