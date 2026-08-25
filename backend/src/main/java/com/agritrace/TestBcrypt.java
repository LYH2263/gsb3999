package com.agritrace;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class TestBcrypt {
    public static void main(String[] args) {
        String hash = BCrypt.withDefaults().hashToString(10, "123456".toCharArray());
        System.out.println("TEST HASH: " + hash);
    }
}
