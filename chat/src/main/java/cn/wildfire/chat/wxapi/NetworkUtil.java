package cn.wildfire.chat.wxapi;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class NetworkUtil
{
    private static String TAG = "MicroMsg.NetworkUtil";

    public static final int GET_TOKEN = 1;
    public static final int CHECK_TOKEN = 2;
    public static final int REFRESH_TOKEN = 3;
    public static final int GET_INFO = 4;
    public static final int GET_IMG = 5;

    public static void sendWxAPI(String url, int msgTag, HttpCallback callback) {
        HttpsThread httpsThread = new HttpsThread(url, msgTag, callback);
        httpsThread.start();
    }

    public static void getImage(String url, int msgTag) {
        HttpsThread httpsThread = new HttpsThread(url, msgTag);
        httpsThread.start();
    }

    static class HttpsThread extends Thread {

        private String httpsUrl;
        private int msgTag;
        private HttpCallback callback;

        public HttpsThread(String url, int msgTag) {
            this.httpsUrl = url;
            this.msgTag = msgTag;
        }
        public  HttpsThread(String url, int msgTag, HttpCallback callback) {
            this.httpsUrl = url;
            this.msgTag = msgTag;
            this.callback = callback;
        }

        @Override
        public void run() {
            if (msgTag == GET_IMG) {
                try {
                    byte[] imgdata = httpURLConnectionGet(httpsUrl);
                    Message msg = Message.obtain();
                    msg.what = msgTag;
                    Bundle data = new Bundle();
                    data.putByteArray("imgdata", imgdata);
                    msg.setData(data);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            } else {
                int resCode;
                InputStream in;
                String httpResult = null;
                try {
                    URL url = new URL(httpsUrl);
                    URLConnection urlConnection = url.openConnection();
                    HttpsURLConnection httpsConn = (HttpsURLConnection) urlConnection;
                    httpsConn.setAllowUserInteraction(false);
                    httpsConn.setInstanceFollowRedirects(true);
                    httpsConn.setRequestMethod("GET");
                    httpsConn.connect();
                    resCode = httpsConn.getResponseCode();

                    if (resCode == HttpURLConnection.HTTP_OK) {
                        in = httpsConn.getInputStream();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "iso-8859-1"), 8);
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        in.close();
                        httpResult = sb.toString();
                        Log.i(TAG, httpResult);
                        if (callback != null) {
                            callback.onSuccess(sb.toString());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }

        private static byte[] httpURLConnectionGet(String url) throws Exception {
            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            if(connection == null){
                Log.i(TAG,"open connection failed.");
            }
            int responseCode = connection.getResponseCode();
            if (responseCode >= 300) {
                connection.disconnect();
                Log.w(TAG, "dz[httpURLConnectionGet 300]");
                return null;
            }

            InputStream is = connection.getInputStream();
            byte[] data = readStream(is);
            connection.disconnect();

            return data;
        }

        private static byte[] readStream(InputStream inStream) throws IOException {
            byte[] buffer = new byte[1024];
            int len = -1;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] data = outStream.toByteArray();
            outStream.close();
            inStream.close();
            return data;
        }
    }

    public interface HttpCallback {
        void onSuccess(String response);
    }
}
