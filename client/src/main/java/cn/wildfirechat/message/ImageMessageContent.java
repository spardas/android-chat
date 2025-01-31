package cn.wildfirechat.message;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Parcel;
import android.util.Size;

import com.tencent.mars.proto.ProtoLogic;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import cn.wildfirechat.message.core.ContentTag;
import cn.wildfirechat.message.core.MessageContentType;
import cn.wildfirechat.message.core.MessagePayload;
import cn.wildfirechat.message.core.PersistFlag;
import cn.wildfirechat.remote.ChatManager;

/**
 * Created by heavyrain lee on 2017/12/6.
 */

@ContentTag(type = MessageContentType.ContentType_Image, flag = PersistFlag.Persist_And_Count)
public class ImageMessageContent extends MediaMessageContent {
    private Bitmap thumbnail;
    private byte[] thumbnailBytes;
    private double imageWidth;
    private double imageHeight;
    private String thumbPara;

    public ImageMessageContent() {
    }

    public ImageMessageContent(String path) {
        this.localPath = path;
        mediaType = MessageContentMediaType.IMAGE;
    }

    public Bitmap getThumbnail() {
        if (thumbnailBytes != null) {
            thumbnail = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length);
        }
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }


    @Override
    public MessagePayload encode() {
        MessagePayload payload = super.encode();
        payload.searchableContent = "[图片]";

        if (thumbPara != null && !thumbPara.isEmpty() && imageWidth > 0) {
            try {
                JSONObject objWrite = new JSONObject();
                objWrite.put("w", imageWidth);
                objWrite.put("h", imageHeight);
                objWrite.put("tp", thumbPara);
                payload.content = objWrite.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //检查是否有缩略图参数，如果有计算大小，并带上缩略图参数
        //encode有可能在主进程被调用，也可能在协议进程被调用，还不知道该怎么处理？
//        else if(ProtoLogic.getImageThumbPara() != null) {
//
////            imageWidth = get size from image;
////            imageHeight = get size from image
//            thumbPara = ChatManager.Instance().getImageThumbPara();
//            try {
//                JSONObject objWrite = new JSONObject();
//                objWrite.put("w", imageWidth);
//                objWrite.put("h", imageHeight);
//                objWrite.put("tp", thumbPara);
//                payload.content = objWrite.toString();
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }

        if (payload.content == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 75, baos);
            payload.binaryContent = baos.toByteArray();
        }

        return payload;
    }


    @Override
    public void decode(MessagePayload payload) {
        super.decode(payload);
        thumbnailBytes = payload.binaryContent;
        if (payload.content != null && !payload.content.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(payload.content);
                imageWidth = jsonObject.optDouble("w");
                imageHeight = jsonObject.optDouble("h");
                thumbPara = jsonObject.optString("tp");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public String digest(Message message) {
        return "[图片]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public double getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(double imageWidth) {
        this.imageWidth = imageWidth;
    }

    public double getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(double imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getThumbPara() {
        return thumbPara;
    }

    public void setThumbPara(String thumbPara) {
        this.thumbPara = thumbPara;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.thumbnail, flags);
        dest.writeByteArray(this.thumbnailBytes);
    }

    protected ImageMessageContent(Parcel in) {
        super(in);
        this.thumbnail = in.readParcelable(Bitmap.class.getClassLoader());
        this.thumbnailBytes = in.createByteArray();
    }

    public static final Creator<ImageMessageContent> CREATOR = new Creator<ImageMessageContent>() {
        @Override
        public ImageMessageContent createFromParcel(Parcel source) {
            return new ImageMessageContent(source);
        }

        @Override
        public ImageMessageContent[] newArray(int size) {
            return new ImageMessageContent[size];
        }
    };
}
