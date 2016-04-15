package Oblig3;

import java.util.concurrent.CyclicBarrier;

/**
 * Created by alexa on 15.04.2016.
 */
public class RadixParallell {
    //void radixSortPara(int[] a, int[] b, int maskLen, int shift)
    int threadCount = 4;
    RadixWorker[] workers;

    //Variables from the sequential radixSort method.
    int[] a;
    int[] b;
    int[] count;
    int maskLen;
    int shift;
    int mask;
    int acumVal;
    int n;
    int j;

    //Used by threads
    ThreadManager threadman;
    private int[] results; //A


    RadixParallell(){
        threadman = new ThreadManager();
        workers = new RadixWorker[threadCount];
        for(int i = 0; i < threadCount; i++){
            workers[i] = new RadixWorker(i, threadman);
            workers[i].start();
        }
    }



    int findLargest(int[] a){
        this.a = a;
        results = new int[threadCount];
        threadman.setFindLargest();
        waitFinished();
        return findLargest(results, 0, results.length);
    }



    void radixSortPara(int[] a, int[] b, int maskLen, int shift){
        this.a = a;
        this.b = b;
        this.maskLen = maskLen;
        this.shift = shift;
        this.mask = (1<<maskLen) -1;
        acumVal = 0;
        count = new int[mask+1];
        n = a.length;
    }

    void shutDownThreads(){
        threadman.setExit();
    }

    void waitFinished(){
        synchronized (threadman){
            try{
                while(!threadman.equals("FINISHED")){
                    threadman.wait();
                    System.out.println("NOT FINISHED");
                }
            }catch(Exception e){
                System.out.println("Exception in findLargest");
                e.printStackTrace();
            }
        }
        threadman.setWait();
    }


    /*private void runThreads(){
        // System.out.println(" radixSort maskLen:"+maskLen+", shift :"+shift);

        //Check if we have any threads running, if not stop them

// b) count=the frequency of each radix value in a
        for (int i = 0; i < n; i++) {
            count[(a[i]>>> shift) & mask]++;
        }
// c) Add up in 'count' - accumulated values, i.e pointers
        for (int i = 0; i <= mask; i++) {
            j = count[i];
            count[i] = acumVal;
            acumVal += j;
        }
// d) move numbers in sorted order a to b
        for (int i = 0; i < n; i++) {
            b[count[(a[i]>>>shift) & mask]++] = a[i];
        }


    }*/

    //A needs to be done first, alone.



    class RadixWorker extends Thread{
        int threadNr;
        CyclicBarrier barrier;
        ThreadManager threadman;


        RadixWorker(int threadNr, ThreadManager threadman){
            this.threadNr = threadNr;
            this.threadman = threadman;
            barrier = new CyclicBarrier(threadCount);
        }

        public void run(){
            while(!(threadman.equals("EXIT"))){
                try{
                    synchronized (threadman){
                        //System.out.println(threadNr + " waiting");
                        threadman.wait(1000); //I need some polling. This shouldnt affect speeds though.
                        if(threadman.equals("WAIT")){
                            System.exit(1);
                        }
                    }

                    if(threadman.equals("FINDLARGEST")){
                        runA();
                    }

                    if(threadman.equals("SORT")){
                        System.out.println(threadNr + " running SORT");
                    }


                }catch(Exception e){
                    System.out.println("Exception in thread " + threadNr);
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            System.out.println("EXITING WITH threadman = " + threadman.state);
            //runA();

            //runB();
            //runC();
            //runD();
            //Shutdown threads if not used again within 1 second.
            //Removes the need for System.exit()
        }

        void runA(){
            int split = a.length / threadCount;

            //Splits up the array
            int start = split * threadNr;
            int end;
            if (threadNr == threadCount - 1) {
                end = a.length;
            } else {
                end = split * threadNr + split;
            }
            System.out.println("THREAD: " + threadNr + " - START: " + start + " - END: " + end);

            results[threadNr] = findLargest(a, start, end);
            threadman.setFinished();

            //Then returns the largest number
        }
    }


    private int findLargest(int[] intArray, int start, int end) {
        int largest = intArray[start];
        for (int i = start + 1; i < end; i++) {
            if (intArray[i] > largest) largest = intArray[i];
        }
        return largest;
    }


}

class ThreadManager{
    String state = "";


    ThreadManager(){
        setWait();
    }

    void setWait(){
        state = "WAIT";
    }

    synchronized void setExit(){
        state = "EXIT";
        notifyAll();
    }

    synchronized void setSort(){
        state = "SORT";
        notifyAll();
    }

    synchronized void setFindLargest(){
        state = "FINDLARGEST";
        notifyAll();
    }

    synchronized void setFinished(){
        state = "FINISHED";
        notifyAll();
    }


    boolean equals(String s){
        return state.equals(s);
    }
}
