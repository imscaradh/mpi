from mpi4py import MPI
import numpy as np
import math

a = np.array([
    [1, 2, 3, 4],
    [5, 6, 7, 8],
    [9, 10, 11, 12],
    [13, 14, 15, 16]])

b = np.array([
    [1, 2, 3, 4],
    [5, 6, 7, 8],
    [9, 10, 11, 12],
    [13, 14, 15, 16]])

global c
global dim


def init_shift_left(matrix, index):
    row_length = matrix.shape[0]
    for l in range(index):
        tmp = matrix[index][0]
        for i in range(row_length - 1):
            matrix[index][i] = matrix[index][i + 1]
        matrix[index][-1] = tmp


def init_shift_up(matrix, index):
    row_length = matrix.shape[0]
    for l in range(index):
        tmp = matrix[0][index]
        for i in range(row_length - 1):
            matrix[i][index] = matrix[i + 1][index]
        matrix[-1][index] = tmp


def shift_left(matrix):
    dim = matrix.shape[0]
    for i in range(dim):
        tmp = matrix[i][0]
        for j in range(dim - 1):
            matrix[i][j] = matrix[i][j + 1]
        matrix[i][-1] = tmp


def shift_up(matrix):
    dim = matrix.shape[0]
    for i in range(dim):
        tmp = matrix[i][0]
        for j in range(dim - 1):
            matrix[i][j] = matrix[j + 1][i]
        matrix[-1][i] = tmp


def main():
    comm = MPI.COMM_WORLD
    dim = comm.Get_size()
    rank = comm.Get_rank()

    c = np.zeros((dim, dim))
    steps = int(math.sqrt(dim))
    result_quarter = np.zeros((steps, steps))

    for turns in range(dim + 1):
        data = []
        # send the whole stuff to processors
        if rank == 0:
            print("P%d (Core Proc) --------------------------" % rank)
            if turns == 0:
                for i in range(dim):
                    init_shift_left(a, i)

                for i in range(dim):
                    init_shift_up(b, i)
            else:
                shift_left(a)
                shift_up(b)

            for i in range(0, dim, steps):
                for j in range(0, dim, steps):
                    a_quarter = a[i:i + steps, j:j + steps]
                    b_quarter = b[i:i + steps, j:j + steps]
                    quarters_to_transfer = {'a': a_quarter, 'b': b_quarter}
                    data.append(quarters_to_transfer)

            print("Sending: \n", data)
        data = comm.scatter(data, root=0)

        a_quarter = data.get('a')
        b_quarter = data.get('b')
        steps = int(math.sqrt(dim))
        for i in range(steps):
            for j in range(steps):
                result_quarter[i, j] = result_quarter[i, j] + a_quarter[i, j] + b_quarter[i, j]
    
    data = comm.gather(result_quarter, root=0)

    # this is the last step
    if rank == 0:
        matrix_to_add = np.zeros((1, 4))
        print(data)
        steps = int(math.sqrt(dim))
        for i in range(0, dim, steps):
            temp = np.concatenate((data[i], data[i + 1]), axis=1)
            matrix_to_add = np.concatenate((matrix_to_add, temp), axis=0)

        # Little hack to prevent dimension problem while concatenating
        matrix_to_add = np.delete(matrix_to_add, (0), axis=0)
        c = np.add(c, matrix_to_add)

        shift_left(a)
#        shift_up(b)
        print(c)


if __name__ == '__main__':
    main()
