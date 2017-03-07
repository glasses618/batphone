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
import android.view.LayoutInflater;
import android.util.Log;

import java.util.ArrayList;

public class GroupListAdapter extends ArrayAdapter<Group> {

    private static final String TAG = "GroupListAdapter";
    public GroupListAdapter(Context context, ArrayList<Group> groups) {
        super(context, 0, groups);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Group group = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_list_item, parent, false);
        }
        TextView tvName = (TextView) convertView.findViewById(R.id.text_view_group_name);
        if(group.getIsMyGroup()) {
            tvName.setText(group.getName() + "(OWN)");
        } else {
            tvName.setText(group.getName() + "(" + group.getLeaderAbbreviation() + ")");
        }
        tvName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"clicked!!");
                Intent intent = new Intent(getContext(), GroupChatActivity.class);
                intent.putExtra("group_name", group.getName());
                intent.putExtra("leader_sid", group.getLeader());
                getContext().startActivity(intent);

            }
        });
        return convertView;


    }
}
