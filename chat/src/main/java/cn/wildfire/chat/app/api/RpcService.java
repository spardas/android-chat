package cn.wildfire.chat.app.api;


import cn.wildfire.chat.app.api.model.LoginBody;
import cn.wildfire.chat.app.api.model.LoginResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RpcService {

    // 登录接口
    @POST("/user/login")
    Call<CommonResult<LoginResult>> login(@Body LoginBody body);
}
