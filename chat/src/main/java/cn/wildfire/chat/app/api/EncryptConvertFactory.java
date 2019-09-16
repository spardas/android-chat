package cn.wildfire.chat.app.api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import cn.wildfire.chat.app.MyApp;
import cn.wildfire.chat.kit.utils.AESUtils;
import cn.wildfire.chat.kit.utils.IDUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class EncryptConvertFactory extends Converter.Factory {
    private final Gson gson;

    static EncryptConvertFactory create() {
        return create(new Gson());
    }

    private static EncryptConvertFactory create(Gson gson) {
        return new EncryptConvertFactory(gson);
    }

    private EncryptConvertFactory(Gson gson) {
        this.gson = gson;
    }

    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return new EncryptRequestBodyConvert<>(gson);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new EncryptResponseBodyConvert<>(gson, adapter);
    }

    public static class EncryptRequestBodyConvert<T> implements Converter<T, RequestBody> {

        private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
        private final Gson gson;

        EncryptRequestBodyConvert(Gson gson) {
            this.gson = gson;
        }

        @Override
        public RequestBody convert(@NonNull T value) {
            //加密操作，返回字节数组
            Log.e("Hunter", "加密的字符串: " + gson.toJson(value));
            String did = IDUtils.getDeviceUUID(MyApp.sContext);
            byte[] encrypt = AESUtils.encrypt(gson.toJson(value), did);
            if (encrypt == null) {
                throw new IllegalStateException("请求构造为空");
            }
            return RequestBody.create(MEDIA_TYPE, encrypt);
        }
    }

    public static class EncryptResponseBodyConvert<T> implements Converter<ResponseBody, T> {

        private final Gson gson;
        private final TypeAdapter<T> adapter;

        EncryptResponseBodyConvert(Gson gson, TypeAdapter<T> adapter) {
            this.gson = gson;
            this.adapter = adapter;
        }


        @Override
        public T convert(@NonNull ResponseBody value) throws IOException {
            byte[] bytes = value.bytes();

            //对字节数组进行解密操作
            String did = IDUtils.getDeviceUUID(MyApp.sContext);
            String decryptString = AESUtils.decrypt(bytes, did);
            Log.i("Hunter", "解密后的服务器数据字符串：" + decryptString);
            if (decryptString == null) {
                return null;
            }
            //对解密的字符串进行处理
            int position = decryptString.lastIndexOf("}");
            String jsonString = decryptString.substring(0, position + 1);
            Log.i("Hunter", "解密后的服务器数据字符串处理为json：" + jsonString);

            //这部分代码参考GsonConverterFactory中GsonResponseBodyConverter<T>的源码对json的处理
            Reader reader = new StringReader(jsonString);
            JsonReader jsonReader = gson.newJsonReader(reader);
            try {
                return adapter.read(jsonReader);
            } finally {
                reader.close();
                jsonReader.close();
            }

        }
    }
}
