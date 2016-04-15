package Oblig3;


import java.util.*;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by alexa on 12.04.2016.
 */
public class RadixPara {
    int threadCount = 4;


    public int runA(int[] intArray) { //Finds the largest number in an array. Speedup ~ 1.6
        int split = intArray.length / threadCount;
        int[] results = new int[threadCount]; //This is where the results are saved by the threads.
        FindLargestWorker[] workers = new FindLargestWorker[threadCount];

        //Splits up the array, and starts the threads
        for (int i = 0; i < threadCount; i++) {
            int start = split * i;
            int end;
            if (i == threadCount - 1) {
                end = intArray.length;
            } else {
                end = split * i + split;
            }

            workers[i] = new FindLargestWorker(i, intArray, start, end, results);
            workers[i].start();
        }

        //Waits for threads to finish
        for (FindLargestWorker w : workers) {
            try {
                w.join();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

        //Then returns the largest number
        return findLargest(results, 0, results.length);
    }


    private int findLargest(int[] intArray, int start, int end) {
        int largest = intArray[start];
        for (int i = start + 1; i < end; i++) {
            if (intArray[i] > largest) largest = intArray[i];
        }
        return largest;
    }

    class FindLargestWorker extends Thread {
        int threadNr;
        int[] intArray;
        int start;
        int end;
        int[] results;

        FindLargestWorker(int threadNr, int[] intArray, int start, int end, int[] results) {
            this.threadNr = threadNr;
            this.intArray = intArray;
            this.start = start;
            this.end = end;
            this.results = results;
        }

        @Override
        public void run() {
            results[threadNr] = findLargest(intArray, start, end);
        }
    }


    //(int[] intArray, int mask)
    public int[] runB(int[] intArray, int mask, int shift) {//Takes an array, and makes a "bit array" out of it.
        //I'm too lazy to pinpoint the speedup, but it reduces a 1200ms sort, to a 960 ms sort @ 100 mill numbers
        //And reduces a 300 mill sort from 4000ms to 2800ms
        //Max Number for both runs = 1000000000

        int[][] allCount = new int[threadCount][];
        int[] sumCount = new int[mask + 1]; //Arrayet vi returnerer
        CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
        int split = intArray.length / threadCount;

        for (int i = 0; i < threadCount; i++) {
            int start = split * i;
            int end;
            if (i == threadCount - 1) {
                end = intArray.length;
            } else {
                end = split * i + split;
            }
            new PartBWorker(i, intArray, start, end, allCount, sumCount, mask, shift, barrier).start();
        }

        try {
            barrier.await(); //It has to wait through 2x barriers.
            barrier.await(); //Everything is done.
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sumCount;

    }

    public class PartBWorker extends Thread {
        int threadNr;
        int[] intArray;
        int start;
        int end;
        int[][] allCount;
        int[] sumCount;
        int mask;
        int shift;
        CyclicBarrier barrier;

        int[] count;

        PartBWorker(int threadNr, int[] intArray, int start, int end, int[][] allCount, int[] sumCount, int mask, int shift, CyclicBarrier barrier) {
            this.threadNr = threadNr;
            this.intArray = intArray;
            this.start = start;
            this.end = end;
            this.allCount = allCount;
            this.sumCount = sumCount;
            this.mask = mask;
            this.shift = shift;
            this.barrier = barrier;

            count = new int[mask + 1]; //Local array
        }

        @Override
        public void run() {
            //Adds up this threads array values to its local count.
            for (int i = start; i < end; i++) {
                count[(intArray[i] >>> shift) & mask]++;
            }

            allCount[threadNr] = count;
            try {
                barrier.await();
            } catch (Exception e) {
                System.out.println("Exception in thread " + threadNr);
                e.printStackTrace();
            }

            //Divides the new array into equal parts
            int threadCount = allCount.length;
            int split = count.length / threadCount;
            start = split * threadNr;

            if (threadNr == threadCount - 1) {
                end = count.length;
            } else {
                end = split * threadNr + split;
            }


            //Goes through the start -> end numbers in all of the bitArrays, and adds them all to Count.
            for (int thread = 0; thread < allCount.length; thread++) { //For each thread
                for (int i = start; i < end; i++) { //Go through the assigned values
                    sumCount[i] += allCount[thread][i];
                }
            }
            try {
                barrier.await();
            } catch (Exception e) {
                System.out.println("Exception in thread " + threadNr);
                e.printStackTrace();
            }
        }
    }


    public void runC(int[] intArray) {
        /*
            Ta imot "bit"array og mask som argumenter

            del opp intArray i like store deler (int start, end) og start trådene

            Hver tråd går så gjennom sin del og regner ut av akumVal for sin del av arrayet og setter dette i et accumVal[threads] array
            Synkroniser
            Hver tråd regner ut av akkumVal før sin tråd:
                tråd 0 accumVal = accumVal[0] = 0;
                tråd 1 accumVal = accumVal[1]
                tråd 2 accumVal = accumVal[1] + accumVal[2]
                tråd 3 accumVal = accumVal[1] + accumVal[2] + accumVal[3]


            for (int i = start; i <= end; i++) {
                j = count[i];
                count[i] = acumVal;
                acumVal += j;
            }
        }

        int[] intArray = {0, 1, 1, 0, 0, 0, 1, 3, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        //FasitSvar: [0, 0, 1, 2, 2, 2, 2, 3, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10]
        System.out.println("[0, 1, 1, 0, 0, 0, 1, 3, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0]");
        */

        CyclicBarrier barrier = new CyclicBarrier(threadCount + 1);
        int[] acumValueAll = new int[threadCount];

        int split = intArray.length / threadCount;
        //Splits up the array
        for (int i = 0; i < threadCount; i++) {
            int start = split * i;
            int end;
            if (i == threadCount - 1) {
                end = intArray.length;
            } else {
                end = split * i + split;
            }
            new PartCWorker(i, intArray, acumValueAll, start, end, barrier).start();
        }

        try {
            barrier.await();
            barrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println(Arrays.toString(intArray));

    }

    class PartCWorker extends Thread {
        int threadNr;
        int[] intArray;
        int[] acumValAll;
        int start, end;
        CyclicBarrier barrier;

        PartCWorker(int threadNr, int[] intArray, int[] acumValAll, int start, int end, CyclicBarrier barrier) {
            this.threadNr = threadNr;
            this.intArray = intArray;
            this.acumValAll = acumValAll;
            this.start = start;
            this.end = end;
            this.barrier = barrier;
        }

        public void run() {
            int acumValLocal = 0;
            for (int i = start; i < end; i++) {
                acumValLocal += intArray[i];
            }
            //Puts acumVal in a shared array
            acumValAll[threadNr] = acumValLocal;

            //Then sync
            try {
                barrier.await();
            } catch (Exception e) {
                System.out.println("Failure in thread: " + threadNr);
                e.printStackTrace();
            }

            acumValLocal = 0;
            for (int i = 0; i < threadNr; i++) {
                acumValLocal += acumValAll[i];
            }


            //System.out.println("Thread: " + threadNr + " - acumValLocal: " + acumValLocal);

            int j;
            for (int i = start; i < end; i++) {
                j = intArray[i];
                intArray[i] = acumValLocal;
                acumValLocal += j;
            }

            try {
                barrier.await();
            } catch (Exception e) {
                System.out.println("Failure in thread: " + threadNr);
                e.printStackTrace();
            }
        }
    }


    public void runD(int[] a, int[] b, int[] count, int shift, int mask) {
        PartDWorker[] workers = new PartDWorker[threadCount];

        int split = a.length / threadCount;
        for (int i = 0; i < threadCount; i++) {
            int start = split * i;
            int end;
            if (i == threadCount - 1) {
                end = a.length;
            } else {
                end = split * i + split;
            }

            workers[i] = new PartDWorker(count, a, b, shift, mask, start, end);
            workers[i].start();
        }


        try{
            for(PartDWorker w : workers){
                w.join();
            }
            Thread.sleep(500);
        }catch(Exception e){
            e.printStackTrace();
        }


    }

    class PartDWorker extends Thread{
        int[] count;
        int[] a;
        int[] b;
        int shift;
        int mask;
        int start;
        int end;

        PartDWorker(int[] count, int[] a, int[] b, int shift, int mask, int start, int end) {
            this.count = count;
            this.a = a;
            this.b = b;
            this.shift = shift;
            this.mask = mask;
            this.start = start;
            this.end = end;
        }

        public void run() {
            for (int i = start; i < end; i++) {
                int tempInt = count[(a[i] >>> shift) & mask]++;
                b[count[(a[i] >>> shift) & mask]++] = a[i];

            }
        }

    }
}



