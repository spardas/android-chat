package cn.wildfire.chat.wxapi;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WxOperator {

    private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
    private static final int THUMB_SIZE = 100; //缩略图大小

    private static WxOperator sOperator;

    private IWXAPI api;

    public static WxOperator inst(Context context) {
        if (sOperator == null) {
            synchronized (WxOperator.class) {
                if (sOperator == null) {
                    sOperator = new WxOperator(context.getApplicationContext());
                }
            }
        }
        return sOperator;
    }

    private WxOperator(Context context) {
        api = WXAPIFactory.createWXAPI(context, WXConstant.APP_ID, true);
        api.registerApp(WXConstant.APP_ID);
    }

    public void login(Activity activity) {
        if (!api.isWXAppInstalled()) {
            Toast.makeText(activity, "您手机尚未安装微信，请安装后再登录", Toast.LENGTH_SHORT).show();
            return;
        }
        api.registerApp(WXConstant.APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_xb_live_state";//官方说明：用于保持请求和回调的状态，授权请求后原样带回给第三方。该参数可用于防止csrf攻击（跨站请求伪造攻击），建议第三方带上该参数，可设置为简单的随机数加session进行校验
        api.sendReq(req);
    }

    /**
     * 是否支持分享到朋友圈
     */
    public boolean isWXAppSupportAPI(IWXAPI wxapi) {
        int wxSdkVersion = wxapi.getWXAppSupportAPI();
        if (wxSdkVersion >= TIMELINE_SUPPORTED_VERSION) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 分享文本类型
     *
     * @param text 文本内容
     * @param type 微信会话或者朋友圈等
     */
    public void shareTextToWx(String text, int type) {
        if (text == null || text.length() == 0) {
            return;
        }

        WXTextObject textObj = new WXTextObject();
        textObj.text = text;

        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.description = text;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = type;

        api.sendReq(req);
    }

    private String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

}
