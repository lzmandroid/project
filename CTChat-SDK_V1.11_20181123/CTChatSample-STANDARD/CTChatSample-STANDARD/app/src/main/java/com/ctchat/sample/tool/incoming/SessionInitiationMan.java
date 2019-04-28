package com.ctchat.sample.tool.incoming;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 会话发起方式
 */
public class SessionInitiationMan {
    public static final String INTENT_DATA_KEY = "initialization";

    public static final int INITIATION_LIST = 0;//跳转到list
    public static final int INITIATION_PTT = 1;//跳转到ptt
    public static final int INITIATION_IM = 2;//跳转到im

    public static final int SESSION_UNCONNECTION = 0;//不直接建立会话
    public static final int SESSION_CONNECTION = 1;//进入直接建立会话

    public static class SessionInitiation implements Parcelable {
        public String sessionCode;
        public int initializationMode;
        public int connectionStatus;

        public SessionInitiation() {
        }

        protected SessionInitiation(Parcel p) {
            sessionCode = p.readString();
            initializationMode = p.readInt();
            connectionStatus = p.readInt();
        }

        @Override
        public void writeToParcel(Parcel p, int flags) {
            p.writeString(sessionCode);
            p.writeInt(initializationMode);
            p.writeInt(connectionStatus);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SessionInitiation> CREATOR = new Creator<SessionInitiation>() {
            @Override
            public SessionInitiation createFromParcel(Parcel in) {
                return new SessionInitiation(in);
            }

            @Override
            public SessionInitiation[] newArray(int size) {
                return new SessionInitiation[size];
            }
        };


        public String getSessionCode() {
            return sessionCode;
        }

        public int getInitializationMode() {
            return initializationMode;
        }

        public int getConnectionStatus() {
            return connectionStatus;
        }
    }

}
