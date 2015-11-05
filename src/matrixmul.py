from mpi4py import MPI
import numpy as np
import math
import time

global a
global b
global c
global procs


def init_shift_left(matrix, rank, size, N):
    dim = matrix.shape[0]
    for i in range(dim):
        matrix[i] = np.roll(matrix[i], -((rank * N / size) + i))
    return matrix


def shift_left(matrix, size, procs):
    comm = MPI.COMM_WORLD
    rolled_arr = np.empty((size/procs, size))
    comm.Scatter(matrix, rolled_arr, root=0)
    rolled_arr = np.roll(rolled_arr, -1)
    comm.Gather(rolled_arr, matrix, root=0)

    return matrix


def main():
    comm = MPI.COMM_WORLD
    procs = comm.Get_size()
    rank = comm.Get_rank()
    wt = MPI.Wtime()

    N = 3000
    a = None
    b = None
    c = np.zeros((N, N))
    steps = int(N / math.sqrt(procs))
    result_quarter = np.zeros((steps, steps))

    if rank == 0:
        a = np.random.randint(1000, size=(N, N))
        b = np.random.randint(1000, size=(N, N))
        a = np.reshape(a, (procs, N * N / procs))
        b = np.transpose(b)
        b = np.reshape(b, (procs, (N * N) / procs))
    rolled_arr = np.zeros((N, N / procs))
    comm.Scatter(a, rolled_arr, root=0)
    transformed = init_shift_left(rolled_arr, rank, procs, N)
    comm.Gather(transformed, a, root=0)
    comm.Scatter(b, rolled_arr, root=0)
    transformed = init_shift_left(rolled_arr, rank, procs, N)
    comm.Gather(transformed, b, root=0)
    for turns in range(N):

        data = []
        # send the whole stuff to processors
        if turns > 0:
            a = shift_left(a, N, procs)
            b = shift_left(b, N, procs)

        if rank == 0:
            a_to_send = np.reshape(a, (N, N))
            b_to_send = np.transpose(np.reshape(b, (N, N)))
            for i in range(0, N, steps):
                for j in range(0, N, steps):
                    a_quarter = a_to_send[i:i + steps, j:j + steps]
                    b_quarter = b_to_send[i:i + steps, j:j + steps]
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

        #print("\n Result:\n", repr(c))

        used_time = MPI.Wtime() - wt
        print("Used time: %d ms" % (used_time * 1000))

if __name__ == '__main__':
    main()
