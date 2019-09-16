package cn.wildfire.chat.app.login;

import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

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
import cn.wildfirechat.chat.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PwdLoginActivity extends WfcBaseActivity {
    @Bind(R.id.input_phone)
    EditText phoneView;

    @Bind(R.id.input_pwd)
    EditText pwdView;

    @Override
    protected int contentLayout() {
        return R.layout.activity_pwd_login;
    }

    @OnClick(R.id.btn_login)
    void onLoginClick() {
        String phone = phoneView.getText().toString();
        String pwd = pwdView.getText().toString();
        pwdLogin(phone, pwd);
    }

    private void pwdLogin(String phone, String pwd) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content("登录中...")
                .progress(true, 100)
                .cancelable(false)
                .build();
        LoginBody body = new LoginBody();
        LoginHelper.buildBaseLoginBody(body, "pwdLogin");
        body.setLoginId(phone);
        body.setPwd(pwd);
        RpcService rpcService = RetrofitHelper.createService(RpcService.class);
        Call<CommonResult<LoginResult>> call = rpcService.login(body);
        call.enqueue(new Callback<CommonResult<LoginResult>>() {
            @Override
            public void onResponse(Call<CommonResult<LoginResult>> call, Response<CommonResult<LoginResult>> response) {
                if (!PwdLoginActivity.this.isFinishing()) {
                    return;
                }
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (response.body() != null) {
                    if (response.body().getData() != null) {
                        LoginData.instance.save(PwdLoginActivity.this, response.body().getData());
                        startActivity(new Intent(PwdLoginActivity.this, MainActivity.class));
                        finish();
                    } else if (response.body().getCode() == 2001) {
                        Toast.makeText(PwdLoginActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PwdLoginActivity.this, response.body().getMsg(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PwdLoginActivity.this, "服务器异常，请稍后重试", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CommonResult<LoginResult>> call, Throwable t) {
                if (!PwdLoginActivity.this.isFinishing()) {
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
                Toast.makeText(PwdLoginActivity.this, "网络异常，请稍后重试", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
