package com.ctchat.sample.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.ctchat.sample.R;


public class SingleChoiceDialog extends Dialog {

    public SingleChoiceDialog(Context context) {
        super(context);
    }

    public SingleChoiceDialog(Context context, int theme) {
        super(context, theme);
    }

    public static class Builder {
        private Context context;
        private String title;
        private String confirmMessage;
        private View view;
        private String [] mItems;
        private AdapterView.OnItemClickListener onItemClickListener;
        private SingleAdapter singleAdapter;
        private int currIndex;

        private OnClickListener onConfirmListener;

        public Builder (Context context) {
            this.context = context;
        }

        //使用资源设置对话框标题信息
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        public int getSelectedIndex() {
            return currIndex;
        }

        public Builder setItems(String[] item, int position) {
            this.mItems = item;
            currIndex = position;
            singleAdapter = new SingleAdapter(mItems, currIndex, context );
            return this;
        }

        public Builder setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
            return this;
        }

        //设置确定按钮事件和文本
        public Builder setConfirmButton(
                int confirmButtonText, OnClickListener listener) {
            this.confirmMessage = (String) context.getText(confirmButtonText);
            this.onConfirmListener = listener;
            return this;
        }


        //创建自定义的对话框
        public SingleChoiceDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 实例化自定义的对话框主题
            final SingleChoiceDialog dialog = new SingleChoiceDialog(context, R.style.style_custom_dialog);
            View layout = inflater.inflate(R.layout.dialog_single_choice, null);
            dialog.addContentView(layout,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            // 设置对话框标题
            ((TextView) layout.findViewById(R.id.tv_single_title)).setText(title);
            ListView lvItems = (ListView) layout.findViewById(R.id.lv_single_choice_items);
            lvItems.setAdapter(singleAdapter);
            lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    currIndex = position;
                    singleAdapter.setSelectedItem(position);
                    onItemClickListener.onItemClick(parent, view, position, id);

                }
            });
            // 设置确定按钮事件和文本
            if (confirmMessage != null) {
                Button cfmButton = ((Button) layout.findViewById(R.id.btn_single_cancel));
                cfmButton.setText(confirmMessage);

                if (onConfirmListener != null) {
                    cfmButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            //EditText文本信息的设置
                            onConfirmListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                layout.findViewById(R.id.btn_single_cancel).setVisibility(View.GONE);
            }

            dialog.setContentView(layout);
            return dialog;
        }

    }

    public static class SingleAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{

        private String[] mObjects;
        private int mSelectedItem = 0;
        private Context context;

        public SingleAdapter(int mSelectedItem, Context context) {
            this.mSelectedItem = mSelectedItem;
            this.context = context;
        }

        public SingleAdapter(String[] mObjects, int mSelectedItem, Context context) {
            if (mObjects != null) {
                this.mObjects = mObjects;
            }
            this.mSelectedItem = mSelectedItem;
            this.context = context;
        }

        public void setSelectedItem(int selectedItem) {
           if (selectedItem >= 0 && selectedItem <= mObjects.length) {
               this.mSelectedItem = selectedItem;
               notifyDataSetChanged();
           }
        }

        public int getmSelectedItem() {
            return mSelectedItem;
        }

        public void clear() {
            mObjects = null;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mObjects.length;
        }

        @Override
        public Object getItem(int position) {
            return mObjects[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_single_choice_dialog, null);
                viewHolder.tvChoice = (TextView) convertView.findViewById(R.id.tv_choice_items);
                viewHolder.cbChoice = (CheckBox) convertView.findViewById(R.id.cb_single_choice);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (mSelectedItem == position) {
                viewHolder.cbChoice.setChecked(true);
            } else {
                viewHolder.cbChoice.setChecked(false);
            }
            viewHolder.tvChoice.setText(getItem(position).toString());

            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position != mSelectedItem) {
                mSelectedItem = position;
            }
            notifyDataSetChanged();
        }

        public class ViewHolder {
            TextView tvChoice;
            CheckBox cbChoice;
        }
    }
}
