package com.xl.traffic.gateway.core.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 3DES加密方法
 *
 * @author: xl
 * @date: 2021/7/13
 **/
public class DESCipher {

    private static final Logger logger = LoggerFactory.getLogger(DESCipher.class);

    public static final String key = "qwejda!@#dasd235412faced";




    /**
     * 加密
     *
     * @param plainByte 明文
     * @return 密文
     */
    public static byte[] encrypt(byte[] plainByte, String key) {

        byte[] cipherByte = null;
        try {
            DESedeKeySpec dks = new DESedeKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory
                    .getInstance("DESede");
            SecretKey securekey = keyFactory.generateSecret(dks);

            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, securekey);
            cipherByte = cipher.doFinal(plainByte);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return cipherByte;
    }

    /**
     * 解密
     *
     * @param cipherByte 密文
     * @return 明文
     */
    public static byte[] decrypt(byte[] cipherByte, String key) {

        byte[] decryptByte = null;
        try {
            DESedeKeySpec dks = new DESedeKeySpec(key.getBytes("UTF-8"));
            SecretKeyFactory keyFactory = SecretKeyFactory
                    .getInstance("DESede");
            SecretKey securekey = keyFactory.generateSecret(dks);

            Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, securekey);
            decryptByte = cipher.doFinal(cipherByte);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return decryptByte;
    }

    public static void main(String[] args) throws Exception {
        String key = "qwejda!@#dasd235412faced";
        key = "335166913351669133516691";
        byte[] b1 = {1, 2, 3};

        DESCipher method = new DESCipher();
        System.out.println("原始数据[" + b1.length + "]：");
        for (byte b : b1)
            System.out.print(b + " ");
        System.out.println();

        byte[] encode = method.encrypt(b1, key);
        System.out.println("加密数据[" + encode.length + "]：");
        for (byte b : encode)
            System.out.print(b + " ");
        System.out.println();
        byte[] decode = method.decrypt(encode, key);
        System.out.println("解密数据[" + decode.length + "]：");
        for (byte b : decode)
            System.out.print(b + " ");
        System.out.println();

    }


}
