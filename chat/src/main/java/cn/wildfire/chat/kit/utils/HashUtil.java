package cn.wildfire.chat.kit.utils;

import android.util.Log;

import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import cn.wildfirechat.chat.BuildConfig;

public class HashUtil {
    private static final String UTF_8 = "utf-8";
    //可以将密钥保存在本地用C需要写的代码中，编程成so包，防止被太容易查看到
    //这里介绍一种通过超长密码字符串作为密码表的例子,密钥可以在代码中分散保存使用的时候拼接后使用
    private static final String HASH = "7b81BWZ5wFPu59ufx23C70d8fDdr9850U5l4555KQ18c6OMTtAo2gH3E590q51l9gBs45xo6N547445wHoqCxUH9Hd0eO9cx33208Q43B2hRA3600V6hA860sw0e8RiR3Wv1M5601ttGivFk432xL5IZ73885J4CnB2g9glJ697454W45P2X9310893LpQe39K921u17rSvCRs454r3Rn6N61B3VE2A27d362OV8C8R1x37Bcv4lD1t5434TV5w202WWU1c4mI5grN97H12KW4Epv2v3R904jzj23qEqh532882n3S35vz993P0fQ38iXo8xK8A29tp27Aw3D1JuneTh1231y7k5uN597p158Vu8OMM1a0H2T9bn991161HX5MP1680HNgI9a1600Z78K6gujN8QmxsN2p35b10n458eB613W1RJgI3Z4j3344q9G3ZmH083379DT9e9JbGGR0bYyCbJQ1WD8yl4956I33xM762496pqYrWoTxZ3ifLI40ifRbP60gsX3183YI83AygoIYG8x462N7srIa6peN0Z55RG68175M7P561c0KD655p11o1363BwLVT0b032M6321mS81c5h5ZbSIkH3G83y9p431bR9JL9I8B55h8D6484Wz7Ai65Hi04qeYcvK4lIz6Q76sYPO45y13X60U4VFqH18rJ8fZdF5rBGt52N02G6N068bVy2c203I3GiDeg36ED0lUo34";

    public static final String FULL = getFull();

    public static long hash64A(byte[] data, int seed) {
        return hash64A(ByteBuffer.wrap(data), seed);
    }

    public static long hash64A(ByteBuffer buf, int seed) {
        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            // for big-endian version, do this first:
            // finish.position(8-buf.remaining());
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
    }

    public static long hash(byte[] key) {
        return hash64A(key, 0x1234ABCD);
    }

    public static long hash(String key) {
        return hash(encode(key));
    }

    private static byte[] encode(String data) {
        try {
            return data.getBytes(UTF_8);
        } catch (Exception e) {
            return data.getBytes();
        }
    }

    /**
     * 利用java原生的类实现SHA256加密
     *
     * @param str 加密后的报文
     * @return encrypt
     */
    public static String getSHA256(String str) {
        MessageDigest messageDigest;
        String encodestr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = str.getBytes(UTF_8);
            messageDigest.update(bytes);
            encodestr = StringUtils.bytes2HexStr(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encodestr;
    }

    /**
     * This method uses the JCE to provide the crypto algorithm. HMAC computes a
     * Hashed Message Authentication Code with the crypto hash algorithm as a
     * parameter.
     *
     * @param crypto   : the crypto algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
     * @param keyBytes : the bytes to use for the HMAC key
     * @param text     : the message or text to be authenticated
     */
    private static byte[] hmacSha(String crypto, byte[] keyBytes, byte[] text) {
        try {
            Mac hmac;
            hmac = Mac.getInstance(crypto);
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    /**
     * This method converts a HEX string to Byte[]
     *
     * @param hex : the HEX string
     * @return a byte array
     */

    private static byte[] hexStr2Bytes(String hex) {
        // Adding one byte to get the right conversion
        // Values starting with "0" can be converted
        byte[] bArray = new BigInteger("10" + hex, 16).toByteArray();

        // Copy all the REAL bytes, not the "first"
        byte[] ret = new byte[bArray.length - 1];
        System.arraycopy(bArray, 1, ret, 0, ret.length);
        return ret;
    }

    // 0 1 2 3 4 5 6 7 8
    private static final int[] DIGITS_POWER = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};


    /**
     * This method generates a TOTP value for the given set of parameters.
     *
     * @param key          : the shared secret, HEX encoded
     * @param time         : a value that reflects a time
     * @param returnDigits : number of digits to return
     * @return a numeric String in base 10 that includes
     */

    public static String generate(String key, String time,
                                  String returnDigits) {
        return generate(key, time, returnDigits, "HmacSHA1");
    }

    public static String generate(byte[] hexBytes, String time,
                                  String returnDigits) {
        return generate(hexBytes, time, returnDigits, "HmacSHA1");
    }

    /**
     * This method generates a TOTP value for the given set of parameters.
     *
     * @param key          : the shared secret, HEX encoded
     * @param time         : a value that reflects a time
     * @param returnDigits : number of digits to return
     * @return: a numeric String in base 10 that includes
     */

    public static String generate256(String key, String time,
                                     String returnDigits) {
        return generate(key, time, returnDigits, "HmacSHA256");
    }

    public static String generate256(byte[] hexBytes, String time,
                                     String returnDigits) {
        return generate(hexBytes, time, returnDigits, "HmacSHA256");
    }

    /**
     * This method generates a TOTP value for the given set of parameters.
     *
     * @param key          : the shared secret, HEX encoded
     * @param time         : a value that reflects a time
     * @param returnDigits : number of digits to return
     * @return: a numeric String in base 10 that includes
     */

    public static String generate512(String key, String time,
                                     String returnDigits) {
        return generate(key, time, returnDigits, "HmacSHA512");
    }

    public static String generate512(byte[] hexBytes, String time,
                                     String returnDigits) {
        return generate(hexBytes, time, returnDigits, "HmacSHA512");
    }

    /**
     * This method generates a TOTP value for the given set of parameters.
     *
     * @param key          : the shared secret, HEX encoded
     * @param time         : a value that reflects a time
     * @param returnDigits : number of digits to return 返回长度 --6
     * @param crypto       : the crypto function to use
     * @return: a numeric String in base 10 that includes
     */

    public static String generate(String key, String time,
                                  String returnDigits, String crypto) {
        int codeDigits = Integer.decode(returnDigits);
        StringBuilder result = null;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        StringBuilder timeBuilder = new StringBuilder(time);
        while (timeBuilder.length() < 16) {
            timeBuilder.insert(0, "0");
        }
        time = timeBuilder.toString();

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] k = hexStr2Bytes(key);
        byte[] hash = hmacSha(crypto, k, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < codeDigits) {
            result.insert(0, "0");
        }
        return result.toString();
    }

    public static String generate(byte[] hexBytes, String time,
                                  String returnDigits, String crypto) {
        int codeDigits = Integer.decode(returnDigits);
        StringBuilder result;

        // Using the counter
        // First 8 bytes are for the movingFactor
        // Compliant with base RFC 4226 (HOTP)
        StringBuilder timeBuilder = new StringBuilder(time);
        while (timeBuilder.length() < 16) {
            timeBuilder.insert(0, "0");
        }
        time = timeBuilder.toString();

        // Get the HEX in a Byte[]
        byte[] msg = hexStr2Bytes(time);
        byte[] hash = hmacSha(crypto, hexBytes, msg);

        // put selected bytes into result int
        int offset = hash[hash.length - 1] & 0xf;

        int binary = ((hash[offset] & 0x7f) << 24)
                | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);

        int otp = binary % DIGITS_POWER[codeDigits];

        result = new StringBuilder(Integer.toString(otp));
        while (result.length() < codeDigits) {
            result.insert(0, "0");
        }
        return result.toString();
    }

    public static String sign(String content, String key) {
        String sd = getKey(key, 8);
        return getSHA256(content + sd);
    }

    public static String getKey(String key, String returnDigits) {
        long time = System.currentTimeMillis() / 30000;
        String secret = generate512(StringUtils.bytes2HexStr(key.getBytes()),
                String.valueOf(time), returnDigits);
        if (secret.length() > 16) {
            return secret.substring(0, 16);
        }
        return StringUtils.rightPad(secret, 16, "8");
    }

    public static String getKey(String did, int len) {
        try {
            String seed = StringUtils.bytes2HexStr(did.getBytes(UTF_8));
            int size = seed.length() > 4 ? seed.length() : 4;
            int index = Integer.parseInt(seed.substring(size - 4, size), 16) % 100;
            String full = getFull();
            int start = index * 8;
            int end = start + len;
            if (full.length() < end) {
                end = full.length();
                start = end - len;
            }
            return full.substring(start, end);
        } catch (Exception e) {
            Log.e("HashUtil", "getKey error", e);
        }
        return did;
    }

    public static final String getFull() {
        return BuildConfig.appKey + HASH;
    }
}
