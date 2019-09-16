package cn.wildfire.chat.app.api;

import android.text.TextUtils;
import android.util.Log;

import java.io.EOFException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.wildfire.chat.app.MyApp;
import cn.wildfire.chat.app.login.model.LoginData;
import cn.wildfire.chat.kit.utils.HashUtil;
import cn.wildfire.chat.kit.utils.IDUtils;
import cn.wildfire.chat.kit.utils.StringUtils;
import cn.wildfirechat.chat.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static okhttp3.internal.Util.UTF_8;

public class RetrofitHelper {
    public static final String TAG = "RetrofitHelper";
    private static Retrofit retrofit;

    static {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder().addNetworkInterceptor(loggingInterceptor)
                .addInterceptor(new SessionInterceptor())
                .addInterceptor(chain -> {
                    String token = "";
                    if (!TextUtils.isEmpty(LoginData.instance.getLoginToken())) {
                        token = LoginData.instance.getLoginToken();
                    }
                    TreeMap<String, String> treeMap = new TreeMap<>();
                    String did = IDUtils.getDeviceUUID(MyApp.sContext);
                    treeMap.put("did", did);
                    treeMap.put("token", token);
                    treeMap.put("productVersion", String.valueOf(BuildConfig.VERSION_CODE));
                    treeMap.put("versionName", BuildConfig.VERSION_NAME);
                    treeMap.put("channel", "im");
                    treeMap.put("productId", "MONEY_DUCK_ANDROID");
                    String data = getRequestMD5Data(chain);
                    treeMap.put("data", data);
                    Request.Builder requestBuilder = chain.request().newBuilder();
                    StringBuilder content = new StringBuilder();
                    boolean first = true;
                    for (Map.Entry<String, String> entry : treeMap.entrySet()) {
                        //data不加进header
                        if (!"data".equals(entry.getKey())) {
                            requestBuilder.addHeader(entry.getKey(), entry.getValue());
                        }
                        if (!TextUtils.isEmpty(entry.getValue())) {
                            content.append(first ? "" : "&").append(entry.getKey()).append("=").append(entry.getValue());
                            first = false;
                        }
                    }
                    requestBuilder.addHeader("sign", HashUtil.sign(content.toString(), did));
                    return chain.proceed(requestBuilder.build());
                })
                .sslSocketFactory(getSSLSocketFactory())
                .hostnameVerifier(getHostnameVerifier())
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_HOST)
                .addConverterFactory(BuildConfig.openSec ? EncryptConvertFactory.create() : GsonConverterFactory.create()) // 加密字符串
                .client(httpClient).build();
    }

    private static String getRequestMD5Data(Interceptor.Chain chain) {
        try {
            RequestBody requestBody = chain.request().body();
            if (requestBody == null) {
                return StringUtils.EMPTY_STRING;
            }
            if (requestBody.contentLength() == 0) {
                return StringUtils.EMPTY_STRING;
            }
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            return StringUtils.bytes2HexStr(MessageDigest.getInstance("MD5").digest(buffer.readByteArray()));
        } catch (Exception e) {
            Log.e(TAG, "getRequestData error", e);
        }
        return StringUtils.EMPTY_STRING;
    }

    private static String getRequestData(Interceptor.Chain chain) {
        try {
            RequestBody requestBody = chain.request().body();
            if (requestBody == null) {
                return StringUtils.EMPTY_STRING;
            }
            Charset charset = UTF_8;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF_8);
            }
            Buffer buffer = new Buffer();
            requestBody.writeTo(buffer);
            if (isPlaintext(buffer)) {
                String data = buffer.readString(charset);
                Log.e(TAG, "data=" + Arrays.toString(data.getBytes()));
                return data;
            }
        } catch (Exception e) {
            Log.e(TAG, "getRequestData error", e);
        }
        return StringUtils.EMPTY_STRING;
    }

    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    public static <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }

    //获取这个SSLSocketFactory
    public static SSLSocketFactory getSSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //获取TrustManager
    private static TrustManager[] getTrustManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
        return trustAllCerts;
    }

    //获取HostnameVerifier
    public static HostnameVerifier getHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        };
        return hostnameVerifier;
    }

}
