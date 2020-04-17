package org.ld.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {

    private static ThreadLocal<MessageDigest> messageDigestHolder = new ThreadLocal<>();

    private static final char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String getMD5Format(String data) {
        try {
            MessageDigest message = (MessageDigest)messageDigestHolder.get();
            if (message == null) {
                message = MessageDigest.getInstance("MD5");
                messageDigestHolder.set(message);
            }

            message.update(data.getBytes());
            byte[] b = message.digest();
            String digestHexStr = "";

            for(int i = 0; i < 16; ++i) {
                digestHexStr = digestHexStr + byteHEX(b[i]);
            }

            return digestHexStr;
        } catch (Exception var5) {
            throw new RuntimeException("MD5格式化时发生异常");
        }
    }

    public static String getMD5Format(byte[] data) {
        try {
            MessageDigest message = (MessageDigest)messageDigestHolder.get();
            if (message == null) {
                message = MessageDigest.getInstance("MD5");
                messageDigestHolder.set(message);
            }

            message.update(data);
            byte[] b = message.digest();
            String digestHexStr = "";

            for(int i = 0; i < 16; ++i) {
                digestHexStr = digestHexStr + byteHEX(b[i]);
            }

            return digestHexStr;
        } catch (Exception var5) {
            return null;
        }
    }

    private static String byteHEX(byte ib) {
        char[] ob = new char[]{hexDigits[ib >>> 4 & 15], hexDigits[ib & 15]};
        String s = new String(ob);
        return s;
    }

    static {
        try {
            MessageDigest message = MessageDigest.getInstance("MD5");
            messageDigestHolder.set(message);
        } catch (NoSuchAlgorithmException var1) {
            throw new RuntimeException("MD5格式化时发生异常");
        }
    }
}
