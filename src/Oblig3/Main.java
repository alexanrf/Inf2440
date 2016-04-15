package Oblig3;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by alexa on 09.04.2016.
 */
public class Main {
    public static void main(String[] args){
        System.out.println("HEY");
        /*
        int[] arr = new int[100000000]; //1 mill
        Random ran = new Random(5);
        for(int i = 0; i < arr.length; i++){
            arr[i] = ran.nextInt(100000000);//1 mill
        }

        RadixPara radixPara = new RadixPara();
        radixPara.testRunA();

*/

/* Tests our MainRadix to vanillaRadix
        //Seeds: 5/16, god spredning + 3 7
        for(int j = 0; j < 10; j++){
            int[] arr = new int[100000000]; //10 mill
            Random ran = new Random(5);
            for(int i = 0; i < arr.length; i++){
                arr[i] = ran.nextInt(1000000000);
            }
            //System.out.println(Arrays.toString(arr));

            Radix radix = new Radix();
            //System.out.println(Arrays.toString(arr));
            System.out.println("Parallell");
            radix.testSort(radix.radixMulti(arr));
            System.out.println("Sequential");

            VanillaRadix vRadix = new VanillaRadix();
            vRadix.radixMulti(arr);
        }
        */



/*

        int[] arr = new int[100]; //10 mill
        Radix radix = new Radix();
        Random ran = new Random();
        for(int i = 0; i < arr.length; i++){
            arr[i] = ran.nextInt(100000); //16
        }
        System.out.println("Input: " + Arrays.toString(arr));
        arr = radix.radixMulti(arr);

        System.out.println(Arrays.toString(arr));

*/


        /* Slett om C fungerer
        RadixPara radixPara = new RadixPara();
        radixPara.runC();
        */

/*
        int[] arr = new int[100]; //10 mill
        Random ran = new Random();
        for(int i = 0; i < arr.length; i++){
            arr[i] = ran.nextInt(100); //16
        }
        System.out.println(Arrays.toString(arr));
        RadixParallell radixPara = new RadixParallell();
        System.out.println("LARGEST: " + radixPara.findLargest(arr));

        radixPara.shutDownThreads();
        //System.exit(1);
*/

        int[] arr = new int[10000]; //10 mill
        Radix radix = new Radix();
        Random ran = new Random();
        for(int i = 0; i < arr.length; i++){
            arr[i] = ran.nextInt(100000); //16
        }
        System.out.println("Input: " + Arrays.toString(arr));
        arr = radix.radixMulti(arr);


    }


}

class Radix {
    /**
     * N.B. Sorterer a[] stigende – antar at: 0 ≤ a[i]) < 232
     */
    // viktig konstant
    final static int NUM_BIT = 7; // eller 6,8,9,10..

    int[] radixMulti(int[] a) {
        long tt = System.nanoTime();
        // 1-5 digit radixSort of : a[]
        int max = a[0];
        int numBit = 2;
        int numDigits;
        int n = a.length;
        int[] bit;

// a) finn max verdi i a[]
        for (int i = 1; i < n; i++)
            if (a[i] > max) max = a[i];

        while (max >= (1L << numBit)){ //Stopper når tallet er mindre enn 2^numBit for å finne hvor mange bit det er.
            numBit++;
        }
        /* DEBUG OUTPUT
        System.out.println("Max: " + max+ "\tStørste tallet i arrayet");
        System.out.println("numBit:" + numBit + "\tAntall bits i største tall");
        System.out.println("NUM_BIT:" + NUM_BIT + "\tStatic verdi"); //NUM_BIT = antall bit i en byte?
*/

        // bestem antall bit i numBits sifre
        numDigits = Math.max(1, numBit / NUM_BIT);
        bit = new int[numDigits];
        int rest = numBit % NUM_BIT, sum = 0;
        /* DEBUG OUTPUT
        System.out.println("numDigits: " + numDigits + " ---- Math.max(1, "+numBit +"/"+NUM_BIT+") \t antall ");
        System.out.println("Rest: " + rest);
*/
        // fordel bitene vi skal sortere paa jevnt
        for (int i = 0; i < bit.length; i++) {
            bit[i] = numBit / numDigits;
            if (rest-- > 0){
                bit[i]++;
            }
        }
        //System.out.println(Arrays.toString(bit));


        int[] t = a, b = new int[n];
        for (int i = 0; i < bit.length; i++) {
            /* DEBUG OUTPUT
            System.out.println("radixSort(a, b, bit[i], sum");
            System.out.println("radixSort("+a.length+", " + b.length + ", bit["+i+"], "+sum);
            */
            radixSort(a, b, bit[i], sum); // i-te siffer fra a[] til b[]
            sum += bit[i];
            // swap arrays (pointers only)
            t = a;
            a = b;
            b = t;
        }

        if ((bit.length & 1) != 0) {
            // et odde antall sifre, kopier innhold tilbake til original a[] (nå b)
            System.arraycopy(a, 0, b, 0, a.length);
        }
        double tid = (System.nanoTime() - tt) / 1000000.0;
        System.out.println("\nSorterte " + n + " tall paa:" + tid + "millisek.");
        testSort(a);
        return a;
    } // end radix2

    /**
     * Sort a[] on one digit ; number of bits = maskLen, shiftet up 'shift' bits
     */
    void radixSort(int[] a, int[] b, int maskLen, int shift) {
        // System.out.println(" radixSort maskLen:"+maskLen+", shift :"+shift);
        int acumVal = 0;
        int j;

        //Lager et "bit" array like langt som nærmeste 2^x
        int mask = (1 << maskLen) - 1;
        int[] count = new int[mask + 1];
        System.out.println("radixSort.count.length: " + count.length + " ShiftValue: " + shift + " Mask: " + mask);



// b) count=the frequency of each radix value in a
        for (int i = 0; i < a.length; i++) { //For each value in a array, increment the corresponding index in the array
            count[(a[i] >>> shift) & mask]++;
            //System.out.println((a[i] >>> shift & mask));
        }
        //System.out.println("B: " + Arrays.toString(count));


/* Parallell implementasjon
        RadixPara radixPara = new RadixPara();
        count = radixPara.runB(a, mask, shift);
*/

// c) Add up in 'count' - accumulated values, i.e pointers

        for (int i = 0; i <= mask; i++) {
            j = count[i];
            count[i] = acumVal;
            acumVal += j;
        }
        //System.out.println("C: " + Arrays.toString(count));


        /*Parallell implementasjon C
        RadixPara radixPara = new RadixPara();
        radixPara.runC(count);
*/
        //count, a, b, shift, mask
        /*
        System.out.println("count" + Arrays.toString(count));
        System.out.println("a: " + Arrays.toString(a));
        System.out.println("B: " + Arrays.toString(b));
        System.out.println("shift: " + shift);
        System.out.println("mask: " + mask);

        System.out.println("Pre-D: " + Arrays.toString(b));
        */
// d) move numbers in sorted order a to b
/*
        for (int i = 0; i < a.length; i++) {
            int tempInt = count[(a[i] >>> shift) & mask]++;
            b[tempInt] = a[i];
            System.out.println("b["+i+"] - " + "a["+tempInt+"]");
        }
        //System.out.println("D: " + Arrays.toString(b));
*/

        RadixPara radixPara = new RadixPara();
        radixPara.runD(a, b, count, shift, mask);

    }// end radixSort

    //D trenger: a, count, mask, b,

    void testSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                System.out.println("SorteringsFEIL på plass: " +
                        i + " a[" + i + "]:" + a[i] + " > a[" + (i + 1) + "]:" + a[i + 1]);
                //return;
            }
        }
    }// end simple sorteingstest
}


/*
Forelesning:
c) Legger sammen verdiene  i count[] til 'pekere' til b[]
d) flytt tallene fra a[] til b[]

c)
Gå gjennom arrayet og telle opp hvor mange tall det er i sin del.
Hver tråd får da vite hvor de må starte
Parallellt gå gjennom sin del og sett inn i arrayet mens man inkrementerer count

d)











 */