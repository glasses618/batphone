package org.servalproject.group;

import android.app.Service;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.IBinder;
import android.text.TextUtils;

import org.servalproject.rhizome.MeshMS;
import org.servalproject.servaldna.SubscriberId;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servaldna.keyring.KeyringIdentity;
import org.servalproject.servaldna.meshms.MeshMSMessage;
import org.servalproject.servaldna.meshms.MeshMSMessageList;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Stack;


public class GroupService extends Service {

    private static final String TAG = "GroupService";
    public static final String NEW_CHAT_MESSAGE_ACTION = "org.servalproject.group.NEW_CHAT_MESSGAE";
    public static final String UPDATE_GROUP_ACTION = "org.servalproject.group.UPDATE_GROUP";
    private ServalBatPhoneApplication app;
    private KeyringIdentity identity;


    @Override
    public void onCreate() {
        Log.d(TAG, "create group service");
        try {
            app = ServalBatPhoneApplication.context;
            this.identity = app.server.getIdentity();
        } catch (Exception e ) {
            Log.e(TAG, e.getMessage(), e);
            app.displayToastMessage(e.getMessage());


        }
    }

    @Override
    public  IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.d(TAG,"NEW MESSAGE!!");
            Bundle bundle = intent.getExtras();
            String senderSidText = (String) bundle.get("sender");
            Log.d(TAG,"from: " +  senderSidText);
            SubscriberId senderSid = new SubscriberId(senderSidText);
            updateGroupMessageList(identity.sid, senderSid);
        }
        catch(Exception e) {
            Log.e(TAG, e.getMessage(), e);
            app.displayToastMessage(e.getMessage());

        }
        stopSelf();
        return super.onStartCommand(intent,flags,startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"Destroy group service");
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
                    return true;
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    app.displayToastMessage(e.getMessage());
                }
                return false;
            }
        } .execute(sender, receiver,  messageText);
    }

    private void updateGroupMessageList(SubscriberId receiver, SubscriberId sender) {
        GroupMessageTask task = new GroupMessageTask(this);
        task.execute(receiver, sender);
    }

    private class GroupMessageTask extends AsyncTask<SubscriberId, Void, Void> {
        private Context mContext;
        private GroupDAO groupDAO;

        public GroupMessageTask(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(SubscriberId... args) {
            try {

                Log.d(TAG,"start update......");
                SubscriberId receiver = args[0];
                SubscriberId sender =  args[1];
                MeshMSMessageList results = app.server.getRestfulClient().meshmsListMessages(identity.sid, sender);
                MeshMSMessage item;
                LinkedList<Object> listItems = new LinkedList<Object>();
                DateFormat df = DateFormat.getDateInstance();
                DateFormat tf = DateFormat.getTimeInstance(DateFormat.SHORT);
                DateFormat tff = DateFormat.getTimeInstance(DateFormat.LONG);
                String lastDate = df.format(new Date());
                Log.d(TAG,"current time:" + tff.format(new Date(System.currentTimeMillis())));
                groupDAO = new GroupDAO(getApplicationContext(),identity.sid.toString());
                Long lastMessageTime = groupDAO.getLastMessageTimestamp(sender.toString());
                Stack<MeshMSMessage> itemStack = new Stack<MeshMSMessage>();
                while((item = results.nextMessage())!=null) {

                    if(item.type == MeshMSMessage.Type.MESSAGE_RECEIVED && GroupMessage.extractGroupMessage(item.text) != null) {
                        Long currentTime = System.currentTimeMillis();
                        Log.d(TAG, "lastMessageTimestamp: " +
                              String.valueOf(lastMessageTime)
                              + ", item.timestamp: " + String.valueOf(item.timestamp*1000) + ", content: "
                              + item.text);
                        if(item.timestamp*1000 <= lastMessageTime)
                            continue;
                        itemStack.push(item);
                    }
                }
                while(!itemStack.empty()) {
                    processGroupMessage(sender.toString(), receiver.toString(), itemStack.pop());
                }
            } catch(Exception e) {
                Log.e(TAG, e.getMessage(), e);
                app.displayToastMessage(e.getMessage());
            }
            return null;
        }

        private void processGroupMessage(String sender, String receiver, MeshMSMessage item) {
            ArrayList<String> extractResults = GroupMessage.extractGroupMessage(item.text);
            String groupOperation = extractResults.get(0);
            String groupName = extractResults.get(1);
            String groupLeaderSid = extractResults.get(2);
            Long timestamp = item.timestamp*1000;

            if(groupOperation.equals("JOIN")) {
                groupDAO.insertMessage(new GroupMessage("JOIN", sender, receiver,
                                                        groupName, timestamp, 0, "", groupLeaderSid));
            } else if(groupOperation.equals("LEAVE")) {
                groupDAO.insertMessage(new GroupMessage("LEAVE", sender, receiver,
                                                        groupName, timestamp, 0, "", groupLeaderSid));
            } else if(groupOperation.equals("CHAT")) {

                String text = TextUtils.join(",",extractResults.subList(3, extractResults.size()));
                groupDAO.insertMessage(new GroupMessage("CHAT", sender, receiver,
                                                        groupName, timestamp, 1, text, groupLeaderSid));
                Intent intent = new Intent(NEW_CHAT_MESSAGE_ACTION);
                app.sendBroadcast(intent);
            } else if(groupOperation.equals("DONE_LEAVE")) {
                groupDAO.deleteGroup(groupName, groupLeaderSid);
                groupDAO.insertMessage(new GroupMessage("DONE_LEAVE", sender, receiver,
                                                        groupName, timestamp, 1, "", groupLeaderSid));
            } else if(groupOperation.equals("UPDATE")) {
                ArrayList<GroupMember> members = new ArrayList<GroupMember>();
                Log.d(TAG, "UPDATE!!" + " TABLE NAME= " + groupName);
                for (String member : extractResults.subList(3, extractResults.size())) {
                    members.add(new GroupMember(groupName, groupLeaderSid, "MEMBER", member, ""));
                }
                groupDAO.updateGroup(new Group(groupName, members, groupLeaderSid, false));
                groupDAO.insertMessage(new GroupMessage("UPDATE", sender, receiver,
                                                        groupName, timestamp, 1, "", groupLeaderSid));
                app.sendBroadcast(new Intent(UPDATE_GROUP_ACTION));
            } else if(groupOperation.equals("CHANGE_LEADER")) {
                String oldLeader = groupLeaderSid;
                String newLeader = extractResults.get(3);
                Log.d(TAG,"Change Leader: group: " + groupName + ", from: " + oldLeader + ", to: " + newLeader);
                groupDAO.changeLeader(groupName, oldLeader, newLeader);
                groupDAO.insertMessage(new GroupMessage("CHANGE_LEADER", sender, receiver,
                                                        groupName, timestamp, 1, "", groupLeaderSid));
            }

        }
    }

}
