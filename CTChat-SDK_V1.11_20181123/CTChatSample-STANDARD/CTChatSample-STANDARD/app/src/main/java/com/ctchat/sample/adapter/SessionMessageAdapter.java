package com.ctchat.sample.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.chatIM.MessageType;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sdk.ptt.tool.userinfo.UserInfoManager;
import com.ctchat.sdk.ptt.util.DateTool;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import com.ctchat.sample.util.BitmapUtil;
import com.ctchat.sample.util.Util;

public class SessionMessageAdapter extends BaseAdapter {
    private static final String TAG = "SessionMessageAdapter";
    private SessionEntity currentSession = null;
    private Context mContext = null;
    private View.OnClickListener onClicklistener;
    private View.OnLongClickListener onLongClickListener;
    private String filePath = "";

    public ImageLoader imageLoader = ImageLoader.getInstance();

    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnFail(R.drawable.ic_error)
            .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .build();


    public SessionMessageAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public SessionMessageAdapter(Context mContext, View.OnClickListener onClicklistener, View.OnLongClickListener onLongClickListener) {
        this.mContext = mContext;
        this.onClicklistener = onClicklistener;
        this.onLongClickListener = onLongClickListener;
    }

    public void setCurrentSession(SessionEntity currentSession) {
        this.currentSession = currentSession;
    }

    @Override
    public int getCount() {
        int i = 0;
        if (this.currentSession != null) {
            if (this.currentSession.getMessages() != null) {
                i = this.currentSession.getMessages().size();
            }
        }
        return i;
    }

    @Override
    public MessageEntity getItem(int position) {
        if (this.currentSession != null) {
            try {
                MessageEntity localMessage = this.currentSession.getMessages().get(position);
                return localMessage;
            } catch (IndexOutOfBoundsException localIndexOutOfBoundsException) {
            }
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MessageEntity localMessage = getItem(position);

        if ( null == localMessage.getBody()){
            Logger.e(TAG,"Message From["+localMessage.getNameFrom()+"] Body Null! Type["
                    + localMessage.getType() + "] Time["+ localMessage.getTime()+"] Curuser:"
                    + UserInfoManager.getInstance().getUserInfo(mContext).getMdn(),true);
            localMessage.setBody(mContext.getString(R.string.intercom_im_tip_invalid_message));
        }

        if (localMessage.isSelf(mContext) || localMessage.getBody().equals(mContext.getString(R.string.talk_call_state_outgoing_call))) {
            convertView = buildMessageItemWithSend(convertView, localMessage);
        } else {
            convertView = buildMessageItemWithReceive(convertView, localMessage);
        }
        return convertView;
    }

    /**
     * 显示自己发送的消息
     *
     * @param convertView
     * @param localMessage
     * @return
     */
    private View buildMessageItemWithSend(View convertView, MessageEntity localMessage) {
        ViewHolderSend holderSend = null;
        int messageType = localMessage.getType();//消息类型
        int messageState = localMessage.getState();//消息状态
        if ((convertView == null) || !(convertView.getTag() instanceof ViewHolderSend)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chatting_item_msg_text_right, null);
            holderSend = new ViewHolderSend(convertView);
            holderSend.ViewHolderInit(convertView);
            convertView.setTag(holderSend);
        }

        holderSend = (ViewHolderSend) convertView.getTag();

        //设置点击事件的监听
        holderSend.ivContent.setOnClickListener(this.onClicklistener);
        holderSend.rlMessageVoice.setOnClickListener(this.onClicklistener);
        holderSend.ivVideoContent.setOnClickListener(this.onClicklistener);
        holderSend.ivMapView.setOnClickListener(this.onClicklistener);
        holderSend.tvAddress.setOnClickListener(this.onClicklistener);
        holderSend.ivVideoPlayResend.setOnClickListener(this.onClicklistener);
        //设置长按事件监听
        holderSend.tvContent.setOnLongClickListener(this.onLongClickListener);
        holderSend.ivContent.setOnLongClickListener(this.onLongClickListener);
        holderSend.rlMessageVoice.setOnLongClickListener(this.onLongClickListener);
        holderSend.ivVideoContent.setOnLongClickListener(this.onLongClickListener);
        holderSend.ivMapView.setOnLongClickListener(this.onLongClickListener);
        holderSend.tvAddress.setOnLongClickListener(this.onLongClickListener);
        holderSend.tvSystemMsgType.setOnLongClickListener(this.onLongClickListener);

        String date = localMessage.getDate();

        if (DateTool.isToday(date)){
            holderSend.tvSendTime.setText(localMessage.getTime());
        }else {
            holderSend.tvSendTime.setText(date + " " + localMessage.getTime());
        }

        holderSend.tvSender.setVisibility(View.GONE);


        Logger.d(TAG, "messageState = " + messageState + "   msgCode = " + localMessage.getMessageCode());
        switch (messageState) {
            case MessageEntity.STATE_RES_DOING:
            case MessageEntity.STATE_SENDING:
                holderSend.ivSendState.setVisibility(View.VISIBLE);
                holderSend.ivSendState.setImageResource(R.drawable.icon_send_arrow);
                break;
            case MessageEntity.STATE_RES_FAIL:
            case MessageEntity.STATE_RESULT_FAIL:
                holderSend.ivSendState.setVisibility(View.VISIBLE);
                holderSend.ivSendState.setImageResource(R.drawable.icon_send_error);
                break;
            case MessageEntity.STATE_RESULT_OK:
                holderSend.ivSendState.setVisibility(View.GONE);
                break;
            default:
                break;
        }

        switch (messageType) {
            case MessageType.TYPE_SYSTEM:
                holderSend.llMessageSystem.setVisibility(View.VISIBLE);
                holderSend.tvContent.setVisibility(View.GONE);
                holderSend.ivContent.setVisibility(View.GONE);
                holderSend.rlMessageVoice.setVisibility(View.GONE);
                holderSend.rlVideoContent.setVisibility(View.GONE);
                holderSend.rlMessageLocation.setVisibility(View.GONE);
                //TODO：TEST
                holderSend.tvSystemMsgType.setText(localMessage.getBody());
                Drawable drawable = mContext.getResources().getDrawable(R.drawable.call_dial_out);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());
                holderSend.tvSystemMsgType.setCompoundDrawables(drawable,null,null,null);

                holderSend.ivSendState.setVisibility(View.GONE);

                holderSend.tvSystemMsgType.setTag(localMessage);

                //TODO 在textViwe中显示图片
                break;
            case MessageType.TYPE_TEXT://文本
                holderSend.tvContent.setVisibility(View.VISIBLE);
                holderSend.ivContent.setVisibility(View.GONE);
                holderSend.rlMessageVoice.setVisibility(View.GONE);
                holderSend.rlVideoContent.setVisibility(View.GONE);
                holderSend.rlMessageLocation.setVisibility(View.GONE);
                holderSend.llMessageSystem.setVisibility(View.GONE);
                holderSend.tvContent.setText(localMessage.getBody());

                holderSend.tvContent.setTag(localMessage);

                break;
            case MessageType.TYPE_PICTURE://图片
                filePath = Util.SYS_PATH_IMG + localMessage.getMessageCode();

                localMessage.setLocalPath(filePath);

                holderSend.tvContent.setVisibility(View.GONE);
                holderSend.ivContent.setVisibility(View.VISIBLE);
                holderSend.rlMessageVoice.setVisibility(View.GONE);
                holderSend.rlVideoContent.setVisibility(View.GONE);
                holderSend.rlMessageLocation.setVisibility(View.GONE);
                holderSend.llMessageSystem.setVisibility(View.GONE);
                String imagePath = "file://" + filePath;

                holderSend.ivContent.setImageDrawable(null);
                holderSend.ivContent.setBackgroundResource(0);
                setImageContentLayoutParams(holderSend.ivContent, 300, 300);

                displayImage(imagePath, holderSend.ivContent);

                holderSend.ivContent.setTag(localMessage);

                break;
            case MessageType.TYPE_VIDEO://视频
                filePath = Util.SYS_PATH_VIDEO + localMessage.getMessageCode();
                localMessage.setLocalPath(filePath);
                holderSend.tvVideoSendState.setText("");
                holderSend.tvContent.setVisibility(View.GONE);
                holderSend.ivContent.setVisibility(View.GONE);
                holderSend.rlMessageVoice.setVisibility(View.GONE);
                holderSend.rlVideoContent.setVisibility(View.VISIBLE);
                holderSend.rlMessageLocation.setVisibility(View.GONE);
                holderSend.llMessageSystem.setVisibility(View.GONE);

                //缩略图类型：MINI_KIND FULL_SCREEN_KIND MICRO_KIND
                holderSend.ivVideoPlayResend.setVisibility(View.VISIBLE);

                //SDK处理发送视频时会在图片目录同时生成预览帧文件
                Bitmap miniThumb = BitmapUtil.getImage(BitmapUtil.getBitmapByFilePath(Util.SYS_PATH_IMG + localMessage.getMessageCode() + ".jpg"));

//                Bitmap miniThumb = BitmapUtil.getImage(BitmapUtil.getVideoThumbnail(filePath, 0, 0, MediaStore.Video.Thumbnails.MINI_KIND));
                if (miniThumb != null) {
                    holderSend.ivVideoContent.setImageBitmap(miniThumb);
                }
                holderSend.ivVideoPlayResend.setImageResource(R.drawable.intercom_speechme_start);

//                if (localMessage.getState() == MessageEntity.STATE_RES_DOING || localMessage.getState() == MessageEntity.STATE_SENDING) {
//                    holderSend.tvVideoSendState.setText(R.string.video_send_state_uploading);
//                    holderSend.ivVoicePlayResend.setVisibility(View.GONE);
//                } else
                if (localMessage.getState() == MessageEntity.STATE_RES_FAIL || localMessage.getState() == MessageEntity.STATE_RESULT_FAIL) {
                    holderSend.tvVideoSendState.setText(R.string.video_send_state_fail);
                    holderSend.ivVideoPlayResend.setVisibility(View.VISIBLE);
                    holderSend.ivVideoPlayResend.setImageResource(R.drawable.video_msg_resend);
                }

                holderSend.ivVideoContent.setTag(localMessage);
                holderSend.ivVideoPlayResend.setTag(localMessage);
                break;
            case MessageType.TYPE_RECORD://录音
                if (localMessage.getRecordType() == MessageEntity.RECORD_TYPE_PTT) {
                    holderSend.tvVoicePTT.setVisibility(View.VISIBLE);
                } else {
                    holderSend.tvVoicePTT.setVisibility(View.GONE);
                }
                //设置显示是否在播放
                if (localMessage.isRecordPlaying()) {
                    holderSend.ivVoice.setImageResource(R.drawable.intercom_speechme_stop);
                } else {
                    holderSend.ivVoice.setImageResource(R.drawable.intercom_speechme_start);
                }
                holderSend.tvContent.setVisibility(View.GONE);
                holderSend.ivContent.setVisibility(View.GONE);
                holderSend.rlMessageVoice.setVisibility(View.VISIBLE);
                holderSend.rlVideoContent.setVisibility(View.GONE);
                holderSend.rlMessageLocation.setVisibility(View.GONE);
                holderSend.llMessageSystem.setVisibility(View.GONE);
                holderSend.tvVoiceDurtion.setText(localMessage.getRecordTime() + "\"");

                holderSend.rlMessageVoice.setTag(localMessage);

                break;
            case MessageType.TYPE_LOCATION://位置
                holderSend.tvContent.setVisibility(View.GONE);
                holderSend.ivContent.setVisibility(View.GONE);
                holderSend.rlMessageVoice.setVisibility(View.GONE);
                holderSend.rlVideoContent.setVisibility(View.GONE);
                holderSend.llMessageSystem.setVisibility(View.GONE);
                holderSend.rlMessageLocation.setVisibility(View.VISIBLE);

                String address = localMessage.getAddress();
                if (!TextUtils.isEmpty(address)) {
                    holderSend.tvAddress.setText(address + ":"+localMessage.getLatitude() + ":" +localMessage.getLongtitude());
                }
                holderSend.ivMapView.setTag(localMessage);
                holderSend.tvAddress.setTag(localMessage);

                break;
            case MessageType.TYPE_CHANNEL_ALERT://预定义组呼叫
                Logger.d(TAG,"这里是预定义组呼叫信息");
                break;
            case MessageType.TYPE_FILE://文件
                break;
            default:
                break;
        }
        return convertView;
    }

    /**
     * 显示接收到的信息
     *
     * @param convertView
     * @param localMessage
     * @return
     */
    private View buildMessageItemWithReceive(View convertView, MessageEntity localMessage) {
        ViewHolderReceive holderReceive = null;
        int messageType = localMessage.getType();//消息类型
        if ((convertView == null) || !(convertView.getTag() instanceof ViewHolderReceive)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chatting_item_msg_text_left, null);
            holderReceive = new ViewHolderReceive();
            holderReceive.ViewHolderInit(convertView);
            holderReceive.ivMessageUnread = (ImageView) convertView.findViewById(R.id.iv_message_unread);
            // TODO: findViewById() ImageView report_icon 控件用于显示图片/视频发送过程;
            convertView.setTag(holderReceive);
        }

        holderReceive = (ViewHolderReceive) convertView.getTag();

        //设置点击事件的监听
        holderReceive.ivContent.setOnClickListener(this.onClicklistener);
        holderReceive.rlMessageVoice.setOnClickListener(this.onClicklistener);
        holderReceive.ivVideoContent.setOnClickListener(this.onClicklistener);
        holderReceive.ivMapView.setOnClickListener(this.onClicklistener);
        holderReceive.tvAddress.setOnClickListener(this.onClicklistener);

        //设置长按事件监听
        holderReceive.tvContent.setOnLongClickListener(this.onLongClickListener);
        holderReceive.ivContent.setOnLongClickListener(this.onLongClickListener);
        holderReceive.rlMessageVoice.setOnLongClickListener(this.onLongClickListener);
        holderReceive.ivVideoContent.setOnLongClickListener(this.onLongClickListener);
        holderReceive.ivMapView.setOnLongClickListener(this.onLongClickListener);
        holderReceive.tvAddress.setOnLongClickListener(this.onLongClickListener);
        holderReceive.tvSystemMsgType.setOnLongClickListener(this.onLongClickListener);

        String date = localMessage.getDate();

        if (DateTool.isToday(date)){
            holderReceive.tvSendTime.setText(localMessage.getTime());
        }else {
            holderReceive.tvSendTime.setText(date + " " + localMessage.getTime());
        }

        holderReceive.tvSender.setText(localMessage.getNameFrom());

        //是否为未读消息
        if (localMessage.getState() == MessageEntity.STATE_NEW) {
            holderReceive.ivMessageUnread.setVisibility(View.VISIBLE);
        } else {
            holderReceive.ivMessageUnread.setVisibility(View.GONE);
        }


        switch (messageType) {
            case MessageType.TYPE_SYSTEM:
                holderReceive.llMessageSystem.setVisibility(View.VISIBLE);
                holderReceive.tvContent.setVisibility(View.GONE);
                holderReceive.ivContent.setVisibility(View.GONE);
                holderReceive.rlMessageVoice.setVisibility(View.GONE);
                holderReceive.rlVideoContent.setVisibility(View.GONE);
                holderReceive.rlMessageLocation.setVisibility(View.GONE);
                if (localMessage.getBody().equals(mContext.getString(R.string.talk_call_state_missed_call))
                        || localMessage.getBody().equals(mContext.getString(R.string.talk_call_state_missed_group_call))) {
                    holderReceive.tvSystemMsgType.setText(localMessage.getBody());
                    holderReceive.tvSystemMsgType.setTextColor(Color.RED);
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.call_missed);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());
                    holderReceive.tvSystemMsgType.setCompoundDrawables(drawable,null,null,null);
                } else {
                    //TODO：TEST
                    holderReceive.tvSystemMsgType.setText(localMessage.getBody());
                    holderReceive.tvSystemMsgType.setTextColor(mContext.getResources().getColor(R.color.black));
                    Drawable drawable = mContext.getResources().getDrawable(R.drawable.call_record);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(),drawable.getMinimumHeight());
                    holderReceive.tvSystemMsgType.setCompoundDrawables(drawable,null,null,null);
                }

                holderReceive.tvSystemMsgType.setTag(localMessage);

                break;
            case MessageType.TYPE_TEXT://文本
                holderReceive.ivContent.setVisibility(View.GONE);
                holderReceive.tvContent.setVisibility(View.VISIBLE);
                holderReceive.rlMessageVoice.setVisibility(View.GONE);
                holderReceive.rlVideoContent.setVisibility(View.GONE);
                holderReceive.rlMessageLocation.setVisibility(View.GONE);
                holderReceive.llMessageSystem.setVisibility(View.GONE);
                holderReceive.tvContent.setText(localMessage.getBody());

                holderReceive.tvContent.setTag(localMessage);

                break;
            case MessageType.TYPE_PICTURE://图片
                filePath = Util.SYS_PATH_IMG + localMessage.getMessageCode();
                localMessage.setLocalPath(filePath);

                holderReceive.tvContent.setVisibility(View.GONE);
                holderReceive.ivContent.setVisibility(View.VISIBLE);
                holderReceive.rlMessageVoice.setVisibility(View.GONE);
                holderReceive.rlVideoContent.setVisibility(View.GONE);
                holderReceive.rlMessageLocation.setVisibility(View.GONE);
                holderReceive.llMessageSystem.setVisibility(View.GONE);
                String url = localMessage.getImageUri();

                holderReceive.ivContent.setImageDrawable(null);
                holderReceive.ivContent.setBackgroundResource(0);
                setImageContentLayoutParams(holderReceive.ivContent, 300, 300);

                displayImage(url,holderReceive.ivContent);

                holderReceive.ivContent.setTag(localMessage);

                break;
            case MessageType.TYPE_VIDEO://视频
                filePath = Util.SYS_PATH_VIDEO + localMessage.getMessageCode();
                localMessage.setLocalPath(filePath);

                holderReceive.tvContent.setVisibility(View.GONE);
                holderReceive.ivContent.setVisibility(View.GONE);
                holderReceive.rlMessageVoice.setVisibility(View.GONE);
                holderReceive.rlVideoContent.setVisibility(View.VISIBLE);
                holderReceive.rlMessageLocation.setVisibility(View.GONE);
                holderReceive.llMessageSystem.setVisibility(View.GONE);
                if (localMessage.getState() == MessageEntity.STATE_DOWNLOADING) {
                    holderReceive.tvVideoSendState.setText(R.string.video_down_state_downloading);
                } else if (localMessage.getState() == MessageEntity.STATE_RESULT_FAIL) {
                    holderReceive.tvVideoSendState.setText(R.string.video_down_state_fail);
                } else if (localMessage.getState() == MessageEntity.STATE_RESULT_OK) {
                    //缩略图类型：MINI_KIND FULL_SCREEN_KIND MICRO_KIND
                    Bitmap miniThumb = BitmapUtil.getImage(BitmapUtil.getBitmapByFilePath(Util.SYS_PATH_IMG + localMessage.getMessageCode() + ".jpg"));
//                    Bitmap miniThumb = BitmapUtil.getImage(BitmapUtil.getVideoThumbnail(filePath, 0, 0, MediaStore.Video.Thumbnails.MINI_KIND));
                    holderReceive.tvVideoSendState.setText("");
                    if (miniThumb != null) {
                        holderReceive.ivVideoContent.setImageBitmap(miniThumb);
                    }
                }

                holderReceive.ivVideoContent.setTag(localMessage);

                break;
            case MessageType.TYPE_RECORD://录音
                if (localMessage.getRecordType() == MessageEntity.RECORD_TYPE_PTT) {
                    holderReceive.tvVoicePTT.setVisibility(View.VISIBLE);
                } else {
                    holderReceive.tvVoicePTT.setVisibility(View.GONE);
                }
                //设置显示是否在播放
                if (localMessage.isRecordPlaying()) {
                    holderReceive.ivVoice.setImageResource(R.drawable.intercom_speech_stop);
                } else {
                    holderReceive.ivVoice.setImageResource(R.drawable.intercom_speech_start);
                }
                holderReceive.tvContent.setVisibility(View.GONE);
                holderReceive.ivContent.setVisibility(View.GONE);
                holderReceive.llMessageSystem.setVisibility(View.GONE);
                holderReceive.rlMessageVoice.setVisibility(View.VISIBLE);
                holderReceive.rlVideoContent.setVisibility(View.GONE);
                holderReceive.rlMessageLocation.setVisibility(View.GONE);
                holderReceive.tvVoiceDurtion.setText(localMessage.getRecordTime() + "\"");

                holderReceive.rlMessageVoice.setTag(localMessage);

                break;
            case MessageType.TYPE_LOCATION://位置
                holderReceive.tvContent.setVisibility(View.GONE);
                holderReceive.ivContent.setVisibility(View.GONE);
                holderReceive.rlMessageVoice.setVisibility(View.GONE);
                holderReceive.rlVideoContent.setVisibility(View.GONE);
                holderReceive.llMessageSystem.setVisibility(View.GONE);
                holderReceive.rlMessageLocation.setVisibility(View.VISIBLE);

                String address = localMessage.getAddress();
                if (!TextUtils.isEmpty(address)) {
                    holderReceive.tvAddress.setText(address + ":"+localMessage.getLatitude() + ":" +localMessage.getLongtitude());
                }

                holderReceive.ivMapView.setTag(localMessage);
                holderReceive.tvAddress.setTag(localMessage);

                break;
            case MessageType.TYPE_CHANNEL_ALERT://预定义组呼叫

                break;
            case MessageType.TYPE_FILE://文件
                break;
            default:
                break;
        }

        return convertView;
    }

    /**
     * 加载显示图片
     * @param url
     * @param ivContent
     */
    private void displayImage(String url, ImageView ivContent) {
        imageLoader.displayImage(url, ivContent, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {

            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                float width = 300;
                float height = 300;
                if (bitmap != null) {
                    width = bitmap.getWidth();
                    height = bitmap.getHeight();
                    if ((width / height) > 1f) {
                        setImageContentLayoutParams(view, 300, (int) (height * (300 / width)));
                    } else if (1f > (width / height)) {
                        setImageContentLayoutParams(view,(int) (width * (300 / height)), 300);
                    } else {
                        setImageContentLayoutParams(view, 300, 300);
                    }
                } else {
                    setImageContentLayoutParams(view, (int) width, (int) height);
                }

            }

            @Override
            public void onLoadingCancelled(String s, View view) {

            }
        });
    }

    /**
     * 设置图片空间尺寸
     * @param view
     * @param width
     * @param height
     */
    private void setImageContentLayoutParams(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    abstract class ViewHolder {
        RelativeLayout rlMessageVoice, rlVideoContent, rlMessageLocation, rlMessageContent;
        LinearLayout llMessageSystem;
        TextView tvSendTime, tvSender, tvVoiceDurtion, tvVoicePTT, tvContent, tvVideoSendState, tvAddress, tvSystemMsgType;
        ImageView tvUserPhoto, ivContent, ivVoice, ivMessageUnread, ivVideoContent, ivMapView, ivVideoPlayResend;

        ViewHolder() {
        }

        public void ViewHolderInit(View convertView) {
            rlMessageContent = (RelativeLayout) convertView.findViewById(R.id.message_content);
            llMessageSystem = (LinearLayout) convertView.findViewById(R.id.message_system);
            tvAddress = (TextView) convertView.findViewById(R.id.tv_location_address);
            rlMessageLocation = (RelativeLayout) convertView.findViewById(R.id.message_location);
            ivMapView = (ImageView) convertView.findViewById(R.id.iv_location);
            rlMessageVoice = (RelativeLayout) convertView.findViewById(R.id.message_voice);
            ivVoice = (ImageView) convertView.findViewById(R.id.iv_voice_message);
            tvVoiceDurtion = (TextView) convertView.findViewById(R.id.message_voice_duration);
            tvVoicePTT = (TextView) convertView.findViewById(R.id.message_voice_ptt);
            tvSendTime = (TextView) convertView.findViewById(R.id.chatting_messagedate);
            tvSender = (TextView) convertView.findViewById(R.id.chatting_username);
            tvUserPhoto = (ImageView) convertView.findViewById(R.id.chatting_userheader);
            tvContent = (TextView) convertView.findViewById(R.id.tv_content);
            ivContent = (ImageView) convertView.findViewById(R.id.iv_content);
            ivVideoContent = (ImageView) convertView.findViewById(R.id.video_contant);
            ivVideoPlayResend = (ImageView) convertView.findViewById(R.id.iv_video_play_resend);
            rlVideoContent = (RelativeLayout) convertView.findViewById(R.id.rl_video_contant);
            tvVideoSendState = (TextView) convertView.findViewById(R.id.tv_videomessage_send_state);
            tvSystemMsgType = (TextView) convertView.findViewById(R.id.tv_system_msg_type);
        }
    }

    protected class ViewHolderSend extends ViewHolder {
        ImageView ivSendState;
        protected ViewHolderSend(View convertView) {
            super();
            ivSendState = (ImageView) convertView.findViewById(R.id.iv_send_state);
        }
    }

    protected class ViewHolderReceive extends ViewHolder {

        protected ViewHolderReceive() {
            super();
        }
    }
}
