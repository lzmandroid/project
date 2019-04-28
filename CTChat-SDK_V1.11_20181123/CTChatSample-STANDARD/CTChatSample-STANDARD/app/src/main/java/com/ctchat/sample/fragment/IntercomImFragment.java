package com.ctchat.sample.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;


import com.ctchat.sdk.basemodule.logger.Logger;
import com.ctchat.sdk.basemodule.util.ToastUtil;
import com.ctchat.sample.R;
import com.ctchat.sdk.ptt.tool.chatIM.IMManager;
import com.ctchat.sdk.ptt.tool.chatIM.MessageType;
import com.ctchat.sdk.ptt.tool.entity.ChannelEntity;
import com.ctchat.sdk.ptt.tool.entity.MessageEntity;
import com.ctchat.sdk.ptt.tool.entity.SessionEntity;
import com.ctchat.sample.IntercomActivity;
import com.ctchat.sample.VideoCameraActivity;
import com.ctchat.sample.VideoMessagePlayActivity;
import com.ctchat.sample.adapter.SessionMessageAdapter;
import com.ctchat.sample.application.WeApplication;
import com.ctchat.sample.presenter.IntercomImPresent;
import com.ctchat.sample.presenter.IntercomImPresentImpl;
import com.ctchat.sample.util.BitmapUtil;
import com.ctchat.sample.util.FileUtil;
import com.ctchat.sample.util.TimeUtil;
import com.ctchat.sample.util.Util;
import com.ctchat.sample.view.IntercomImView;
import com.ctchat.sample.widget.PullToRefreshView;

import java.io.File;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * 对讲即时聊天
 */
public class IntercomImFragment extends BaseFragment implements IntercomImView, View.OnClickListener, View.OnLongClickListener, View.OnTouchListener, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = "IntercomImFragment";
    private IntercomImPresent presenter;
    private Context mContext;
    private View view, layoutRecord;
    private ListView lvChattingMsg;
    private CheckBox ckInputModel, imMsgMore, imVioceMore;
    private EditText etMsgInput;
    private Button btnSend, btnVoice;
    private TextView tvVoiceTips, tvPopDelAll, tvPopDel, tvPopResend, tvPopCopy;
    private IntercomActivity intercomActivity;
    private PullToRefreshView pullToRefreshView;

    private Boolean isVoiceModel = false, isOpen = false;
    private SessionMessageAdapter mAdapater;

    private String mediaDir = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String imgName = "";

    private SessionEntity sessionEntity;
    private MessageEntity voiceMessage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.i(TAG, "IntercomImFragment onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Logger.i(TAG, "IntercomImFragment onCreateView");
        view = inflater.inflate(R.layout.fragment_intercom_im, container, false);
        mContext = getActivity();
        presenter = new IntercomImPresentImpl(this);
        initView(view);
        initData();
        etMsgInput.clearFocus();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        etMsgInput.clearFocus();
        Logger.i(TAG, "IntercomImFragment onResume");
        presenter.onResume();

        if (sessionEntity != null && sessionEntity.getMessageUnreadCount() > 0) {
            notifyDataSetChanged(true);
            //清除未读消息
            intercomActivity.cleanMessageUnreadCount();
        } else {
            notifyDataSetChanged(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void initData() {
        intercomActivity = (IntercomActivity) getActivity();
        sessionEntity = intercomActivity.getSessionEntity();

        //获取指定数量的历史消息
        IMManager.getInstance().setMessageListNumberMax(Util.MESSAGE_UPDATE_NUM);
        IMManager.getInstance().messageListMoreLoad(sessionEntity.getSessionId());

        // 设置数据
        setAdapter();
    }

    @Override
    public void initView(View view) {
        pullToRefreshView = (PullToRefreshView) view.findViewById(R.id.message_pull_to_refresh_view);
        layoutRecord = view.findViewById(R.id.layout_record);
        tvVoiceTips = (TextView) view.findViewById(R.id.tv_voice_tips);
        lvChattingMsg = (ListView) view.findViewById(R.id.lv_chat_message);
        ckInputModel = (CheckBox) view.findViewById(R.id.cb_input_model);
        etMsgInput = (EditText) view.findViewById(R.id.et_input);
        imMsgMore = (CheckBox) view.findViewById(R.id.cb_more_msg);
        imVioceMore = (CheckBox) view.findViewById(R.id.cb_more_voice);
        btnSend = (Button) view.findViewById(R.id.btn_send);
        btnVoice = (Button) view.findViewById(R.id.btn_voice);

        ckInputModel.setOnCheckedChangeListener(this);
        imMsgMore.setOnClickListener(this);
        imVioceMore.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        lvChattingMsg.setOnTouchListener(this);

        btnVoice.setOnTouchListener(this);

        view.findViewById(R.id.ll_ibtn_local_upload).setOnClickListener(this);
        view.findViewById(R.id.ll_ibtn_camera).setOnClickListener(this);
        view.findViewById(R.id.ll_ibtn_video).setOnClickListener(this);
        view.findViewById(R.id.ll_ibtn_location).setOnClickListener(this);

        pullToRefreshView.setOnHeaderRefreshListener(new PullToRefreshView.OnHeaderRefreshListener() {
            @Override
            public void onHeaderRefresh(PullToRefreshView view) {
                IMManager.getInstance().messageListMoreLoad(sessionEntity.getSessionId());
            }
        });

        etMsgInput.setOnFocusChangeListener(new android.view.View.
                OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // 此处为得到焦点时的处理内容
                    moreOperationPanel(false);
                    imMsgMore.setChecked(false);
                }
            }
        });


        imMsgMore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_im_funcbtn);
                    imMsgMore.startAnimation(animation);
                } else {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_im_funcbtn_disable);
                    imMsgMore.startAnimation(animation);
                }
            }
        });

        imVioceMore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_im_funcbtn);
                    imVioceMore.startAnimation(animation);
                } else {
                    Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_im_funcbtn_disable);
                    imVioceMore.startAnimation(animation);
                }
            }
        });

        //根据输入框是否存在文本设置发送按钮使能状态
        btnSend.setEnabled(false);
        etMsgInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    btnSend.setEnabled(true);
                } else {
                    btnSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Util.UPDATE_HISTORY_MESSAGES:
                    pullToRefreshView.onHeaderRefreshComplete();
                    break;
            }
        }
    };

    /**
     * 设置数据显示适配器
     */
    private void setAdapter() {
        mAdapater = new SessionMessageAdapter(intercomActivity, this, this);
        mAdapater.setCurrentSession(sessionEntity);
        lvChattingMsg.setAdapter(mAdapater);
        lvChattingMsg.setSelection(mAdapater.getCount());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_more_msg:
                moreOperationPanel(!isOpen);
                break;
            case R.id.cb_more_voice:
                moreOperationPanel(!isOpen);
                break;
            case R.id.btn_send:
                String msg = etMsgInput.getText().toString();
                sendTextMessage(msg);
                break;
            case R.id.ll_ibtn_local_upload:
                selectPic();
                break;
            case R.id.ll_ibtn_camera:
                takePhoto();
                break;
            case R.id.ll_ibtn_video:
                MovieRecorder();
                break;
            case R.id.ll_ibtn_location:
                takeLocation();
                break;
            case R.id.iv_content:
                //显示图片
                MessageEntity imageMessage = (MessageEntity) v.getTag();
                IMManager.getInstance().downloadImage(imageMessage);
                break;
            case R.id.message_voice:
                //播放语音消息
                voiceMessage = (MessageEntity) v.getTag();
                if (voiceMessage.isRecordPlaying()) {
                    stopPlayRecordMessage();
                } else {
                    startPlayRecordMessage(voiceMessage);
                }
                break;
            case R.id.iv_video_play_resend:
            case R.id.video_contant:
                //播放视频
                MessageEntity videoMessage = (MessageEntity) v.getTag();
                if (videoMessage.getState() == MessageEntity.STATE_RES_FAIL || videoMessage.getState() == MessageEntity.STATE_RESULT_FAIL) {
                    //上传失败，点击重传
                    resendMessage(videoMessage);
                } else {
                    Intent playVideoIntent = new Intent(intercomActivity, VideoMessagePlayActivity.class);
                    playVideoIntent.putExtra("videoPath", videoMessage.getLocalPath());
                    startActivity(playVideoIntent);
                }
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.lv_chat_message:
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    keyboardManage(false);
                }
                return false;
            case R.id.btn_voice:
                return voiceTouchEvent(v, event);
            default:
                break;
        }
        return false;
    }

    /**
     * 录音按钮触摸事件处理
     *
     * @param v
     * @param event
     * @return
     */
    private boolean voiceTouchEvent(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://按下
//                Logger.d(TAG,"ACTION_DOWN");
//                Logger.d(TAG,"geyX:"+ event.getX());
//                Logger.d(TAG,"getY:"+ event.getY());
                v.setPressed(true);
                // 设置viewPage不可滑动

                layoutRecord.setVisibility(View.VISIBLE);
                btnVoice.setText(R.string.up_end);
                tvVoiceTips.setText(getString(R.string.voice_up_tips));
                tvVoiceTips.setTextColor(Color.WHITE);
                //开始录音
                startRecord();
                return true;
            case MotionEvent.ACTION_MOVE://滑动手指
//                Logger.d(TAG,"ACTION_MOVE");
//                Logger.d(TAG,"geyX:"+ event.getX());
//                Logger.d(TAG,"getY:"+ event.getY());
                if (event.getY() < 0) {
                    tvVoiceTips.setText(getString(R.string.voice_cancel_tips));
                    tvVoiceTips.setTextColor(Color.RED);
                } else {
                    tvVoiceTips.setText(getString(R.string.voice_up_tips));
                    tvVoiceTips.setTextColor(Color.WHITE);
                }
                return true;

            case MotionEvent.ACTION_CANCEL:
                Logger.d(TAG, "2 ACTION_CANCEL");
            case MotionEvent.ACTION_UP://松开手指
//                Logger.d(TAG,"ACTION_UP");
//                Logger.d(TAG,"geyX:"+ event.getX());
//                Logger.d(TAG,"getY:"+ event.getY());
                v.setPressed(false);
                // 恢复viewPage滑动

                layoutRecord.setVisibility(View.GONE);
                btnVoice.setText(R.string.down_tlak);
                try {
                    if (event.getY() < 0) {
                        //放弃录音
                        Logger.i(TAG, "放弃发送语音");
                        stopRecord(true);
                    } else {
                        //停止录音发送
                        stopRecord(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            default:
                Logger.d(TAG, "Event:" + event.getAction());
                return false;
        }
    }

    /**
     * 消息重发
     *
     * @param message
     */
    private void resendMessage(MessageEntity message) {
        switch (message.getType()) {
            case MessageType.TYPE_TEXT:
                messageRemove(message);
                sendMessageToSession(message);
                break;
            case MessageType.TYPE_PICTURE://图片
                byte[] bytesImg = message.getImage();

                if (bytesImg == null) {
                    File f = new File(message.getLocalPath());
                    bytesImg = BitmapUtil.getImageByFile(f);
                }
                makeImageMessage(bytesImg,message.getLocalPath());
                messageRemove(message);
                break;
            case MessageType.TYPE_VIDEO://视频
                messageRemove(message);
                //视频重传
                String videoPath = message.getLocalPath();
                if (!TextUtils.isEmpty(videoPath)) {
                    byte[] bytes = FileUtil.getByte(videoPath);
                    makeVedioMessage(videoPath, bytes);
                }
                break;
            case MessageType.TYPE_RECORD://录音
                resendRecordMessageBySession(message);
                break;
            case MessageType.TYPE_LOCATION://位置
                break;
        }
        ToastUtil.shortShow(WeApplication.getInstance(), R.string.message_resend);
    }

    /**
     * 删除消息记录
     *
     * @param message
     */
    private void messageRemove(MessageEntity message) {
        String sessionCode = message.getSessionId();
        String msgCode = message.getMessageCode();
        IMManager.getInstance().messageRemove(sessionCode, msgCode);
        notifyDataSetChanged(false);
        ToastUtil.shortShow(WeApplication.getInstance(), R.string.delete_success);
    }

    /**
     * 删除所有消息记录
     */
    private void messageRemoveAll() {
        IMManager.getInstance().messageRemoveAll(sessionEntity.getSessionId());
        sessionEntity.setMessageLast(null);
        notifyDataSetChanged(false);
        ToastUtil.shortShow(WeApplication.getInstance(), R.string.delete_all_success);
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            // SessionMessageAdapter Item项 控件长按事件
            case R.id.tv_content:
            case R.id.iv_content:
            case R.id.message_voice:
            case R.id.video_contant:
            case R.id.iv_location:
            case R.id.tv_location_address:
            case R.id.tv_system_msg_type:
                initPopupWindow((MessageEntity) v.getTag());
                break;
        }
        return false;
    }

    /**
     * 从相册中选取图片
     */
    private void selectPic() {
        // 激活系统图库，选择一张图片
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_GALLERY
        startActivityForResult(intent, Util.PHOTO_REQUEST_GALLERY);
    }

    /**
     * 使用系统相机拍照
     */
    private void takePhoto() {
        //激活相机
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imgName = "img" + TimeUtil.getCurrTimeData() + ".jpg";
        Logger.i(TAG, "imageName " + imgName);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(new File(mediaDir, imgName)));
        startActivityForResult(intent, Util.PHOTO_REQUEST_SHOOTING);
    }

    /**
     * 录制视频
     */
    private void MovieRecorder() {
        //激活视频拍摄
        Intent intent = new Intent(intercomActivity, VideoCameraActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(VideoCameraActivity.ISTIMING, true);
        intent.putExtras(bundle);
        startActivityForResult(intent, Util.PHOTO_REQUEST_VIDEO);
    }

    /**
     * 获取位置信息
     */
    private void takeLocation() {
        makeLocationMessage(32.125466, 120.123456, "中国北京");
    }


    /**
     * *****************************各种类型消息发送功能*****************************
     */

    /**
     * 拼装文本信息
     *
     * @param msg
     */
    private void sendTextMessage(String msg) {
        if (!msg.trim().equals("")) {
            MessageEntity message = new MessageEntity();
            message.setType(MessageType.TYPE_TEXT);
            message.setBody(msg);
            message.setDate(System.currentTimeMillis() + "");
            etMsgInput.setText("");
            //发送消息给会话
            if (sessionEntity.getSessionType() == SessionEntity.TYPE_DIALOG) {
                sendMessageToSession(message);
            } else {
                sendMessageToChannel(message);
            }
        } else {
            ToastUtil.shortShow(intercomActivity, R.string.empty_content);
        }

    }

    /**
     * 拼装图片信息
     *
     * @param bytes
     */
    private boolean makeImageMessage(byte[] bytes,String localPath) {
        MessageEntity message = new MessageEntity();
        message.setType(MessageType.TYPE_PICTURE);
        message.setImage(bytes);
        message.setLocalPath(localPath);
        message.setDate(System.currentTimeMillis() + "");
        // 发送图片消息
        if (sessionEntity.getSessionType() == SessionEntity.TYPE_DIALOG) {
            return sendImageToSession(message);
        } else {
            return sendImageToChannel(message);
        }
    }

    /**
     * 拼装视频信息
     *
     * @param bytes
     */
    private boolean makeVedioMessage(String filepath, byte[] bytes) {
        Logger.i(TAG, "========================== makeVedioMessage ====================================");
        if (bytes != null && bytes.length != 0) {
            MessageEntity message = new MessageEntity();
            message.setType(MessageType.TYPE_VIDEO);
            message.setVideoBytes(bytes);
            message.setDate(System.currentTimeMillis() + "");
            message.setLocalPath(filepath);
            // 发送视频消息
            return sendVideoToSession(message);
        }

        return false;
    }

    /**
     * 拼装位置信息
     *
     * @param longitude
     * @param latitude
     */
    private void makeLocationMessage(double longitude, double latitude, String address) {
        sendLocationToSession(latitude, longitude, address);
    }

    /**
     * 根据session发送消息
     */
    private void sendMessageToSession(MessageEntity message) {
        Logger.i(TAG, "========================== sendMessageToSession ====================================");
        this.presenter.sendMessageToSession(sessionEntity, message);
    }

    /**
     * 根据channel发送消息
     */
    private void sendMessageToChannel(MessageEntity message) {
        // channel获取
        ChannelEntity channel = sessionEntity.getChannel();
        this.presenter.sendMessionToChannel(channel, message);
    }

    /**
     * 发送图片消息
     */
    private boolean sendImageToSession(MessageEntity message) {
        return presenter.sendImageToSession(sessionEntity, message);
    }

    /**
     * 发送图片消息
     */
    private boolean sendImageToChannel(MessageEntity message) {
        ChannelEntity channel = sessionEntity.getChannel();
        return presenter.sendImageToChannel(channel, message);
    }

    /**
     * 发送视频消息
     */
    private boolean sendVideoToSession(MessageEntity message) {
        return presenter.sendVideoToSession(sessionEntity, message);
    }


    /**
     * 发送位置信息
     *
     * @param latitude  纬度
     * @param longitude 经度
     */
    private void sendLocationToSession(double latitude, double longitude, String address) {
        this.presenter.sendLocationToSession(sessionEntity, latitude, longitude, address);
    }

    /**
     * 重发语音消息
     *
     * @param mseeage
     */
    private void resendRecordMessageBySession(MessageEntity mseeage) {
        presenter.resendRecordMessageBySession(mseeage);
    }

    /**
     * 消息通知事件
     *
     * @param list
     */
    @Override
    public void messageNotify(final List<MessageEntity> list) {
        //更新未读消息数量
        intercomActivity.undateUnreadCount();

        Logger.i(TAG, "========================== messageNotify list====================================");
        intercomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO:更新消息显示
                Logger.i(TAG, "Incoming message list");
                notifyDataSetChanged(true);
            }
        });
    }

    @Override
    public void messageNotify(final MessageEntity message) {
        //更新未读消息数量
        intercomActivity.undateUnreadCount();

        Logger.i(TAG, "========================== messageNotify ====================================");

        intercomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO:更新消息显示
                Logger.i(TAG, "Incoming message");
                Logger.i(TAG, IntercomImFragment.this.hashCode()
                        + " messageNotify : " + "msgCode :"
                        + message.getMessageCode() + " type: "
                        + message.getType() + " isSent:"
                        + (message.isSent() ? "true" : "false") + " msg state : "
                        + message.getState() + "   SessionCode: "
                        + message.getSessionId() + "  byte[] : "
                        + message.getImage() + "   图片信息 ："
                        + message.getImageUri() + " 文本： "
                        + message.getBody());

                notifyDataSetChanged(true);
            }
        });

    }

    /**
     * 发送消息的回调事件
     */
    @Override
    public void onMessageOutgoingSent(final MessageEntity message) {

        Logger.i(TAG, "========================== onMessageOutgoingSent ====================================:" + sessionEntity.getSessionId());
        intercomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.i(TAG, IntercomImFragment.this.hashCode()
                        + " onMessageOutgoingSent isSent: " + "msgCode :"
                        + message.getMessageCode() + " type: "
                        + message.getType() + " isSent: "
                        + (message.isSent() ? "true " : "false") + " msg state : "
                        + message.getState() + "   SessionId: "
                        + message.getSessionId() + "  byte[] : "
                        + message.getImage() + "   图片信息："
                        + message.getImageUri());
                notifyDataSetChanged(true);
            }
        });
    }

    /**
     * 消息下载完成
     */
    @Override
    public void onMessageUpdated(final MessageEntity message) {
        Logger.i(TAG, "========================== updated ====================================");
        intercomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.i(TAG, IntercomImFragment.this.hashCode()
                        + " onMessageUpdated : " + "msgCode :"
                        + message.getMessageCode() + " type: "
                        + message.getType() + " isSent:"
                        + (message.isSent() ? "true" : "false") + " msg state : "
                        + message.getState() + "   SessionCode: "
                        + message.getSessionId() + "  byte[] : "
                        + message.getImage() + "   图片信息 ："
                        + message.getImageUri());

                notifyDataSetChanged(true);

            }
        });
    }

    @Override
    public void onMessageRecordStart() {
        intercomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //TODO 显示录音音量动画
                Logger.i(TAG, "onMessageRecordStart");
            }
        });
    }

    /**
     * 如果录音成功则返回录音的秒数;
     * 其它的可能返回值:
     * -2:未满足最小秒数(1 秒以上)
     * -3:录音被取消
     * 特别说明:录音时间长度设定为最小 1 秒钟,最大 60 秒钟
     *
     * @param seconds
     * @param msgCode
     */
    @Override
    public void onMessageRecordStop(final int seconds, final String msgCode) {
        intercomActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Logger.i(TAG, "onMessageRecordStop seconds = " + seconds + "    msgCode: " + msgCode);
            }
        });
    }

    /**
     * 发送语音消息
     */
    private void startRecord() {
        this.presenter.startRecordMessageToSession(sessionEntity);
    }

    private void stopRecord(boolean isCancel) {
        this.presenter.stopRecordMessage(isCancel);
    }

    /**
     * 播放语音消息
     */
    private void startPlayRecordMessage(MessageEntity message) {
        this.presenter.startPlayRecordMessage(message);
    }

    private void stopPlayRecordMessage() {
        this.presenter.stopPlayRecordMessage();
    }

    /**
     * 播放语音开始的回调监听事件
     *
     * @param msgCode
     * @param resId
     */
    @Override
    public void onMessageRecordPlayStart(String msgCode, String resId) {
        Logger.i(TAG, "========================== onMessageRecordPlayStart ===========================");
        if (voiceMessage != null) {
            voiceMessage.setState(0);
            notifyDataSetChanged(false);
        }
        intercomActivity.updateLatestPttRecordStatus();
    }

    /**
     * 播放语音结束的回调监听事件
     *
     * @param msgCode
     * @param resId
     */
    @Override
    public void onMessageRecordPlayStop(String msgCode, String resId) {
        Logger.i(TAG, "========================== onMessageRecordPlayStop ============================");
        notifyDataSetChanged(false);
        intercomActivity.updateLatestPttRecordStatus();
    }

    /**
     * 获取所有历史消息的回调监听事件
     *
     * @param s
     * @param messageEntityList
     */
    @Override
    public void onMessageListLoad(String s, List<MessageEntity> messageEntityList) {
        Logger.i(TAG, "========================== onMessageListLoad ==================================");
        Message msg = new Message();
        msg.what = Util.UPDATE_HISTORY_MESSAGES;
        mHandler.sendMessage(msg);
        notifyDataSetChanged(false);
    }

    /**
     * PTT语音回调事件
     *
     * @param session
     * @param message
     * @param msgCode
     * @param resId
     */
    @Override
    public void onMessagePttRecord(SessionEntity session, MessageEntity message, String msgCode, String resId) {
        if (session.getSessionId().equals(sessionEntity.getSessionId())) {
            Logger.i(TAG, "========================== onMessagePttRecord =================================");
            Logger.i(TAG, "=========Message: " + message);
            intercomActivity.updateLastPttRecord();
            notifyDataSetChanged(true);
        }
    }

    /**
     * 播放 最近的一条ptt语音记录
     *
     * @param pttMessage
     */
    public void playLatestPttRecord(MessageEntity pttMessage) {
        startPlayRecordMessage(pttMessage);
    }

    /**
     * t停止 最近的一条ptt语音记录
     */
    public void stopLatestPttRecord() {
        stopPlayRecordMessage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Util.PHOTO_REQUEST_GALLERY:// 相册返回
                    if (data != null) {
                        // 根据uri获取图片的全路径
                        Uri uri = data.getData();
                        Logger.d(TAG, "uri = " + uri);
                        String path = BitmapUtil.getImagePathByUri(uri);
                        byte[] bytes = BitmapUtil.getImageByPath(path);
                        Logger.d(TAG, "bytes = " + bytes);
                        if (bytes == null || (false == makeImageMessage(bytes,path))) {
                            ToastUtil.shortShow(WeApplication.getInstance(), R.string.invalid_file);
                        }
                    }
                    break;
                case Util.PHOTO_REQUEST_SHOOTING://相机拍照返回
                    //获取图片文件
                    String path = mediaDir + File.separator + this.imgName;
                    File f = new File(path);
                    byte[] bytesImg = BitmapUtil.getImageByFile(f);
                    if (bytesImg == null || (false == makeImageMessage(bytesImg,path))) {
                        ToastUtil.shortShow(WeApplication.getInstance(), R.string.invalid_file);
                    }
                    break;
                case Util.PHOTO_REQUEST_VIDEO:// 录像返回
                    if (data.hasExtra("videoPath")) {
                        String videoPath = data.getStringExtra("videoPath");
                        if (!TextUtils.isEmpty(videoPath)) {
                            byte[] bytesVideo = FileUtil.getByte(videoPath);
                            makeVedioMessage(videoPath, bytesVideo);

                            //删除临时文件
                            File file = new File(videoPath);
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                    }
                    break;
                case Util.TAKE_LOCATION:// 位置返回
                    if (data != null) {
                        double longitude = data.getExtras().getDouble(Util.LONGTITUDE);
                        double latitude = data.getExtras().getDouble(Util.LATITUDE);
                        String address = data.getExtras().getString(Util.ADDRESS);
                        makeLocationMessage(longitude, latitude, address);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 刷新adapter
     *
     * @param isSetSelection 是否定位到最后
     */
    public void notifyDataSetChanged(boolean isSetSelection) {
        if (mAdapater != null) {
            mAdapater.notifyDataSetChanged();
            if (isSetSelection) {
                lvChattingMsg.setSelection(mAdapater.getCount());
            }
        }
    }

    /**
     * 创建PopupWindow
     */
    protected void initPopupWindow(final MessageEntity messageEntity) {
        //获取自定义布局文件activity_pop_left.xml 布局文件
        final View popipWindow_view = intercomActivity.getLayoutInflater().inflate(R.layout.popupwindow_menu, null, false);
        //创建Popupwindow 实例，200，LayoutParams.MATCH_PARENT 分别是宽高
        final PopupWindow popupWindow = new PopupWindow(popipWindow_view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
        tvPopDelAll = (TextView) popipWindow_view.findViewById(R.id.pop_del_all);
        tvPopDel = (TextView) popipWindow_view.findViewById(R.id.pop_del);
        tvPopResend = (TextView) popipWindow_view.findViewById(R.id.pop_resend);
        tvPopCopy = (TextView) popipWindow_view.findViewById(R.id.pop_copy);

        tvPopDelAll.setOnClickListener(new View.OnClickListener() {//删除全部
            @Override
            public void onClick(View v) {
                messageRemoveAll();
                popupWindow.dismiss();
            }
        });
        tvPopDel.setOnClickListener(new View.OnClickListener() {//删除
            @Override
            public void onClick(View v) {
                messageRemove(messageEntity);
                popupWindow.dismiss();
            }
        });
        tvPopResend.setOnClickListener(new View.OnClickListener() {//重发
            @Override
            public void onClick(View v) {
                resendMessage(messageEntity);
                popupWindow.dismiss();
            }
        });
        tvPopCopy.setOnClickListener(new View.OnClickListener() {//复制
            @Override
            public void onClick(View v) {
                if (messageEntity.getType() == MessageType.TYPE_TEXT) {
                    setClipboard(messageEntity.getBody());
                }
                popupWindow.dismiss();
            }
        });

        tvPopDelAll.setTag(messageEntity);
        tvPopDel.setTag(messageEntity);
        tvPopCopy.setTag(messageEntity);

        if (messageEntity.getType() == MessageType.TYPE_TEXT) {
            tvPopCopy.setVisibility(View.VISIBLE);
        } else {
            tvPopCopy.setVisibility(View.GONE);
        }

        //自己还未发送成功的消息允许重发
        int state = messageEntity.getState();

        if (messageEntity.isSelf(mContext) && state != MessageEntity.STATE_RESULT_OK) {
            tvPopResend.setVisibility(View.VISIBLE);
        } else {
            tvPopResend.setVisibility(View.GONE);
        }

        popipWindow_view.setFocusable(true);
        popipWindow_view.setFocusableInTouchMode(true);
        popipWindow_view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                return true;
            }
        });

        //点击其他地方消失
        popipWindow_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (popipWindow_view != null && popipWindow_view.isShown()) {
                    popupWindow.dismiss();
                }
                return false;
            }
        });

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

    }

    /**
     * 复制
     *
     * @param text
     */
    public void setClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) intercomActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clipData);
        ToastUtil.shortShow(WeApplication.getInstance(), R.string.copy_right);
    }

    /**
     * ************************************* 底部功能面板 *************************************
     */

    /**
     * 设置底部更多功能面板的显示
     *
     * @param state
     */
    public void moreOperationPanel(Boolean state) {
        if (state) {
            // 隐藏软键盘
            keyboardManage(false);
            view.findViewById(R.id.chat_instant_message_more).setVisibility(View.VISIBLE);
            isOpen = true;
        } else {
            isOpen = false;
            view.findViewById(R.id.chat_instant_message_more).setVisibility(View.GONE);

            if (intercomActivity.isImFragment() && !isVoiceModel) {
                etMsgInput.requestFocus();
                keyboardManage(true);
            }
        }
    }

    /**
     * 设置语音和文字输入模式的切换
     */
    private void setModel() {
        if (isVoiceModel) {
            view.findViewById(R.id.ll_chat_model_voice).setVisibility(View.GONE);
            view.findViewById(R.id.ll_chat_model_msg).setVisibility(View.VISIBLE);
            isVoiceModel = false;
            imVioceMore.setChecked(false);
            // 显示软键盘
            keyboardManage(true);
        } else {
            etMsgInput.clearFocus();
            view.findViewById(R.id.ll_chat_model_msg).setVisibility(View.GONE);
            view.findViewById(R.id.ll_chat_model_voice).setVisibility(View.VISIBLE);
            isVoiceModel = true;
            imMsgMore.setChecked(false);
            // 隐藏软键盘
            keyboardManage(false);
        }
    }

    /**
     * 显示/隐藏软键盘
     *
     * @param state
     */
    public void keyboardManage(Boolean state) {
        if (etMsgInput == null) {
            return;
        }
        if (state) {
            // 显示软键盘
            etMsgInput.setFocusable(true);
            etMsgInput.setFocusableInTouchMode(true);
            etMsgInput.requestFocus();
            InputMethodManager inputManager = (InputMethodManager) etMsgInput.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.showSoftInput(etMsgInput, 0);
            lvChattingMsg.setSelection(mAdapater.getCount());
        } else {
            // 隐藏软键盘
            etMsgInput.clearFocus();//取消焦点
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etMsgInput.getWindowToken(), 0);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_input_model:
                moreOperationPanel(false);
                setModel();
                break;
        }
    }
}