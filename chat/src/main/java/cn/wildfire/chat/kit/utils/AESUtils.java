package cn.wildfire.chat.kit.utils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

    /**
     * AES密钥长度，用来初始化
     */
    private static final int AES_KEY_SIZE = 128;
    /**
     * AES算法
     */
    private static final String ALGORITHM_AES = "AES";
    //初始向量（偏移）
    public static final String iv = "7983B5439EF75A69";   //AES 为16bytes. DES 为8bytes

    //私钥  （密钥）
    private static final String key = "7983b5439ef75a69";   //AES固定格式为128/192/256 bits.即：16/24/32bytes。DES固定格式为128bits，即8bytes。
    private static final String UTF_8 = "utf-8";

    // iv同C语言中iv
    private static byte ivBytes[] = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06,
            0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};

    /**
     * 加密
     *
     * @param data 加密前的字符串
     * @return 加密后的字节数组
     */
    public static byte[] encryptNopadding(String data) {
        try {

            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();

            //判断待加密的字节数组的长度，在此长度基础上扩展一个字节数组的长度为16的倍数
            byte[] dataBytes = data.getBytes();
            int plaintextLength = dataBytes.length;
            if (plaintextLength % blockSize != 0) {
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }

            //创建需新的待加密的字节数组，将上面的字节数组复制进来，多余的补0
            byte[] plaintext = new byte[plaintextLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

            //加密后的字节数组
            return cipher.doFinal(plaintext);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 加密
     *
     * @param data 加密前的字符串
     * @return 加密后的字节数组
     */
    @SuppressWarnings("all")
    public static byte[] encrypt(String data, String k) {
        try {
            // keyBytes32个字节，256位，
            byte[] keyBytes = HashUtil.getKey(k, 16).getBytes(UTF_8);
            SecretKeySpec key = new SecretKeySpec(keyBytes, ALGORITHM_AES);
            // 创建密码器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final IvParameterSpec iv = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.ENCRYPT_MODE, key, iv);// 初始化
            byte[] encrypt = cipher.doFinal(data.getBytes());
            return encrypt; // 加密
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("all")
    public static String decrypt(byte[] encrypt, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] keyBytes = HashUtil.getKey(key, 16).getBytes(UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, ALGORITHM_AES);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            //解密后的字节数组
            byte[] original = cipher.doFinal(encrypt);
            return new String(original, UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param encrypted1 解密前的字节数组
     * @return 解密后的字符串
     */
    public static String decrypt(byte[] encrypted1) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

            //解密后的字节数组
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("all")
    public static byte[] getKey(String seed) throws Exception {
        String key = HashUtil.getKey(seed, "8");
        SecureRandom secureRandom = new SecureRandom(key.getBytes(UTF_8));
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM_AES);
        keyGenerator.init(AES_KEY_SIZE, secureRandom);
        SecretKey secretKey = keyGenerator.generateKey();
        return secretKey.getEncoded();
    }
}
