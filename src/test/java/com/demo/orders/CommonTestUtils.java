package com.demo.orders;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public class CommonTestUtils {

    public static boolean waitForResultWithTimeout(BooleanSupplier condition, Duration timeout, Duration retryInterval) {
        long start = System.currentTimeMillis();
        long max = start + timeout.toMillis();
        while (max >= System.currentTimeMillis()) {
            if (condition.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(retryInterval.toMillis());
            } catch (InterruptedException e) {
                //ignored
            }
        }
        return false;
    }
}
