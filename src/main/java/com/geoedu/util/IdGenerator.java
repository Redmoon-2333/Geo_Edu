package com.geoedu.util;

import java.util.Random;

public class IdGenerator {

    private static final Random RANDOM = new Random();

    public static String generateKnowledgeId(String grade, String chapter, int sequence) {
        return String.format("G%s-%s-%02d", grade, chapter, sequence);
    }

    public static String generateImageId() {
        long timestamp = System.currentTimeMillis();
        char randomLetter = (char) ('A' + RANDOM.nextInt(26));
        return String.format("IMG%d%c", timestamp, randomLetter);
    }

    public static String generateUserId(int sequence) {
        return String.format("U%03d", sequence);
    }
}