package cn.wildfire.chat.app.login;

import cn.wildfire.chat.app.MyApp;
import cn.wildfire.chat.app.api.model.LoginBody;
import cn.wildfire.chat.app.login.model.LoginData;
import cn.wildfire.chat.kit.ChatManagerHolder;
import cn.wildfire.chat.kit.utils.IDUtils;

public class LoginHelper {

    public static void buildBaseLoginBody(LoginBody body, String authLogin) {
        body.setBrand(IDUtils.getBrand());
        body.setDid(IDUtils.getDeviceUUID(MyApp.sContext));
        body.setImei(IDUtils.getIMEI(MyApp.sContext));
        body.setImsi(IDUtils.getIMSI(MyApp.sContext));
        body.setOsVersion(IDUtils.getOSVersion());
        body.setType("WEI_CHAT");
        body.setLoginType(authLogin);
        body.setMobileModel(IDUtils.getPhoneModel());
        body.setUserId(LoginData.instance.getUserId(MyApp.sContext));
        body.setWifiMac(IDUtils.getWifiMac(MyApp.sContext));
        body.setChannel("im");
        try {
            body.setClientId(ChatManagerHolder.gChatManager.getClientId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
