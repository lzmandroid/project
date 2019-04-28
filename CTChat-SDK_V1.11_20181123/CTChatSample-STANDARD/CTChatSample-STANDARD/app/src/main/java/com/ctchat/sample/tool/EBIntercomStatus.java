package com.ctchat.sample.tool;

/**
 * 会话状态对象
 */
public class EBIntercomStatus {
    public static final int SESSION_IDLE_STATUS = 0;
    public static final int SESSION_CALLING_STATUS = 1;
    public static final int SESSION_DIALOG_STATUS = 2;
    public static final int MEDIA_PREPARE_STATUS = 3;
    public static final int MEDIA_IDLE_STATUS = 4;
    public static final int MEDIA_LISTEN_STATUS = 5;
    public static final int MEDIA_TALK_STATUS = 6;

    public int status;

    public EBIntercomStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
