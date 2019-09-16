package cn.wildfire.chat.app.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

import cn.wildfire.chat.app.MyApp;
import cn.wildfire.chat.app.api.model.LoginBody;
import cn.wildfire.chat.app.api.model.LoginResult;
import cn.wildfire.chat.app.login.model.LoginData;
import cn.wildfire.chat.kit.utils.AESUtils;
import cn.wildfire.chat.kit.utils.IDUtils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;

public class SessionInterceptor implements Interceptor {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response originalResponse = chain.proceed(request);

        ResponseBody responseBody = originalResponse.body();
        if (responseBody == null) {
            return originalResponse;
        }
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();
        byte[] bytes = buffer.clone().readByteArray();

        //对字节数组进行解密操作
        String did = IDUtils.getDeviceUUID(MyApp.sContext);
        String bodyString = AESUtils.decrypt(bytes, did);
        if (!TextUtils.isEmpty(bodyString)) {
            int code = 0;
            try {
                JSONObject jsonObject = new JSONObject(bodyString);
                code = jsonObject.optInt("code");
            } catch (Exception ignored) {

            }
            if (code == 203 && autoLogin()) {
                if (originalResponse.body() != null) {
                    originalResponse.body().close();
                }
                return chain.proceed(request);
            } else if (code == 209) {
                //以下代码理论上不会执行
                if (originalResponse.body() != null) {
                    originalResponse.body().close();
                }
                return chain.proceed(request);
            }
        }
        return originalResponse;
    }

    private boolean autoLogin() {
        Context mContext = MyApp.sContext;
        String userId = LoginData.instance.getUserId(mContext);
        if (TextUtils.isEmpty(userId)) {
            return false;
        }
        LoginBody body = new LoginBody();
        body.setAuthCode("");
        body.setBrand(IDUtils.getBrand());
        body.setDid(IDUtils.getDeviceUUID(mContext));
        body.setImei(IDUtils.getIMEI(mContext));
        body.setImsi(IDUtils.getIMSI(mContext));
        body.setOsVersion(IDUtils.getOSVersion());
        body.setType("WEI_CHAT");
        body.setLoginType("autoLogin");
        body.setMobileModel(IDUtils.getPhoneModel());
        body.setUserId(userId);
        body.setWifiMac(IDUtils.getWifiMac(mContext));
        RpcService rpcService = RetrofitHelper.createService(RpcService.class);
        Call<CommonResult<LoginResult>> call = rpcService.login(body);
        try {
            CommonResult<LoginResult> response = call.execute().body();
            if (response != null && response.getData() != null) {
                LoginData.instance.save(mContext, response.getData());
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
