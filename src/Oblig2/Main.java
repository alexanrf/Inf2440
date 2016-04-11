package Oblig2;

import java.util.*;
import java.util.concurrent.CyclicBarrier;

public class Main {

    public static void main(String[] args) {
        int[] maxPrimeArray = {200000, 2000000, 20000000, 2000000000};

        //Sekvensielle tester
        for(int i : maxPrimeArray){
            RunSekv runSekv = new RunSekv();
            runSekv.run(i);
            System.out.println("\n--- --- ---\n");
        }



        //Fungerer ikke
        /*
        Oblig2.RunPara runPara = new Oblig2.RunPara(maxPrime);
        runPara.run();
*/
    }

}

class RunSekv{
    void run(int maxPrime){
        EratosthenesSil sil = new EratosthenesSil(maxPrime);

        long t = System.nanoTime(); // start klokke
        sil.generatePrimesByEratosthenesSeq(); //Oppretter alle primtallene


        double tid = (System.nanoTime()-t)/1000000.0; //Teller opp tiden brukt på å generere primtall
        System.out.println("Genererte alle primtall under < " + maxPrime + " på " +  tid + " millisec");
        System.out.println("Antall primtall: " + sil.countAllPrime());

        long factorize = (long)maxPrime*maxPrime - 100; //Regner ut av tallene som skal faktoriseres

        t = System.nanoTime(); // start klokke igjen
        for(int i = 0; i < 100; i++){
            ArrayList<Long> list = sil.factorize(factorize + i); //Faktoriserer og skriver ut tallene
            prettyPrintFactors(list, factorize + i);
        }

        tid = (System.nanoTime()-t)/1000000.0; //Skriver ut resultat etter sekvensiell kjøring
        System.out.println("100 faktoriseringer med utskrift beregnet på: " + tid + "\n" + tid/100 + "ms. per faktorisering");
    }



    void prettyPrintFactors(ArrayList<Long> list, long num){ //Skriver ut faktoriseringsresultatet pent.
        System.out.print(num + " = ");
        for(int i = 0; i<list.size(); i++){
            if(i == list.size()-1){
                System.out.print(list.get(i) +"\n");
            }
            else{
                System.out.print(list.get(i) + " * ");
            }
        }
    }
}


class RunPara{ //Fungerer ikke
    EratosthenesSil sil;
    int maxPrime;

    RunPara(int maxPrime){
        this.maxPrime = maxPrime;
        sil = new EratosthenesSil(maxPrime);
    }

    void run(){
        double sqrtMax = Math.sqrt(maxPrime);
        sil = new EratosthenesSil(maxPrime);
        sil.generatePrimesParaStartup(sqrtMax); //Lager de første sqrt(n) primtallene
        Integer[] startUpPrimes = getStartupPrimes(); //Setter de første primtallene inn i et array

        int antKjerner = Runtime.getRuntime().availableProcessors();

        CyclicBarrier barrier = new CyclicBarrier(antKjerner + 1);

        System.out.println("StartupPrimes length = " + startUpPrimes.length);
        /* Deler opp de første Sqrt(maxNum) primtallene til hver sin tråd */
        for(int i = 0; i<antKjerner; i++){
            /* Regner ut av hvilke primtall hver tråd skal ta */
            int startPrime = i * (startUpPrimes.length / antKjerner);
            int endPrime;
            if(i == antKjerner-1){
                endPrime = startUpPrimes.length-1;
            }
            else{
                endPrime = (i+1) * (startUpPrimes.length / antKjerner);
            }
            //Starter en tråd.
            new SieveThreadWorker(startPrime, endPrime, sil, startUpPrimes, maxPrime, barrier).start();
        }

        try{
            barrier.await();
        }catch(Exception e){System.out.println(e);}

        System.out.println(sil.countAllPrime());




        //System.out.println("Last Prime: " + startUpPrimes[startUpPrimes.length-1]);



    }

    Integer[] getStartupPrimes(){ //Hente ut de første sqrt(maxNum) primtallene for bruk i den parallell løsningen
        ArrayList<Integer> list = new ArrayList<Integer>();

        int i = 2;
        int prime = 0;
        int sqrtMax = (int)Math.sqrt(maxPrime +1);
        while((prime = sil.nextPrime(prime + 1)) < sqrtMax){
            list.add(prime);
        }
        Integer[] startUpPrimes = new Integer[list.size()];
        list.toArray(startUpPrimes);
        return startUpPrimes;
    }
}

/**
 * Implements the bitArray of length 'maxNum' [0..maxNum/16 ]
 *   1 - true (is prime number)
 *   0 - false
 *  can be used up to 2 G Bits (integer range)
 *  16 numbers, i.e. 8 odd numbers per byte (bitArr[0] represents 1,3,5,7,9,11,13,15 )
 *
 */
class EratosthenesSil {
    byte [] bitArr ;           // bitArr[0] represents the 8 integers:  1,3,5,...,15, and so on
    int  maxNum;               // all primes in this bit-array is <= maxNum
    final  int [] bitMask = {1,2,4,8,16,32,64,128};  // kanskje trenger du denne - 0 starter fra starten.
    final  int [] bitMask2 ={255-1,255-2,255-4,255-8,255-16,255-32,255-64, 255-128}; // kanskje trenger du denne - 0 starter fra slutten
    ArrayList<Long> primes = new ArrayList<Long>();



    EratosthenesSil (int maxNum) {
        this.maxNum = maxNum;
        bitArr = new byte [(maxNum/16)+1]; // 16 pga 8 tall i en byte, og kun oddetall
        setAllPrime();
    } // end konstruktor ErathostenesSil

    void setAllPrime() {
        for (int i = 0; i < bitArr.length; i++) {
            bitArr[i] = (byte)255; // bitarray[0] = 11111111
        }

    } //Sets all bits to 1

    void crossOut(int i) {
        if(i % 2 == 0){
            return;
        }

        int bitIndex = (i-1) / 2;
        int arrayIndex = bitIndex / 8; //bitArr[arrayIndex] - Where the number is stored
        bitIndex = bitIndex % 8; //which bit in bitArr[arrayIndex] corresponds to the number

        bitArr[arrayIndex] = (byte) (bitArr[arrayIndex] & bitMask2[bitIndex]); //Uses the correct mask over the array index to single out our value.
    } //Crosses out the integer (sets it to 0) aka not a prime


    boolean isPrime (int i) { //isCrossedOut()
        if(i == 2){
            return true;
        }
        if(i % 2 == 0 || i == 1){
            return false;
        }

        //Finn position i arrayet
        int bitIndex = (i-1) / 2;
        int arrayIndex = bitIndex / 8;
        bitIndex = bitIndex % 8;
        //System.out.println("isPrime("+ i +")");

        if((bitArr[arrayIndex] & bitMask[bitIndex]) == 0){
            return false;
        }
        return true;
    } //Checks if a value is a prime

    void printBinary(){ //Går gjennom bitArr og skriver ut 0 eller 1 - Brukt i debugging sammenheng
        for(int i = 0; i < bitArr.length; i++){
            for(int y = 0; y < 8; y++){
                if((bitArr[i] & bitMask[y]) == 0){
                    System.out.print("1");
                }
                else{
                    System.out.print("0");
                }
            }
            System.out.println("");
        }
    }


    ArrayList<Long> factorize (long num) { //Sekvensiell faktorisering
        ArrayList <Long> fakt = new ArrayList <Long>(); //Arraylist som holder styr på faktorene våre.

        long prime = 2;
        while(prime != -1){ // != -1 fordi nextPrime returnerer -1 om det ikke er flere primtall
            if(num % prime == 0){ // == 0, da er det en faktor.
                fakt.add(prime);
                num = num / prime;
            }
            else{
                prime = nextPrime((int)prime+1); //Ellers, gå til neste primtall
            }
        }
        if(num != 1){
            fakt.add(num);
        }

        return fakt;
    }




    int nextPrime(int input) { //Ikke særlig optimalt dette. Hadde problemer med å iterere over binærArrayet direkte
        if(input < 2)
            return 2;
        if(input % 2 == 0){ //Unødvendig å sjekke partall.
            input++;
        }
        while(input < maxNum){
            if(isPrime(input)){
                return input;
            }
            input += 2;
        }
        return -1;
    }

    void printAllPrimes(){ //Skriver ut alle primtall
        int i = 0;
        while((i = nextPrime(i+1)) != -1){
            System.out.println(i);
        }
    }

    int countAllPrime() {
        int i = 0;
        int count = 0;
        while((i = nextPrime(i+1)) != -1){
            count++;
        }
        return count;
    }

    void generatePrimesByEratosthenesSeq() {
        crossOut(1);      // 1 er ikke et primtall
        long prime = 0;
        int sqrtMaxNum = (int)Math.sqrt(maxNum)+1; //About 5-10% faster with an int than a double based on observations.

        while((prime = nextPrime((int)prime+1)) <= sqrtMaxNum){ //SqrtMaxNum fordi
            long primeTemp = prime*prime;

            while(primeTemp < maxNum){
                crossOut((int)primeTemp);
                primeTemp += prime*2;
            }
        }
    } // end generatePrimesByEratosthenes


    void generatePrimesParaStartup(double maxNum){
        crossOut(1);
        long prime = 0;
        int sqrtMaxNum = (int)Math.sqrt(maxNum)+1; //About 5-10% faster with an int than a double based on observations.

        while((prime = nextPrime((int)prime+1)) <= sqrtMaxNum){
            long primeTemp = prime*prime;

            while(primeTemp < maxNum){
                crossOut((int)primeTemp);
                primeTemp += prime*2;
            }
        }


    }



}

class SieveThreadWorker extends Thread{
    int startPrime;
    int endPrime;
    EratosthenesSil sil;
    Integer[] firstPrimes; //
    int maxNum;
    CyclicBarrier barrier;

    SieveThreadWorker(int startPrime, int endPrime, EratosthenesSil sil, Integer[] firstPrimes, int maxNum, CyclicBarrier barrier){
        this.startPrime = startPrime;
        this.endPrime = endPrime;
        this.sil = sil;
        this.firstPrimes = firstPrimes;
        this.barrier = barrier;
        this.maxNum = maxNum;
    }

    public void run(){ // Ikke en god løsning.
        int i = startPrime;
        while(i < endPrime){
            long primeTemp = firstPrimes[i] * firstPrimes[i];
            while(primeTemp < maxNum){
                sil.crossOut((int) primeTemp);
                primeTemp += primeTemp*2;
            }
            i++;
        }
        try{
            barrier.await();
        }
        catch(Exception e){System.out.println(e);}
/*
    Tanken bak denne implementasjonen var å finne de første Sqrt(maxNumber) primtallene, for så å dele de opp likt mellom trådene.
    Så gjøre det samme som i den sekvensielle løsningen, ved å gange primtall N*N, og krysse ut den, og alle N*N + (2*N) primtall i resten av byteArrayet vårt.
    Antall primtall som telles opp er feil, så enten så er denne fremgangsmåten feil. Eller så er det problemer med synkronisering på byteArrayet.
    (Tviler på det mtp at det er samme svar hver gang = 999970335)

 */

    }
}


class FactorizationThreadWorker extends Thread{
    int startPrime;
    int endPrime;
    EratosthenesSil sil;

    FactorizationThreadWorker(int startPrime, int endPrime, EratosthenesSil sil){
        /*

         */
    }


    public void run(){

    }
}


 // end class Oblig2.EratosthenesSil
