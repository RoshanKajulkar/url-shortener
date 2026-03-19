package com.example.urlshortener.util;

public class Base62Util {

    private static final String CHARSET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long num) {
        StringBuilder sb = new StringBuilder();

        while (num > 0) {
            int rem = (int) (num % 62);
            sb.append(CHARSET.charAt(rem));
            num = num / 62;
        }

        return sb.reverse().toString();
    }
}