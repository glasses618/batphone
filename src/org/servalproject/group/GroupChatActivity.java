package org.servalproject.group;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils; 

import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servaldna.SubscriberId;
import org.servalproject.servaldna.keyring.KeyringIdentity;
import org.servalproject.servaldna.meshms.MeshMSMessage;
import org.servalproject.servaldna.meshms.MeshMSMessageList;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GroupChatActivity extends Activity{

  private final String TAG = "GroupChatActivity";
  private ServalBatPhoneApplication app;
  private KeyringIdentity identity;
  private ArrayList<GroupChat> chatList = new ArrayList<GroupChat>();
  private ArrayList<String> members = new ArrayList<String>();
  private ListView list;
  private Button buttonSendGroupMessage;
  private EditText etGroupMessageContent;
  private String groupName;
  private String groupLeaderSid;
  private GroupDAO groupDAO;

  @Override 
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.group_chat);
    try{
      app = ServalBatPhoneApplication.context;
      this.identity = app.server.getIdentity();
      buttonSendGroupMessage = (Button) findViewById(R.id.button_send_group_message);
      etGroupMessageContent = (EditText) findViewById(R.id.edit_text_group_message_content);
      list = (ListView) findViewById(R.id.list_view_group_chat);
      setupButtonListener();
      Intent intent = getIntent();
      groupName =  intent.getStringExtra("group_name");
      groupLeaderSid = intent.getStringExtra("leader_sid");
      groupDAO= new GroupDAO(getApplicationContext(),identity.sid.toString());
      updateGroupMembers();
      notifyGroupMember();
    }catch(Exception e) {
      Log.e(TAG,e.getMessage(),e);
      app.displayToastMessage(e.getMessage());
      this.finish();
    }

  }

  @Override
  public void onResume() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(GroupService.NEW_CHAT_MESSAGE_ACTION);
    filter.addAction(GroupService.UPDATE_GROUP_ACTION);
    this.registerReceiver(receiver, filter);
    populateList();
    super.onResume();
  }

  @Override
  public void onPause() {
    this.unregisterReceiver(receiver);
    super.onPause();
  }
    
  BroadcastReceiver receiver = new BroadcastReceiver() {
  
    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(GroupService.NEW_CHAT_MESSAGE_ACTION)) {
        populateList(); 
      } else if(intent.getAction().equals(GroupService.UPDATE_GROUP_ACTION)) {
        updateGroupMembers(); 
      }
    }
  
  }; 

  private void setupButtonListener() {

    buttonSendGroupMessage.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(View v) {
        String text = etGroupMessageContent.getText().toString();
        multicast("Group Message:CHAT,"+ groupName + "," + groupLeaderSid  +","+ text, "CHAT");
        Long timestamp = System.currentTimeMillis();
        GroupChatActivity.this.groupDAO.insertMessage(new GroupMessage("CHAT", GroupChatActivity.this.identity.sid.toString(), "", groupName, timestamp, 1,  text, groupLeaderSid));
        populateList();
        etGroupMessageContent.setText(""); 

      }
    });

  }

  private void updateGroupMembers(){
    members = groupDAO.getMemberList(groupName, groupLeaderSid);
  }
  private void notifyGroupMember() {
    if(groupDAO.isMyGroup(groupName)){
      String membersList = TextUtils.join(",",members);
      multicast("Group Message:UPDATE," + groupName + ","+ groupLeaderSid  + "," + membersList, "UPDATE");
    }
  }
  private void populateList() {
    if(!app.isMainThread()) {
      runOnUiThread(new Runnable() {
        @Override
        public void run(){
          populateList();
        }
      });
      return;
    } 
    UpdateChatTask task = new UpdateChatTask(this);
    task.execute();
  }

  private class UpdateChatTask extends AsyncTask<Void, Void, ArrayList<GroupChat>>{
    private GroupDAO groupDAO;
    private Context context;

    public UpdateChatTask(Context context){
      this.context = context;
    }

    @Override
    protected void onPostExecute(ArrayList<GroupChat> chatList) {
      GroupChatListAdapter adapter = new GroupChatListAdapter(context, chatList);
      list.setAdapter(adapter);
      list.setSelection(adapter.getCount() - 1);
    }

    @Override
    protected ArrayList<GroupChat> doInBackground(Void... params){
      groupDAO = new GroupDAO(getApplicationContext(), identity.sid.toString());
      ArrayList<GroupChat> chatList = new ArrayList<GroupChat>();
      chatList =  groupDAO.getChatList(groupName, groupLeaderSid);
      return chatList;
    }
  }

  public void unicast(SubscriberId sender, SubscriberId receiver, String messageText) {

    if (messageText==null || "".equals(messageText))
      return;
    new AsyncTask<Object, Void, Boolean>(){
      @Override
      protected void onPostExecute(Boolean ret) {
        if (ret) {
          // message.setText("");
          //  populateList();
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
    }.execute(sender, receiver,  messageText);

  }
  public void multicast(String text, String type) {
    try {
        for(int i = 0; i < members.size(); i++) {
          SubscriberId memberSid = new SubscriberId(members.get(i));
          unicast(identity.sid, memberSid, text);
        }
    } catch (Exception e) {
      Log.e(TAG, e.getMessage(), e);
      app.displayToastMessage(e.getMessage());
    }
  }
}
