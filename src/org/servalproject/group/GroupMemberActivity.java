package org.servalproject.group;

import android.os.Bundle;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.servalproject.R;
import org.servalproject.servaldna.SubscriberId;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servaldna.keyring.KeyringIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GroupMemberActivity extends Activity {
  private static final String TAG = "GroupMemberActivity";
  private ArrayList<String> members = new ArrayList<String>();
  private String groupName;
  private String groupLeader;
  private GroupMemberListAdapter adapter;
  private ListView groupMemberListView;
  private GroupDAO groupDAO;
  private ServalBatPhoneApplication app;
  private KeyringIdentity identity;
  
  @Override 
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_member_list);
     try {
      app = ServalBatPhoneApplication.context;
      this.identity = app.server.getIdentity();
      Intent intent = getIntent();
      groupName =  intent.getStringExtra("group_name");
      groupLeader = intent.getStringExtra("group_leader");
      groupDAO= new GroupDAO(getApplicationContext(),identity.sid.toString());
      members = groupDAO.getMemberList(groupName, groupLeader);
    } catch(Exception e) {
      Log.e(TAG, e.getMessage(), e);
    }


    setupGroupMemberList();
  }

  private void setupGroupMemberList() {
    adapter = new GroupMemberListAdapter(this, members);
    groupMemberListView = (ListView) findViewById(R.id.list_view_group_member);
    groupMemberListView.setAdapter(adapter);
  }


  
}
