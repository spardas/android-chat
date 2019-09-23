package cn.wildfire.chat.app.login.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import cn.wildfire.chat.app.api.model.LoginResult;

public class LoginData {
    public static final LoginData instance = new LoginData();
    private static final String ID_KEY = "id";
    private static final String TOKEN_KEY = "token";
    private static final String CONFIG_KEY = "config";
    private static final String PHONE_KEY = "phone";
    private static final String PWD_KEY = "pwd";

    private String userId;
    private String token;

    private String phone;
    private String pwd;

    public void save(Context context, LoginResult result) {
        userId = result.getUserId();
        if (TextUtils.isEmpty(result.getUserId())) {
            userId = result.getImUserId();
        }
        token = result.getToken();
        if (TextUtils.isEmpty(result.getToken())) {
            token = result.getImToken();
        }
        SharedPreferences sp = context.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putString(ID_KEY, userId)
                .putString(TOKEN_KEY, token)
                .apply();
    }

    private void load(Context context) {
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        userId = sp.getString(ID_KEY, "");
        token = sp.getString(TOKEN_KEY, "");
        phone = sp.getString(PHONE_KEY, "");
        pwd = sp.getString(PWD_KEY, "");
    }

    public String getUserId(Context context) {
        if (!TextUtils.isEmpty(userId)) {
            return userId;
        }
        load(context);
        return userId;
    }

    public String getToken(Context context) {
        if (!TextUtils.isEmpty(token)) {
            return token;
        }
        load(context);
        return token;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLoginToken() {
        return null;
    }

    public void savePwd(Context context, String phone, String pwd) {
        SharedPreferences sp = context.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putString(PHONE_KEY, phone)
                .putString(PWD_KEY, pwd)
                .apply();
    }

    public String getPhone(Context context) {
        if (!TextUtils.isEmpty(phone)) {
            return phone;
        }
        load(context);
        return phone;
    }

    public String getPwd(Context context) {
        if (!TextUtils.isEmpty(pwd)) {
            return pwd;
        }
        load(context);
        return pwd;
    }

}
