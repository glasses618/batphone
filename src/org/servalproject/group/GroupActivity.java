package org.servalproject.group;

import android.os.Bundle;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ArrayAdapter;
import android.content.Context;
import android.database.Cursor;

import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servaldna.SubscriberId;
import org.servalproject.servaldna.meshms.MeshMSMessage;
import org.servalproject.servaldna.keyring.KeyringIdentity;
import org.servalproject.batphone.CallHandler;
import org.servalproject.servald.IPeerListListener;
import org.servalproject.servald.Peer;
import org.servalproject.servald.PeerComparator;
import org.servalproject.servald.PeerListService;
import org.servalproject.rhizome.MeshMS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;

public class GroupActivity extends Activity {

    private static final String TAG = "GroupActivity";
    private ServalBatPhoneApplication app;
    private KeyringIdentity identity;
    private ArrayList<Group> groups = new ArrayList<Group>();
    private ArrayAdapter<String> groupPeerListAdapter;
    private GroupListAdapter groupListAdapter;
    private ArrayList<Peer> peers = new ArrayList<Peer>();
    private ArrayList<String> peersList = new ArrayList<String>();
    private ListView groupListView;
    private ListView groupPeerListView;
    private Button buttonCreateGroup;
    private Button buttonDestroyGroup;
    private Button buttonJoinGroup;
    private Button buttonLeaveGroup;
    private EditText etCreateGroup;
    private EditText etGroupPeer;
    private GroupDAO groupDAO;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_list);
        buttonCreateGroup = (Button) findViewById(R.id.button_create_group);
        buttonDestroyGroup = (Button) findViewById(R.id.button_destroy_group);
        buttonJoinGroup = (Button) findViewById(R.id.button_join_group);
        buttonLeaveGroup = (Button) findViewById(R.id.button_leave_group);
        etCreateGroup = (EditText) findViewById(R.id.edit_text_create_group);
        etGroupPeer = (EditText) findViewById(R.id.edit_text_group_peer);
        try {
            app = ServalBatPhoneApplication.context;
            this.identity = app.server.getIdentity();
            groupDAO= new GroupDAO(getApplicationContext(),identity.sid.toString());

            setupGroupList();
            setupButtonListener();
            setupGroupPeerList();

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            app.displayToastMessage(e.getMessage());
            this.finish();
        }
    }

    @Override
    protected void onResume() {
        checkNewJoinRequest();
        checkNewLeaveRequest();
        super.onResume();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                PeerListService.addListener(listener);
                return null;
            }

        } .execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PeerListService.removeListener(listener);
        peers.clear();
        peersList.clear();
        super.onPause();
    }

    private void setupGroupList() {
        groups = groupDAO.getMyGroupList();
        groups.addAll(groupDAO.getOtherGroupList());
        groupListAdapter = new GroupListAdapter(this, groups);
        groupListView = (ListView) findViewById(R.id.list_view_group);
        groupListView.setAdapter(groupListAdapter);

    }

    private void setupGroupPeerList() {
        groupPeerListAdapter =
            new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, peersList);
        groupPeerListView = (ListView) findViewById(R.id.list_view_group_peer);
        groupPeerListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        groupPeerListView.setAdapter(groupPeerListAdapter);
    }

    private void setupButtonListener() {

        buttonCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etCreateGroup.getText().toString();
                createGroup(name);
                setupGroupList();
                etCreateGroup.setText("");
            }
        }
                                            );

        buttonDestroyGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etCreateGroup.getText().toString();
                if(destroyGroup(name)) {
                    setupGroupList();
                }
                etCreateGroup.setText("");
            }
        });

        buttonJoinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = etGroupPeer.getText().toString();
                int len = groupPeerListView.getCount();
                SparseBooleanArray checked = groupPeerListView.getCheckedItemPositions();
                for (int i = 0; i < len; i++) {
                    if (checked.get(i)) {
                        SubscriberId peerSid  = peers.get(i).getSubscriberId();
                        String peerString = peerSid.toString();
                        String text = "Group Message:JOIN," + groupName + "," + peerString;
                        unicast(GroupActivity.this.identity.sid, peerSid, text);
                    }
                }
                etGroupPeer.setText("");
            }
        });

        buttonLeaveGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String groupName = etGroupPeer.getText().toString();
                int len = groupPeerListView.getCount();
                SparseBooleanArray checked = groupPeerListView.getCheckedItemPositions();
                for (int i = 0; i < len; i++) {
                    if (checked.get(i)) {
                        SubscriberId peerSid  = peers.get(i).getSubscriberId();
                        String peerString = peerSid.toString();
                        String text = "Group Message:LEAVE," + groupName + "," + peerString;
                        unicast(GroupActivity.this.identity.sid, peerSid, text);
                    }
                }

                etGroupPeer.setText("");
            }
        });
    }


    private void peerUpdated(Peer p) {
        if (!peers.contains(p)) {
            if (!p.isReachable())
                return;
            peers.add(p);
            Log.d(TAG,"New peer: "+ p.toString());
        }
        Collections.sort(peers, new PeerComparator());
        GroupActivity.this.peersList.clear();
        for(int i = 0; i < peers.size(); i++) {
            GroupActivity.this.peersList.add(peers.get(i).toString());
        }
        GroupActivity.this.groupPeerListAdapter.notifyDataSetChanged();
    }

    private IPeerListListener listener = new IPeerListListener() {
        @Override
        public void peerChanged(final Peer p) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    peerUpdated(p);
                };

            });
        }
    };

    public void createGroup(String name) {
        if (!name.equals("")) {
            groupDAO.createGroup(name, identity.sid.toString(),"");
        }
    }

    public boolean destroyGroup(String groupName) {
        boolean result = false;
        try {
            if(groupDAO.isMyGroup(groupName)) {
                ArrayList<String> members = groupDAO.getMemberList(groupName, identity.sid.toString());
                if (members.size() > 0) {
                    String newLeader = members.get(0);
                    for(int i = 0; i < members.size(); i++) {
                        SubscriberId memberSid = new SubscriberId(members.get(i));
                        String text = "Group Message:CHANGE_LEADER," + groupName + "," + identity.sid.toString() + "," + newLeader;
                        unicast(identity.sid, memberSid, text);
                    }

                }
                groupDAO.deleteGroup(groupName, identity.sid.toString());
                result = true;
            }
        } catch(Exception e) {
            Log.e(TAG, e.getMessage(), e);
            app.displayToastMessage(e.getMessage());
        }

        return result;

    }

    public void unicast(SubscriberId sender, SubscriberId receiver, String messageText) {

        if (messageText==null || "".equals(messageText))
            return;
        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected void onPostExecute(Boolean ret) {
                if (ret) {
                }
            }

            @Override
            protected Boolean doInBackground(Object... args) {
                try {
                    SubscriberId sender = (SubscriberId) args[0];
                    SubscriberId receiver = (SubscriberId) args[1];
                    String text = (String) args[2];
                    app.server.getRestfulClient().meshmsSendMessage(sender, receiver, text);
                    Log.d(TAG, "unicast from " + sender.toString() + " to " + receiver.toString());
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    app.displayToastMessage(e.getMessage());
                }
                return false;
            }
        } .execute(sender, receiver,  messageText);

    }
    public void multicast(Group group, String text) {
        try {
            ArrayList<GroupMember> members =  group.getMembers();
            for(int i = 0; i < members.size(); i++) {
                SubscriberId memberSid = new SubscriberId(members.get(i).getSid());
                unicast(identity.sid, memberSid, text);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            app.displayToastMessage(e.getMessage());
        }
    }

    private void checkNewJoinRequest() {
        HashMap<String,String> newJoinList = groupDAO.getNewJoinList();
        Set set = newJoinList.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()) {
            Map.Entry member = (Map.Entry) i.next();
            Log.d(TAG,"NEW JOIN REQUEST FROM: " + member.getKey()+ " TO Group: " + member.getValue());
            addMember((String) member.getValue(), (String) member.getKey());

        }
    }

    private void checkNewLeaveRequest() {
        HashMap<String,String> newLeaveList = groupDAO.getNewLeaveList();
        Set set = newLeaveList.entrySet();
        Iterator i = set.iterator();
        while(i.hasNext()) {
            Map.Entry member = (Map.Entry) i.next();
            Log.d(TAG,"NEW LEAVE REQUEST FROM: " + member.getKey()+ " TO Group: " + member.getValue());
            removeMember((String) member.getValue(), (String) member.getKey());
        }
    }

    private void addMember(String groupName, String member) {
        try {

            if(groupDAO.isMyGroup(groupName)) {
                groupDAO.insertMember(new GroupMember(groupName, identity.sid.toString(), "MEMBER", member, ""));
                Log.d(TAG, member + " joined!");
            }
        }
        catch(Exception e) {
            Log.e(TAG, e.getMessage(), e);
            app.displayToastMessage(e.getMessage());
            this.finish();
        }

    }

    private void removeMember(String groupName, String member) {
        try {

            if(groupDAO.isMyGroup(groupName)) {
                groupDAO.deleteMember(new GroupMember(groupName, identity.sid.toString(), "MEMBER", member, ""));
                SubscriberId memberSid = new SubscriberId(member);
                unicast(identity.sid, memberSid, "Group Message:DONE_LEAVE," + groupName + "," + identity.sid.toString());
                Log.d(TAG, member + " leave!");
            }
        }
        catch(Exception e) {
            Log.e(TAG, e.getMessage(), e);
            app.displayToastMessage(e.getMessage());
            this.finish();
        }

    }

}
