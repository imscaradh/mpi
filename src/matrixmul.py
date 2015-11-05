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
    np.transpose(tmatrix)


def shift_left(matrix):
    np.roll(matrix, -1, 0)


def shift_up(matrix):
    shift_left(np.transpose(matrix))


def main():
    comm = MPI.COMM_WORLD
    procs = comm.Get_size()
    rank = comm.Get_rank()

    N = 3000

    a = np.random.randint(400, size=(N, N))
    b = np.random.randint(400, size=(N, N))
    c = np.zeros((N, N))

    steps = int(N / math.sqrt(procs))
    result_quarter = np.empty((steps, steps))

    sendData = np.empty([2 * steps, steps], dtype='i')
    recvData = np.empty([2 * steps, steps], dtype='i')

    for turns in range(N):
        wt = MPI.Wtime()
        # send the whole stuff to processors
        if rank == 0:
            if turns == 0:
                init_shift_left(a)
                init_shift_up(b)
            else:
                shift_left(a)
                shift_up(b)

            for i in range(0, N, steps):
                for j in range(0, N, steps):
                    a_quarter = a[i:i + steps, j:j + steps]
                    b_quarter = b[i:i + steps, j:j + steps]
                    sendData[0:steps, 0:steps] = a_quarter
                    sendData[steps:, 0:steps] = b_quarter

        comm.Scatter(sendData, recvData, root=0)

        a_quarter = recvData[0:steps]
        b_quarter = recvData[steps:]
        result_quarter = np.multiply(a_quarter, b_quarter)

        used_time = MPI.Wtime() - wt
        print("Proc", rank, ", time: ", (used_time * 1000))

    # this is the last step
    comm.Gather(result_quarter, recvData, root=0)
    if rank == 0:
        quarter_width = int(math.sqrt(procs))
        c = np.zeros((0, N))
        for i in range(quarter_width):
            temp = np.zeros((steps, 0))
            for j in range(i * quarter_width, (i + 1) * quarter_width):
                temp = np.concatenate((temp, recvData[j]), axis=1)
            c = np.concatenate((c, temp), axis=0)

        used_time = MPI.Wtime() - wt
        print("Used time: ", used_time * 1000)


if __name__ == '__main__':
    main()
