package org.servalproject.group;

import org.servalproject.R;
import org.servalproject.servaldna.SubscriberId;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.LayoutInflater;

import java.util.ArrayList;

public class GroupChatListAdapter extends ArrayAdapter<GroupChat> {

    public GroupChatListAdapter(Context context, ArrayList<GroupChat> chatList) {
        super(context, 0, chatList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupChat chat = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_chat_item, parent, false);
        }
        TextView tvText = (TextView) convertView.findViewById(R.id.text_view_group_chat_text);
        if(chat.getIsMine()) {
            tvText.setText("Me: " + chat.getContent());
        } else {
            tvText.setText(chat.getShortSid() + ": " + chat.getContent());
        }

        return convertView;
    }

}
