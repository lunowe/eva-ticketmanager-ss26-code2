package IDGenerator.PrimeNumberGenerator;

import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;

public class PrimeNumberGenerator {

    private static final int PRIMALITY_CERTAINTY = 30;

    private final long lowerLimit;
    private final long upperLimit;

    public PrimeNumberGenerator(long lowerLimit, long upperLimit){
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    public boolean isPrime(long number) {
        if (number < 2) return false;
        if (number < 4) return true;
        if ((number & 1L) == 0) return false;
        return BigInteger.valueOf(number).isProbablePrime(PRIMALITY_CERTAINTY);
    }

    public long getRandomPrimeNumberInRange(){
        long possiblePrimeNumber = -1;
        long randomTries = 0;
        while(!isPrime(possiblePrimeNumber) && randomTries < (upperLimit - lowerLimit)) {
            possiblePrimeNumber = ThreadLocalRandom.current().nextLong(lowerLimit, upperLimit + 1);
            randomTries++;
        }
        return possiblePrimeNumber;
    }
}
