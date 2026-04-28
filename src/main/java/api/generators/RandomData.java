package api.generators;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.Random;

public class RandomData {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private RandomData() {
    }

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(3).toLowerCase()
                + RandomStringUtils.randomNumeric(3) + "%&$";
    }

    public static String getName() {
        return generateRandomWord(1, 20) + " " + generateRandomWord(1, 20);
    }

    // Генерирует одно случайное слово заданной длины
    private static String generateRandomWord(int minLen, int maxLen) {
        Random rand = new Random();
        int length = minLen + rand.nextInt(maxLen - minLen + 1);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = LETTERS.charAt(rand.nextInt(LETTERS.length()));
            sb.append(c);
        }
        return sb.toString();
    }

    public static double getDepositAmount() {
        double random = RandomUtils.nextDouble(1, 5000);
        return Math.round(random * 100) / 100.0;
    }

    public static double getTransferAmount() {
        double random = RandomUtils.nextDouble(1, 10000);
        return Math.round(random * 100) / 100.0;
    }
}
