package cn.wildfire.chat.app.login;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseResp;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.OnClick;
import cn.wildfire.chat.app.api.CommonResult;
import cn.wildfire.chat.app.api.RetrofitHelper;
import cn.wildfire.chat.app.api.RpcService;
import cn.wildfire.chat.app.api.model.LoginBody;
import cn.wildfire.chat.app.api.model.LoginResult;
import cn.wildfire.chat.app.login.model.LoginData;
import cn.wildfire.chat.app.main.MainActivity;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.wxapi.WXModel;
import cn.wildfire.chat.wxapi.WxOperator;
import cn.wildfirechat.chat.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthLoginActivity extends WfcBaseActivity {
    // 底图是否要消失
    private boolean canDismissImage = true;
    private boolean isWxAuth = false;       // 微信授权是否返回

    @Bind(R.id.splash_image_layout)
    RelativeLayout mSplashImageLayout;

    @Override
    protected int contentLayout() {
        return R.layout.activity_auth_login;
    }

    @Override
    protected void afterViews() {
        EventBus.getDefault().register(this);//注册

        mSplashImageLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (canDismissImage) {
                    mSplashImageLayout.setVisibility(View.GONE);
                }
            }
        }, 1000);
        String uid = LoginData.instance.getUserId(AuthLoginActivity.this);
        if (!TextUtils.isEmpty(uid)) {
            autoLogin();
        } else {
            mSplashImageLayout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.login_with_wx)
    public void onclick() {
        WxOperator.inst(AuthLoginActivity.this).login(AuthLoginActivity.this);
    }

    private void autoLogin() {
        requestLogin("autoLogin", "", null);
    }

    /**
     * @param model model
     * @see cn.wildfire.chat.wxapi.WXEntryActivity#onResp
     * EventBus.getDefault().post(model)
     */
    @Subscribe
    public void authLogin(WXModel model) {
        if (model.getType() == ConstantsAPI.COMMAND_SENDAUTH) { //登录
            if (model.getErrorCode() == BaseResp.ErrCode.ERR_OK) { // 成功
                if (isWxAuth) {
                    return;
                }
                isWxAuth = true;
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .content("登录中...")
                        .progress(true, 100)
                        .cancelable(false)
                        .build();
                requestLogin("authLogin", model.getAuthCode(), dialog);
            } else {
                Toast.makeText(AuthLoginActivity.this, "登录授权失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestLogin(String loginType, String authCode, MaterialDialog loading) {
        if (loading != null) {
            loading.show();
        }
        LoginBody body = new LoginBody();
        body.setAuthCode(authCode);
        LoginHelper.buildBaseLoginBody(body, loginType);
        RpcService rpcService = RetrofitHelper.createService(RpcService.class);
        Call<CommonResult<LoginResult>> call = rpcService.login(body);
        call.enqueue(new Callback<CommonResult<LoginResult>>() {
            @Override
            public void onResponse(Call<CommonResult<LoginResult>> call, Response<CommonResult<LoginResult>> response) {
                isWxAuth = false;
                if (!AuthLoginActivity.this.isFinishing()) {
                    return;
                }
                if (loading != null && loading.isShowing()) {
                    loading.dismiss();
                }
                if (response.body() != null) {
                    if (response.body().getData() != null) {
                        LoginData.instance.save(AuthLoginActivity.this, response.body().getData());
                        startActivity(new Intent(AuthLoginActivity.this, MainActivity.class));
                        finish();
                    } else if (response.body().getCode() == 2001) {
                        canDismissImage = false;
                        Toast.makeText(AuthLoginActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AuthLoginActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AuthLoginActivity.this, "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResult<LoginResult>> call, Throwable t) {
                if (!AuthLoginActivity.this.isFinishing()) {
                    if (loading != null && loading.isShowing()) {
                        loading.dismiss();
                    }
                }
                Toast.makeText(AuthLoginActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
