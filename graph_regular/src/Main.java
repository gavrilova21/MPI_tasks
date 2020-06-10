/*
Проверка графа на регулярность с использованием библиотеки MPI
 */

import mpi.MPI;
import mpi.MaxInt;
import mpi.Request;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Random;
import java.util.Arrays;
import java.util.Set;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

public class Main {
    public static int N=5;
    public static int maxint = 7483647;
    static int[][] d = new int[][] {{maxint, 1, maxint, maxint, maxint,maxint},
            {1,maxint,1,1,maxint,maxint},
            {maxint,1,maxint,1,1,maxint},
            {maxint,1,1,maxint,maxint,maxint},
            {maxint,maxint,1,maxint,maxint, maxint},
            {maxint,maxint,1,maxint,maxint, 1},
    };
    static int[] df;

    public static int[] get_rows(int[][] a, int num, int c){
        int[] mas = new int[N*c+10];
        int k=0;
        for(int i=num;i<num+c;++i){
            for(int j=0;j<N;j++){
                mas[k]=a[i][j];
                k++;
            }
        }
    return mas;
    }
    public static void main(String[] args) throws Exception {
        int numtasks,rank;
        long startwtime=0;
        MPI.Init(args);
        rank = MPI.COMM_WORLD.Rank();
        numtasks = MPI.COMM_WORLD.Size();
        int[] offset = new int[1];
        int[] result = new int[N*N];
        int sizep = N/numtasks;
        int[] df = get_rows(d, 0,N);
        /*---------------------------- master ----------------------------*/
        if (rank==0){
            startwtime = System.currentTimeMillis();
        }
        int[] rf = new int[sizep*N];
        result = new int[N];
        MPI.COMM_WORLD.Scatter(df, 0, sizep*N,MPI.INT, rf,0,sizep*N,MPI.INT,0);
        int[] st = new int[sizep];
        for (int i = 0; i < sizep; i++) {
            for (int j = 0; j < N; j++) {
                if(rf[i*N+j]!=maxint){
                    st[i]+=1;
                };
            }
        }
        MPI.COMM_WORLD.Gather(st,0,sizep,MPI.INT,result,0,sizep, MPI.INT,0);

        if (rank == 0){
            long endtime = System.currentTimeMillis();
            int f = result[0];
            boolean t = true;
            for(int i=0;i<N;i++){
                if(result[i]!=f){
                    t=false;
                }
            }
            if (t == true){
                System.out.println("Граф регулярный");
            }
            else {
                System.out.println("Граф не является регулярным");
            }
            System.out.println(" time " +(endtime-startwtime));

        }
        MPI.Finalize();
    }
}