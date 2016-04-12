package Oblig3;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by alexa on 09.04.2016.
 */
public class Main {
    public static void main(String[] args){
        /*
        int[] arr = new int[100000000]; //1 mill
        Random ran = new Random(5);
        for(int i = 0; i < arr.length; i++){
            arr[i] = ran.nextInt(100000000);//1 mill
        }

        RadixPara radixPara = new RadixPara();
        radixPara.testRunA();

*/


        //Seeds: 5/16, god spredning + 3 7
        int[] arr = new int[300000000];
        Random ran = new Random(5);
        for(int i = 0; i < arr.length; i++){
            arr[i] = ran.nextInt(1000000000);
        }
        //System.out.println(Arrays.toString(arr));

        Radix radix = new Radix();
        //System.out.println(Arrays.toString(arr));
        radix.testSort(radix.radixMulti(arr));

        VanillaRadix vRadix = new VanillaRadix();
        vRadix.radixMulti(arr);




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
        System.out.println("Max: " + max+ "\tStørste tallet i arrayet");
        System.out.println("numBit:" + numBit + "\tAntall bits i største tall");
        System.out.println("NUM_BIT:" + NUM_BIT + "\tStatic verdi"); //NUM_BIT = antall bit i en byte?

        // bestem antall bit i numBits sifre
        numDigits = Math.max(1, numBit / NUM_BIT);
        System.out.println("numDigits: " + numDigits + " ---- Math.max(1, "+numBit +"/"+NUM_BIT+") \t antall ");
        bit = new int[numDigits];
        int rest = numBit % NUM_BIT, sum = 0;
        System.out.println("Rest: " + rest);

        // fordel bitene vi skal sortere paa jevnt
        for (int i = 0; i < bit.length; i++) {
            bit[i] = numBit / numDigits;
            if (rest-- > 0){
                bit[i]++;
            }
        }
        System.out.println(Arrays.toString(bit));


        int[] t = a, b = new int[n];
        for (int i = 0; i < bit.length; i++) {
            System.out.println("radixSort(a, b, bit[i], sum");
            System.out.println("radixSort("+a.length+", " + b.length + ", bit["+i+"], "+sum);
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
        int arrayLength = a.length;
        int j;

        //Lager et "bit" array like langt som nærmeste 2^x
        int mask = (1 << maskLen) - 1;
        int[] count = new int[mask + 1];
        System.out.println("radixSort.count.length: " + count.length + " ShiftValue: " + shift + " Mask: " + mask);



// b) count=the frequency of each radix value in a
/*
        for (int i = 0; i < arrayLength; i++) { //For each value in a array, increment the corresponding index in the array
            count[(a[i] >>> shift) & mask]++;
            //System.out.println((a[i] >>> shift & mask));
        }
        System.out.println(Arrays.toString(count));
*/
        RadixPara radixPara = new RadixPara();
        count = radixPara.runB(a, mask, shift);


// c) Add up in 'count' - accumulated values, i.e pointers
        for (int i = 0; i <= mask; i++) {
            j = count[i];
            count[i] = acumVal;
            acumVal += j;
        }


// d) move numbers in sorted order a to b
        for (int i = 0; i < arrayLength; i++) {
            b[count[(a[i] >>> shift) & mask]++] = a[i];
        }
    }// end radixSort

    void testSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i] > a[i + 1]) {
                System.out.println("SorteringsFEIL på plass: " +
                        i + " a[" + i + "]:" + a[i] + " > a[" + (i + 1) + "]:" + a[i + 1]);
                return;
            }
        }
    }// end simple sorteingstest
}