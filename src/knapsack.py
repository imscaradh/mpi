from mpi4py import MPI
import numpy as np
import math


def main():
    comm = MPI.COMM_WORLD
    dim = comm.Get_size()
    rank = comm.Get_rank()
    wt = MPI.Wtime()

    W = 5

    items = {
        2: 3,
        3: 4,
        4: 5,
        5: 6,
    }

    V = np.zeros((len(items) + 1, W + 1))

    for i in range(1, len(V)):
        for j in range(len(V[0])):
            item = items.get(i + 1)
            wi = 0 if item is None else i + 1
            if wi <= j:
                b = 0 if item is None else item
                V[i][j] = max(b + V[i - 1][j - wi], V[i - 1][j])
            else:
                V[i][j] = V[i - 1][j]

    print(np.transpose(V))
    used_time = MPI.Wtime() - wt
    print("Used time: %d ms" % (used_time * 1000))

if __name__ == '__main__':
    main()
