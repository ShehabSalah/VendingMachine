package com.flapkap.challenge.utils;

import java.util.Arrays;
import java.util.List;

public class AllowedPrices {
    private static final List<Integer> ALLOWED_PRICES = Arrays.asList(5, 10, 20, 50, 100);

    public static boolean isAllowedPrice(int amount) {
        return ALLOWED_PRICES.contains(amount);
    }

}
