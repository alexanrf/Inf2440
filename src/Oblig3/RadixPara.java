package Oblig3;


import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by alexa on 12.04.2016.
 */
public class RadixPara{
    int threadCount = 4;


    public int runA(int[] intArray){ //Finds the largest number in an array. Speedup ~ 1.6
        int split = intArray.length/threadCount;
        int[] results = new int[threadCount]; //This is where the results are saved by the threads.
        FindLargestWorker[] workers = new FindLargestWorker[threadCount];

        //Splits up the array, and starts the threads
        for(int i = 0; i < threadCount; i++){
            int start = split*i;
            int end;
            if(i == threadCount-1){
                end = intArray.length;
            }else{
                end = split*i + split;
            }

            workers[i] = new FindLargestWorker(i, intArray, start, end, results);
            workers[i].start();
        }

        //Waits for threads to finish
        for(FindLargestWorker w : workers){
            try{
                w.join();
            }catch(Exception e){ e.printStackTrace(); System.exit(0);}
        }

        //Then returns the largest number
        return findLargest(results, 0, results.length);
    }

    public void testRunA(){ //Does tests to compare sequential findLargest to the parallell one.
        int[] arraySizes = {2000, 20000, 200000, 2000000, 20000000, 200000000}; //2k, 20k, 200k, 2 mill, 20 mill, 200 mill,
        Random rand = new Random(420);
        int maxSizeInt = 1000000000; // 1 billion

        int amountOfTests = 5;

        //Sequential.
        System.out.println(" - - - - - - Find Largest - - - - - - ");
        System.out.print(" - - - - - - Sequential tests - - - - - - ");
        for(int i = 0; i < arraySizes.length; i++){
            System.out.print("\n" + arraySizes[i] + ": ");
            for(int j = 0; j < amountOfTests; j++) {
                //Fills the array with ints
                int[] intArray = new int[arraySizes[i]];
                for(int k = 0; k < intArray.length; k++){
                    intArray[k] = rand.nextInt(maxSizeInt);
                }

                //Starts the clock
                long t = System.nanoTime();
                findLargest(intArray, 0, intArray.length-1);
                double time = (System.nanoTime()-t)/1000000.0;
                System.out.print(time + "ms - ");

            }
        }
        System.out.print("\n - - - - - - Concurrent tests - - - - - - ");
        for(int i = 0; i < arraySizes.length; i++){
            System.out.print("\n" + arraySizes[i] + ": ");
            for(int j = 0; j < amountOfTests; j++) {
                //Fills the array with ints
                int[] intArray = new int[arraySizes[i]];
                for(int k = 0; k < intArray.length; k++){
                    intArray[k] = rand.nextInt(maxSizeInt);
                }

                //Starts the clock
                long t = System.nanoTime();
                runA(intArray);
                double time = (System.nanoTime()-t)/1000000.0;
                System.out.print(time + "ms - ");
            }
        }
    }

    private int findLargest(int[] intArray, int start, int end){
        int largest = intArray[start];
        for(int i = start+1; i<end; i++){
            if(intArray[i] > largest) largest = intArray[i];
        }
        return largest;
    }

    class FindLargestWorker extends Thread{
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
        public void run(){
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
        CyclicBarrier barrier = new CyclicBarrier(threadCount+1);
        int split = intArray.length / threadCount;

        for (int i = 0; i < threadCount; i++) {
            int start = split * i;
            int end;
            if (i == threadCount - 1) {
                end = intArray.length;
            } else {
                end = split * i + split;
            }
            new PartBWorker(i, intArray, start, end, allCount, sumCount,  mask, shift, barrier).start();
        }

        try{
            barrier.await(); //Waits until first stage is complete, then resets the barrier
            barrier.reset();
            barrier.await(); //Everything is done.
        }catch(Exception e){
            e.printStackTrace();
        }

        return sumCount;

    }

    public class PartBWorker extends Thread{
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

        PartBWorker(int threadNr, int[] intArray, int start, int end, int[][] allCount, int[] sumCount, int mask, int shift, CyclicBarrier barrier){
            this.threadNr = threadNr;
            this.intArray = intArray;
            this.start = start;
            this.end = end;
            this.allCount = allCount;
            this.sumCount = sumCount;
            this.mask = mask;
            this.shift = shift;
            this.barrier = barrier;

            count = new int[mask+1]; //Local array
        }

        @Override
        public void run(){
            //Adds up this threads array values to its local count.
            for(int i = start; i < end; i++){
                count[(intArray[i] >>> shift) & mask]++;
            }

            allCount[threadNr] = count;
            try{
                barrier.await();
            }catch(Exception e){
                System.out.println("Exception in thread " + threadNr);
                e.printStackTrace();
            }

            //Divides the new array into equal parts
            int threadCount = allCount.length;
            int split = count.length/threadCount;
            start = split * threadNr;

            if (threadNr == threadCount - 1) {
                end = count.length;
            }
            else {
                end = split * threadNr + split;
            }


            //Goes through the start -> end numbers in all of the bitArrays, and adds them all to Count.
            for(int thread = 0; thread < allCount.length; thread++){ //For each thread
                for(int i = start; i < end; i++){ //Go through the assigned values
                    sumCount[i] += allCount[thread][i];
                }
            }
            try{
                Thread.sleep(1); //I get a wierd exception here.
                barrier.await();
            } catch(Exception e){
                System.out.println("Exception in thread " + threadNr);
                e.printStackTrace();
            }
        }



    }



}


