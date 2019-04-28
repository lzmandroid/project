package com.ctchat.sample.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ctchat.sample.R;

public class MemberCountView extends LinearLayout {
    private Context context;
    private TextView tvLeftBracket;
    private TextView tvLeftMemberCount;
    private TextView tvMiddleMemberCount;
    private TextView tvRightMemberCount;
    private TextView tvRightBracket;

    public MemberCountView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public MemberCountView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    public TextView getTvLeftBracket() {
        return tvLeftBracket;
    }

    public TextView getTvLeftMemberCount() {
        return tvLeftMemberCount;
    }

    public TextView getTvMiddleMemberCount() {
        return tvMiddleMemberCount;
    }

    public TextView getTvRightMemberCount() {
        return tvRightMemberCount;
    }

    public TextView getTvRightBracket() {
        return tvRightBracket;
    }

    private void initView() {
        View view = LayoutInflater.from(context).inflate(R.layout.member_count_layout, this);
        tvLeftBracket = (TextView) view.findViewById(R.id.tv_left_bracket);
        tvLeftMemberCount = (TextView) view.findViewById(R.id.tv_left_member_count);
        tvMiddleMemberCount = (TextView) view.findViewById(R.id.tv_middle_member_count);
        tvRightMemberCount = (TextView) view.findViewById(R.id.tv_right_member_count);
        tvRightBracket = (TextView) view.findViewById(R.id.tv_right_bracket);
    }
}
