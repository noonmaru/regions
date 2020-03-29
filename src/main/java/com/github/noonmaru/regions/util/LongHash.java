package com.github.noonmaru.regions.util;

public final class LongHash {
    private LongHash() {
    }

    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - Integer.MIN_VALUE;
    }

    public static int msw(long l) {
        return (int) (l >> 32);
    }

    public static int lsw(long l) {
        return (int) (l & 0xFFFFFFFFL) + Integer.MIN_VALUE;
    }
}
