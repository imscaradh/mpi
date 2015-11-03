from mpi4py import MPI
import numpy as np
import math

global a
global b
global c
global procs


def init_shift_left(matrix):
    size = matrix.shape[0]
    for i in range(size):
        matrix[i] = np.roll(matrix[i], i * -1, 0)


def init_shift_up(matrix):
    tmatrix = np.transpose(matrix)
    init_shift_left(tmatrix)
    matrix = np.transpose(tmatrix)


def shift_left(matrix):
    size = matrix.shape[0]
    for i in range(size):
        matrix[i] = np.roll(matrix[i], -1, 0)


def shift_up(matrix):
    tmatrix = np.transpose(matrix)
    shift_left(tmatrix)
    matrix = np.transpose(tmatrix)


def main():
    comm = MPI.COMM_WORLD
    procs = comm.Get_size()
    rank = comm.Get_rank()
    wt = MPI.Wtime()

    N = 16

    a = np.random.randint(1000, size=(N, N))
    b = np.random.randint(1000, size=(N, N))
    c = np.zeros((N, N))

    steps = int(N / math.sqrt(procs))
    result_quarter = np.zeros((steps, steps))

    for turns in range(N):
        data = []
        # send the whole stuff to processors
        if rank == 0:
            if turns == 0:
                print("A matrix: \n", a)
                print("\nB matrix:\n", b)
                init_shift_left(a)
                init_shift_up(b)
            else:
                shift_left(a)
                shift_up(b)

            for i in range(0, N, steps):
                for j in range(0, N, steps):
                    a_quarter = a[i:i + steps, j:j + steps]
                    b_quarter = b[i:i + steps, j:j + steps]
                    quarters_to_transfer = {'a': a_quarter, 'b': b_quarter}
                    data.append(quarters_to_transfer)
        data = comm.scatter(data, root=0)

        a_quarter = data.get('a')
        b_quarter = data.get('b')
        for i in range(steps):
            for j in range(steps):
                result_quarter[i, j] = result_quarter[i, j] + a_quarter[i, j] * b_quarter[i, j]
    data = comm.gather(result_quarter, root=0)

    # this is the last step
    if rank == 0:
        quarter_width = int(math.sqrt(procs))
        c = np.zeros((0, N))
        for i in range(quarter_width):
            temp = np.zeros((steps, 0))
            for j in range(i * quarter_width, (i + 1) * quarter_width):
                temp = np.concatenate((temp, data[j]), axis=1)
            c = np.concatenate((c, temp), axis=0)

        print("\n Result:\n", repr(c))

        used_time = MPI.Wtime() - wt
        print("Used time: %d ms" % (used_time * 1000))

if __name__ == '__main__':
    main()
