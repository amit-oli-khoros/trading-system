package org.amit.common;

import java.util.concurrent.atomic.AtomicLong;

public class OrderIdGenerator {
    private static final AtomicLong counter = new AtomicLong(1);

    private OrderIdGenerator() {
    }

    public static long nextId() {
        return counter.getAndIncrement();
    }
}
