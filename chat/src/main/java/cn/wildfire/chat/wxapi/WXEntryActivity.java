package cn.wildfire.chat.wxapi;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;


public class WXEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, WXConstant.APP_ID, false);

        try {
            Intent intent = getIntent();
            api.handleIntent(intent, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
    }

    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) { //微信支付
            WXModel model = new WXModel(ConstantsAPI.COMMAND_PAY_BY_WX, resp.errCode);
            EventBus.getDefault().post(model);
        } else if (resp.getType() == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) { //分享
            WXModel model = new WXModel(2, resp.errCode, "");
            EventBus.getDefault().post(model);
        } else if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) { //登陆
            SendAuth.Resp authResp = (SendAuth.Resp) resp;
            WXModel model = new WXModel(1, resp.errCode, authResp.code);
            EventBus.getDefault().post(model);
        }
        finish();
    }
}
