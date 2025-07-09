package org.amit.common;

import java.util.concurrent.atomic.AtomicLong;

public class TradeIdGenerator {
    private static final AtomicLong counter = new AtomicLong(1);

    private TradeIdGenerator() {

    }

    public static long nextId() {
        return counter.getAndIncrement();
    }
}
