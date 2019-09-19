package cn.wildfire.chat.app;

import android.os.Environment;

/**
 * Created by heavyrain lee on 2017/11/24.
 */

public interface Config {

    String IM_SERVER_HOST = "192.168.1.106";
    int IM_SERVER_PORT = 8079;

    //正式商用时，建议用https，确保token安全
    String APP_SERVER_ADDRESS = "http://192.168.1.106:8888";

    String ICE_ADDRESS = "turn:turn.liyufan.win:3478";
    String ICE_USERNAME = "wfchat";
    String ICE_PASSWORD = "wfchat";

    int DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND = 120;

    String VIDEO_SAVE_DIR = Environment.getExternalStorageDirectory().getPath() + "/wfc/video";
    String AUDIO_SAVE_DIR = Environment.getExternalStorageDirectory().getPath() + "/wfc/audio";
    String PHOTO_SAVE_DIR = Environment.getExternalStorageDirectory().getPath() + "/wfc/photo";
    String FILE_SAVE_DIR = Environment.getExternalStorageDirectory().getPath() + "/wfc/file";
}
