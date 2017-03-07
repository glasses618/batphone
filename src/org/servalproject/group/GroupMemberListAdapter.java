package org.servalproject.group;

import org.servalproject.R;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.ArrayList;

public class GroupMemberListAdapter extends ArrayAdapter<String> {
    public GroupMemberListAdapter(Context context, ArrayList<String> members) {
        super(context, 0, members);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String member = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_member_list_item, parent, false);
        }
        TextView tvName = (TextView) convertView.findViewById(R.id.text_view_group_member_name);
        tvName.setText(member);

        return convertView;


    }
}
