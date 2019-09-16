package cn.wildfire.chat.wxapi;

public class WXModel {

    private int type;
    private int errorCode;
    private String authCode; // 登录

    public WXModel(int type, int errorCode) {
        this.type = type;
        this.errorCode = errorCode;
    }

    public WXModel(int type, int errorCode, String authCode) {
        this.type = type;
        this.errorCode = errorCode;
        this.authCode = authCode;
    }

    public int getType() {
        return type;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getAuthCode() {
        return authCode;
    }
}
