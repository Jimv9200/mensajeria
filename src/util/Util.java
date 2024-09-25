package util;

import java.io.*;
import java.security.*;

import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;



public class Util {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String byteArrayToHexString(byte[] bytes, String separator){
        String result = "";
        for (int i=0; i<bytes.length; i++) {
            result += String.format("%02x", bytes[i]) + separator;
        }
        return result.toString();
    }


    public static byte[] objectToByteArray(Object o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(o);
        out.close();
        byte[] buffer = baos.toByteArray();
        return buffer;
    }

    public static Object byteArrayToObject(byte[] b) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
        Object o = in.readObject();
        in.close();
        return o;
    }





}
