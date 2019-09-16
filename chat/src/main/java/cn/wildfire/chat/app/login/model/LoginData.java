package cn.wildfire.chat.app.login.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import cn.wildfire.chat.app.api.model.LoginResult;

public class LoginData {
    public static final LoginData instance = new LoginData();
    private static final String ID_KEY = "id";
    private static final String TOKEN_KEY = "token";
    private static final String IM_TOKEN_KEY = "imToken";
    private static final String CONFIG_KEY = "config";

    private String userId;
    private String token;

    public void save(Context context, LoginResult result) {
        userId = result.getUserId();
        token = result.getToken();
        if (TextUtils.isEmpty(result.getToken())) {
            token = result.getImToken();
        }
        SharedPreferences sp = context.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE);
        sp.edit().putString(ID_KEY, result.getUserId())
                .putString(TOKEN_KEY, result.getToken())
                .putString(IM_TOKEN_KEY, result.getImToken())
                .apply();
    }

    private void load(Context context) {
        SharedPreferences sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        userId = sp.getString(ID_KEY, "");
        token = sp.getString(TOKEN_KEY, "");
        if (TextUtils.isEmpty(token)) {
            token = sp.getString(IM_TOKEN_KEY, "");
        }
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
}
