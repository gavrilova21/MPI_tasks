import mpi.MPI;
import mpi.*;

public class Main {

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int n = 1000000; //100000, 10000
        VectDot.scatter_gather(rank, size,n);
        MPI.Finalize();
    }
}