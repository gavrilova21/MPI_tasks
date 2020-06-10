import mpi.MPI;
import mpi.Request;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class VectDot {
    public static void blocking(int rank, int size, int n) throws IOException {
        int count_buf = n / (size-1);
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0){

            Random random = new Random();
            for (int i = 0;i < n;i++){
                a[i] = random.nextInt()%1000;
                b[i] = random.nextInt()%1000;
            }
            double starttime = System.currentTimeMillis();
            for (int i=1;i<size;i++){
                int[] buf = new int[2*count_buf]; // for saving a and b values in proccess
                for (int j=0;j<count_buf;j++){
                    buf[j] = a[(i-1)*count_buf+j];
                    buf[count_buf+j] = b[(i-1)*count_buf+j];
                }
                MPI.COMM_WORLD.Send(buf, 0,2*count_buf,MPI.INT,i,0);
            }
            for (int i = 1;i < size; i++){
                int[] tmp = new int[count_buf];
                MPI.COMM_WORLD.Recv(tmp,0, count_buf, MPI.INT, i, 0);
                System.arraycopy(tmp, 0, result, count_buf * (i - 1), count_buf);
            }
            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/times.txt",true));
            writer.append((endtime - starttime)+" ");

            writer.close();
            System.out.println("Result time "+(endtime-starttime));
        }
        else {
            int[] rbuf = new int[count_buf * 2];
            MPI.COMM_WORLD.Recv(rbuf, 0,2*count_buf, MPI.INT, 0, 0);
            int[] sbuf = new int[count_buf];
            for(int i = 0;i < count_buf; i++){
                sbuf[i] = rbuf[i] * rbuf[count_buf + i];
            }
            MPI.COMM_WORLD.Send(sbuf, 0, count_buf, MPI.INT, 0, 0);
        }
    }

    public static void non_blocking(int rank, int size, int n) throws IOException {
        int count_buf = n / (size - 1);
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0){

            Random random = new Random();
            for (int i = 0;i < n; i++){
                a[i] = random.nextInt()%1000;
                b[i] = random.nextInt()%1000;
            }
            double starttime = System.currentTimeMillis();
            for (int i = 1;i < size; i++){
                int[] buf = new int[2 * count_buf]; // for saving a and b values in proccess
                for (int j = 0;j < count_buf;j++){
                    buf[j] = a[(i-1) * count_buf+j];
                    buf[count_buf+j] = b[(i-1)*count_buf+j];
                }
                MPI.COMM_WORLD.Isend(buf, 0,2*count_buf,MPI.INT,i,0);
            }
            Request[] requests = new Request[size+1];
            for (int i=1;i<size;i++){
                int[] tmp= new int[count_buf];
                requests[i] = MPI.COMM_WORLD.Irecv(tmp,0, count_buf, MPI.INT, i, 0);
                requests[i].Wait();
                System.arraycopy(tmp, 0, result, count_buf * (i - 1), count_buf);
            }
            Request.Waitall(requests);
            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/nonblocking.txt",true));
            writer.append((endtime - starttime) + " ");
            writer.close();
            System.out.println("Result time " + (endtime-starttime));
        }
        else {
            int[] rbuf = new int[count_buf * 2];
            Request req = MPI.COMM_WORLD.Irecv(rbuf, 0, 2 * count_buf, MPI.INT, 0, 0 );
            req.Wait();
            int[] sbuf = new int[count_buf];
            for(int i = 0; i<count_buf; i++){
                sbuf[i] = rbuf[i] * rbuf[count_buf + i];
            }
            MPI.COMM_WORLD.Isend(sbuf, 0, count_buf, MPI.INT, 0, 0);
        }
    }

    public static void bufferic(int rank, int size, int n) throws IOException {
        int count_buf = n / (size - 1);
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0){

            Random random = new Random();
            for (int i = 0;i < n; i++){
                a[i] = random.nextInt()%1000;
                b[i] = random.nextInt()%1000;
            }
            double starttime = System.currentTimeMillis();
            for (int i = 1;i < size;i++){
                MPI.Buffer_detach();
                byte buffer[] = new byte[2*8*n + MPI.BSEND_OVERHEAD];
                int[] buf = new int[2*count_buf]; // for saving a and b values in proccess
                MPI.Buffer_attach(ByteBuffer.wrap(buffer));
                for (int j=0;j<count_buf;j++){
                    buf[j] = a[(i-1)*count_buf + j];
                    buf[count_buf+j] = b[(i-1)*count_buf+j];
                }
                MPI.COMM_WORLD.Bsend(buf, 0,2*count_buf,MPI.INT,i,0);
                MPI.Buffer_detach();
            }
            for (int i = 1;i < size; i++){
                int[] tmp = new int[count_buf];
                MPI.COMM_WORLD.Recv(tmp,0, count_buf, MPI.INT, i, 0);
                System.arraycopy(tmp, 0, result, count_buf * (i - 1), count_buf);
            }
            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/bufferic.txt", true));
            writer.append((endtime - starttime)+" ");
            writer.close();
            System.out.println("Result time "+(endtime-starttime));
        }
        else {
            int[] rbuf = new int[count_buf*2];
            MPI.COMM_WORLD.Recv(rbuf, 0, 2*count_buf, MPI.INT, 0, 0);
            int[] sbuf = new int[count_buf];
            for(int i = 0;i < count_buf;i++){
                sbuf[i] = rbuf[i]*rbuf[count_buf+i];
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(8*n + MPI.BSEND_OVERHEAD);
            MPI.Buffer_attach(buffer);
            MPI.COMM_WORLD.Bsend(sbuf, 0, count_buf,MPI.INT, 0, 0);
            MPI.Buffer_detach();
        }
    }

    public static void bufferic_non_blocking(int rank, int size, int n) throws IOException {
        int count_buf = n / (size - 1);
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0){
            Random random = new Random();
            for (int i = 0;i < n;i++){
                a[i] = random.nextInt()%1000;
                b[i] = random.nextInt()%1000;
            }
            double starttime = System.currentTimeMillis();

            for (int i = 1;i < size;i++){
                byte buffer[] = new byte[16*n+ MPI.BSEND_OVERHEAD];
                int[] buf = new int[2*count_buf]; // for saving a and b values in proccess
                MPI.Buffer_attach(ByteBuffer.wrap(buffer));
                for (int j = 0;j<count_buf;j++){
                    buf[j] = a[(i - 1)*count_buf+j];
                    buf[count_buf+j] = b[(i - 1)*count_buf + j];
                }
                MPI.COMM_WORLD.Ibsend(buf, 0, 2*count_buf, MPI.INT, i, 0);
                MPI.Buffer_detach();
            }
            Request[] requests = new Request[size+1];
            for (int i = 1;i < size; i++){
                int[] tmp = new int[count_buf];
                requests[i] = MPI.COMM_WORLD.Irecv(tmp, 0, count_buf, MPI.INT, i, 0);
                requests[i].Wait();
                System.arraycopy(tmp, 0, result, count_buf * (i - 1), count_buf);
            }
            Request.Waitall(requests);

            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/bufferic_nonblocking.txt",true));
            writer.append((endtime - starttime)+" ");
            writer.close();
            System.out.println("Result time "+(endtime - starttime));
        }
        else {
            int[] rbuf = new int[count_buf*2];
            Request req = MPI.COMM_WORLD.Irecv(rbuf, 0, 2*count_buf, MPI.INT, 0, 0);
            req.Wait();
            int[] sbuf = new int[count_buf];
            for(int i = 0;i < count_buf;i++){
                sbuf[i] = rbuf[i]*rbuf[count_buf+i];
            }
            ByteBuffer buffer = ByteBuffer.allocateDirect(8*n + MPI.BSEND_OVERHEAD);
            MPI.Buffer_attach(buffer);
            MPI.COMM_WORLD.Ibsend(sbuf, 0, count_buf, MPI.INT, 0, 0);
            MPI.Buffer_detach();
        }
    }

    public static void synchron(int rank, int size, int n) throws IOException {
        int count_buf = n / (size - 1);
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0) {

            Random random = new Random();
            for (int i = 0; i < n; i++) {
                a[i] = random.nextInt() % 1000;
                b[i] = random.nextInt() % 1000;
            }
            double starttime = System.currentTimeMillis();

            for (int i = 1; i < size; i++) {
                int[] buf = new int[2 * count_buf]; // for saving a and b values in proccess
                for (int j = 0; j < count_buf; j++) {
                    buf[j] = a[(i - 1) * count_buf + j];
                    buf[count_buf + j] = b[(i - 1) * count_buf + j];
                }
                MPI.COMM_WORLD.Ssend(buf, 0, 2 * count_buf, MPI.INT, i, 0);
            }
            for (int i = 1; i < size; i++) {
                int[] tmp = new int[count_buf];
                MPI.COMM_WORLD.Recv(tmp, 0, count_buf, MPI.INT, i, 0);
                System.arraycopy(tmp, 0, result, count_buf * (i - 1), count_buf);
            }

            long endtime =System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/syncron.txt",true));
            writer.append((endtime-starttime)+" ");
            writer.close();
            System.out.println("Result time "+(endtime-starttime));
        } else {
            int[] rbuf = new int[count_buf * 2];
            MPI.COMM_WORLD.Recv(rbuf, 0, 2 * count_buf, MPI.INT, 0, 0);
            int[] sbuf = new int[count_buf];
            for (int i = 0; i < count_buf; i++) {
                sbuf[i] = rbuf[i] * rbuf[count_buf + i];
            }
            MPI.COMM_WORLD.Ssend(sbuf, 0, count_buf, MPI.INT, 0, 0);
        }
    }

    public static void synchon_non_blocking(int rank, int size, int n) throws IOException {
        int count_buf = n/(size-1);
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0){

            Random random = new Random();
            for (int i=0;i<n;i++){
                a[i] = random.nextInt()%1000;
                b[i] = random.nextInt()%1000;
            }
            double starttime = System.currentTimeMillis();
            for (int i = 1;i<size;i++){
                int[] buf = new int[2*count_buf]; // for saving a and b values in proccess
                for (int j = 0;j<count_buf;j++){
                    buf[j] = a[(i-1)*count_buf+j];
                    buf[count_buf+j] = b[(i-1)*count_buf+j];
                }
                MPI.COMM_WORLD.Issend(buf, 0,2*count_buf,MPI.INT,i,0);
            }
            Request[] requests = new Request[size+1];
            for (int i = 1;i<size;i++){
                int[] tmp= new int[count_buf];
                requests[i] = MPI.COMM_WORLD.Irecv(tmp,0, count_buf, MPI.INT, i, 0);
                requests[i].Wait();
                System.arraycopy(tmp, 0, result, count_buf * (i - 1), count_buf);
            }
            Request.Waitall(requests);
            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/syncron_nonblocking.txt",true));
            writer.append((endtime - starttime)+" ");
            writer.close();
            System.out.println("Result time "+(endtime-starttime));
        }
        else {
            int[] rbuf = new int[count_buf*2];
            Request req = MPI.COMM_WORLD.Irecv(rbuf, 0,2*count_buf, MPI.INT, 0,0 );
            req.Wait();
            int[] sbuf = new int[count_buf];
            for(int i = 0;i<count_buf;i++){
                sbuf[i] = rbuf[i]*rbuf[count_buf+i];
            }
            MPI.COMM_WORLD.Issend(sbuf,0,count_buf,MPI.INT,0,0);
        }
    }


    public static void ready(int rank, int size, int n) throws IOException {
        int count_buf = n / (size - 1);
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0) {

            Random random = new Random();
            for (int i = 0; i < n; i++) {
                a[i] = random.nextInt() % 1000;
                b[i] = random.nextInt() % 1000;
            }
            double starttime = System.currentTimeMillis();
            for (int i = 1; i < size; i++) {
                int[] buf = new int[2 * count_buf]; // for saving a and b values in proccess
                for (int j = 0; j < count_buf; j++) {
                    buf[j] = a[(i - 1) * count_buf + j];
                    buf[count_buf + j] = b[(i - 1) * count_buf + j];
                }
                MPI.COMM_WORLD.Rsend(buf, 0, 2 * count_buf, MPI.INT, i, 0);
            }
            for (int i = 1; i < size; i++) {
                int[] tmp = new int[count_buf];
                MPI.COMM_WORLD.Recv(tmp, 0, count_buf, MPI.INT, i, 0);
                System.arraycopy(tmp, 0, result, count_buf * (i - 1), count_buf);
            }
            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/ready.txt",true));
            writer.append((endtime - starttime)+" ");
            writer.close();
            System.out.println("Result time "+(endtime-starttime));
        } else {
            int[] rbuf = new int[count_buf * 2];
            MPI.COMM_WORLD.Recv(rbuf, 0, 2 * count_buf, MPI.INT, 0, 0);
            int[] sbuf = new int[count_buf];
            for (int i = 0; i < count_buf; i++) {
                sbuf[i] = rbuf[i] * rbuf[count_buf + i];
            }
            MPI.COMM_WORLD.Rsend(sbuf, 0, count_buf, MPI.INT, 0, 0);
        }
    }

    public static void ready_non_blocking(int rank, int size, int n) throws IOException {
        int count_buf = n / (size-1);
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0){

            Random random = new Random();
            for (int i=0;i<n;i++){
                a[i] = random.nextInt()%1000;
                b[i] = random.nextInt()%1000;
            }
            double starttime = System.currentTimeMillis();
            for (int i = 1;i<size;i++){
                int[] buf = new int[2*count_buf]; // for saving a and b values in proccess
                for (int j=0;j<count_buf;j++){
                    buf[j] = a[(i-1)*count_buf+j];
                    buf[count_buf+j] = b[(i-1)*count_buf+j];
                }
                MPI.COMM_WORLD.Irsend(buf, 0,2*count_buf,MPI.INT,i,0);
            }
            Request[] requests = new Request[size+1];
            for (int i = 1;i<size;i++){
                int[] tmp = new int[count_buf];
                requests[i] = MPI.COMM_WORLD.Irecv(tmp,0, count_buf, MPI.INT, i, 0);
                requests[i].Wait();
                System.arraycopy(tmp, 0, result, count_buf * (i - 1), count_buf);
            }
            Request.Waitall(requests);
            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/ready_nonblocking.txt",true));
            writer.append((endtime - starttime)+" ");
            writer.close();
            System.out.println("Result time "+(endtime-starttime));
        }
        else {
            int[] rbuf = new int[count_buf*2];
            Request req = MPI.COMM_WORLD.Irecv(rbuf, 0,2*count_buf, MPI.INT, 0,0 );
            req.Wait();
            int[] sbuf = new int[count_buf];
            for(int i = 0;i<count_buf;i++){
                sbuf[i] = rbuf[i]*rbuf[count_buf+i];
            }
            MPI.COMM_WORLD.Irsend(sbuf,0,count_buf,MPI.INT,0,0);
        }
    }

    public static void bcast_reduse(int rank, int size, int n) throws IOException {
        int[] result = new int[n];
        int[] d = new int[1];
        long startwtime = 0;
        d[0] = n/size;
        int[] a = new int[n];
        int[] b = new int[n];
        MPI.COMM_WORLD.Bcast(d, 1, 0,MPI.INT, 0);
        if (rank==0){
            Random random = new Random();
            startwtime = System.currentTimeMillis();
            for (int i=0; i<n; i++){
                    a[i] = random.nextInt()%1000;
                    b[i] = random.nextInt()%1000;
            }
        }
        MPI.COMM_WORLD.Bcast(a, 0, n,MPI.INT, 0);
        MPI.COMM_WORLD.Bcast(b, 0, n,MPI.INT, 0);
        int[] sum = new int[1];
        for (int i=rank * d[0]; i<(rank+1)*d[0];i++){
            sum[0]+=a[i] * b[i];
        }
        MPI.COMM_WORLD.Reduce(sum,0,result,0,1,MPI.INT,MPI.SUM,0);
        if (rank == 0){
            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/broad.txt",true));
            writer.append((endtime-startwtime)+" ");
            writer.close();
            System.out.println(" time " +(endtime -startwtime));
        }
    }
    public static void scatter_gather(int rank, int size, int n) throws IOException {
        long startwtime = 0;
        int[] a = new int[n];
        int[] b = new int[n];
        int[] result = new int[n];
        if (rank == 0){
            Random random = new Random();
            startwtime = System.currentTimeMillis();
            for (int i = 0; i < n; i++){
                a[i] = random.nextInt()%1000;
                b[i] = random.nextInt()%1000;
            }
        }
        int sizep = n / size;
        int[] ap = new int[sizep];
        int[] bp = new int[sizep];
        MPI.COMM_WORLD.Scatter(a,0,sizep,MPI.INT,ap,0,sizep,MPI.INT,0);
        MPI.COMM_WORLD.Scatter(b,0,sizep,MPI.INT,bp,0,sizep,MPI.INT,0);
        int[] sum = new int[sizep];
        for (int i = 0; i<sizep;i++){
            sum[i] = ap[i]*bp[i];
        }
        MPI.COMM_WORLD.Gather(sum,0,sizep,MPI.INT,result,0,sizep, MPI.INT,0);
        if (rank == 0){
            long endtime = System.currentTimeMillis();
            BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/anastasiagavrilova/Desktop/scater.txt",true));
            writer.append((endtime-startwtime)+" ");
            writer.close();
            System.out.println(" time " +(endtime-startwtime));
            MPI.COMM_WORLD.
        }
    }
}
