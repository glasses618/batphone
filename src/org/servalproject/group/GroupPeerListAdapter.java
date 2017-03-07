package org.servalproject.group;

import org.servalproject.R;
import org.servalproject.servaldna.SubscriberId;
import org.servalproject.servald.Peer;

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

public class GroupPeerListAdapter extends ArrayAdapter<Peer> {

    private static final String TAG = "GroupPeerListAdapter";
    public GroupPeerListAdapter(Context context, ArrayList<Peer> peers) {
        super(context, 0, peers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Peer peer = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.group_peer_list_item, parent, false);
        }
        TextView tvName = (TextView) convertView.findViewById(R.id.text_view_group_peer_name);
        tvName.setText(peer.toString());
        tvName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        return convertView;

    }
}
