package com.home.young.myPassword.application;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    private static final int NUMBER_0XFF = 0xFF;
    private static final int NUMBER_0X10 = 0x10;

    public static  String getMD5(String value) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(value.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & NUMBER_0XFF) < NUMBER_0X10) hex.append("0");
            hex.append(Integer.toHexString(b & NUMBER_0XFF));
        }
        return hex.toString();
    }
}
