package com.theladders;

public class Util {
    private Util() {}
    public static String nullIfEmpty(String str) {
        return str == null || str.isBlank() ? null : str;
    }
}
