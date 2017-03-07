package org.servalproject.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import android.util.Log;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

public class GroupDAO {
    public static final String MESSAGES_TABLE_NAME = "messages";
    public static final String MESSAGES_COLUMN_ID = "id";
    public static final String MESSAGES_COLUMN_TYPE = "type";
    public static final String MESSAGES_COLUMN_FROM_WHO = "from_who";
    public static final String MESSAGES_COLUMN_TO_WHO = "to_who";
    public static final String MESSAGES_COLUMN_OBJECT_GROUP = "object_group";
    public static final String MESSAGES_COLUMN_TIMESTAMP = "timestamp";
    public static final String MESSAGES_COLUMN_DONE = "done";
    public static final String MESSAGES_COLUMN_CONTENT = "content";
    public static final String MESSAGES_COLUMN_LEADER = "leader";
    public static final String MESSAGES_CREATE_TABLE = "CREATE TABLE " +
            MESSAGES_TABLE_NAME + " (" +
            MESSAGES_COLUMN_ID + " INTEGER PRIMARY KEY, " +
            MESSAGES_COLUMN_TYPE + " TEXT, " +
            MESSAGES_COLUMN_FROM_WHO + " TEXT, " +
            MESSAGES_COLUMN_TO_WHO + " TEXT, " +
            MESSAGES_COLUMN_OBJECT_GROUP + " TEXT, " +
            MESSAGES_COLUMN_TIMESTAMP + " TEXT, " +
            MESSAGES_COLUMN_DONE + " INTEGER, " +
            MESSAGES_COLUMN_CONTENT + " TEXT, " +
            MESSAGES_COLUMN_LEADER + " TEXT) ";


    public static final String MEMBERS_TABLE_NAME = "members";
    public static final String MEMBERS_COLUMN_ID = "id";
    public static final String MEMBERS_COLUMN_ROLE = "role";
    public static final String MEMBERS_COLUMN_SID = "sid";
    public static final String MEMBERS_COLUMN_MEMBER_NAME = "member_name";
    public static final String MEMBERS_COLUMN_GROUP_NAME = "group_name";
    public static final String MEMBERS_COLUMN_LEADER = "leader";
    public static final String MEMBERS_CREATE_TABLE = "CREATE TABLE " +
            MEMBERS_TABLE_NAME + " (" +
            MEMBERS_COLUMN_ID +  " INTEGER PRIMARY KEY, " +
            MEMBERS_COLUMN_ROLE + " TEXT , " +
            MEMBERS_COLUMN_SID +  " TEXT , " +
            MEMBERS_COLUMN_GROUP_NAME +  " TEXT , " +
            MEMBERS_COLUMN_MEMBER_NAME + " TEXT , " +
            MEMBERS_COLUMN_LEADER + " TEXT)" ;


    private SQLiteDatabase db;
    private String mySid;
    public GroupDAO(Context context, String mySid) {
        this.mySid = mySid;
        db = GroupDbHelper.getDatabase(context);

    }

    public boolean insertMessage(GroupMessage gm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGES_COLUMN_TYPE, gm.getType());
        contentValues.put(MESSAGES_COLUMN_FROM_WHO, gm.getFromWho());
        contentValues.put(MESSAGES_COLUMN_TO_WHO, gm.getToWho());
        contentValues.put(MESSAGES_COLUMN_OBJECT_GROUP, gm.getObjectGroup());
        contentValues.put(MESSAGES_COLUMN_TIMESTAMP, gm.getTimestamp());
        contentValues.put(MESSAGES_COLUMN_DONE, gm.getDone());
        contentValues.put(MESSAGES_COLUMN_CONTENT, gm.getContent());
        contentValues.put(MESSAGES_COLUMN_LEADER, gm.getGroupLeader());
        db.insert(MESSAGES_TABLE_NAME, null, contentValues);
        Log.d("GroupDAO", "insert message");
        return true;
    }

    public boolean deleteMessage(Integer id) {
        return db.delete(MESSAGES_TABLE_NAME, MESSAGES_COLUMN_ID + " = " + id, null) > 0;
    }

    public boolean doneMessage(Integer id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MESSAGES_COLUMN_DONE, 1);
        db.update(MESSAGES_TABLE_NAME, contentValues, MESSAGES_COLUMN_ID+ " = ?", new String[] {Integer.toString(id)});
        Log.d("GroupDAO", "done message");
        return true;

    }

    public Long getLastMessageTimestamp(String sid ) {

        Long result = Long.valueOf(0);
        Cursor c = db.rawQuery(
                       "SELECT " + MESSAGES_COLUMN_TIMESTAMP + ", MAX(" + MESSAGES_COLUMN_TIMESTAMP + ") " +
                       " FROM " + MESSAGES_TABLE_NAME +
                       " WHERE  " + MESSAGES_COLUMN_FROM_WHO + " = ? "
                       , new String[] {sid});
        c.moveToFirst();
        if(c.getCount() > 0) {
            result = c.getLong(c.getColumnIndexOrThrow(MESSAGES_COLUMN_TIMESTAMP));
        }
        c.close();
        return result;
    }

    public HashMap<String, String> getNewJoinList() {
        HashMap<String, String>  map = new HashMap<String, String>();

        Cursor c = db.query(MESSAGES_TABLE_NAME,
                            new String[] {MESSAGES_COLUMN_ID, MESSAGES_COLUMN_FROM_WHO, MESSAGES_COLUMN_OBJECT_GROUP},
                            MESSAGES_COLUMN_TYPE + " = ? AND " + MESSAGES_COLUMN_DONE + " = ? ", new String[] {"JOIN", "0"}, null, null, null );

        while(c.moveToNext()) {
            String from = c.getString(c.getColumnIndexOrThrow(MESSAGES_COLUMN_FROM_WHO));
            Integer id = c.getInt(c.getColumnIndexOrThrow(MESSAGES_COLUMN_ID));
            String groupName = c.getString(c.getColumnIndexOrThrow(MESSAGES_COLUMN_OBJECT_GROUP));
            doneMessage(id);
            map.put(from, groupName);
        }
        c.close();
        return map;
    }

    public HashMap<String, String> getNewLeaveList() {
        HashMap<String, String> map = new HashMap<String, String>();

        Cursor c = db.query(MESSAGES_TABLE_NAME,
                            new String[] {MESSAGES_COLUMN_ID, MESSAGES_COLUMN_FROM_WHO, MESSAGES_COLUMN_OBJECT_GROUP},
                            MESSAGES_COLUMN_TYPE + " = ? AND " + MESSAGES_COLUMN_DONE + " = ? ", new String[] {"LEAVE", "0"}, null, null, null );

        while(c.moveToNext()) {
            String from = c.getString(c.getColumnIndexOrThrow(MESSAGES_COLUMN_FROM_WHO));
            Integer id = c.getInt(c.getColumnIndexOrThrow(MESSAGES_COLUMN_ID));
            String groupName = c.getString(c.getColumnIndexOrThrow(MESSAGES_COLUMN_OBJECT_GROUP));
            doneMessage(id);
            map.put(from, groupName);
        }
        c.close();
        return map;
    }

    public ArrayList<GroupChat> getChatList(String group, String leaderSid) {
        ArrayList<GroupChat> chatList = new ArrayList<GroupChat>();
        Cursor c = db.query(MESSAGES_TABLE_NAME, null, MESSAGES_COLUMN_TYPE + " = ? AND " +
                            MESSAGES_COLUMN_OBJECT_GROUP + " = ? AND " + MESSAGES_COLUMN_LEADER + " = ? ",
                            new String[] {"CHAT", group, leaderSid}, null, null, null);

        while(c.moveToNext()) {
            String groupName = c.getString(c.getColumnIndexOrThrow(MESSAGES_COLUMN_OBJECT_GROUP));
            String from = c.getString(c.getColumnIndexOrThrow(MESSAGES_COLUMN_FROM_WHO));
            String to = c.getString(c.getColumnIndexOrThrow(MESSAGES_COLUMN_TO_WHO));
            Long timestamp = c.getLong(c.getColumnIndexOrThrow(MESSAGES_COLUMN_TIMESTAMP));
            boolean done = c.getInt(c.getColumnIndexOrThrow(MESSAGES_COLUMN_DONE))>0;
            String content = c.getString(c.getColumnIndexOrThrow(MESSAGES_COLUMN_CONTENT));
            Log.d("GroupDAO","chat content: " + content);
            if(from.equals(mySid)) {
                GroupChat chat = new GroupChat(groupName, mySid, content, timestamp, done, true);
                chatList.add(chat);
            } else if(to.equals(mySid)) {
                GroupChat chat = new GroupChat(groupName, from, content, timestamp, done, false);
                chatList.add(chat);
            }
        }
        c.close();
        return chatList;
    }

    public boolean insertMember(GroupMember gm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MEMBERS_COLUMN_GROUP_NAME, gm.getGroupName());
        contentValues.put(MEMBERS_COLUMN_ROLE, gm.getRole());
        contentValues.put(MEMBERS_COLUMN_SID, gm.getSid());
        contentValues.put(MEMBERS_COLUMN_MEMBER_NAME, gm.getMemberName());
        contentValues.put(MEMBERS_COLUMN_LEADER, gm.getLeader());
        db.insert(MEMBERS_TABLE_NAME, null, contentValues);
        Log.d("GroupDAO", "insert member");
        return true;
    }

    public void changeLeader(String groupName, String oldLeaderSid, String newLeaderSid) {
        if (getGroup(groupName, oldLeaderSid) != null) {

            Cursor c = db.query(MEMBERS_TABLE_NAME, null,
                                MEMBERS_COLUMN_GROUP_NAME + " = ? AND " + MEMBERS_COLUMN_LEADER + " = ? ",
                                new String[] {groupName, oldLeaderSid}, null, null, null);
            while(c.moveToNext()) {
                Integer id = c.getInt(c.getColumnIndexOrThrow(MEMBERS_COLUMN_ID));
                String sid = c.getString(c.getColumnIndexOrThrow(MEMBERS_COLUMN_SID));
                String role = c.getString(c.getColumnIndexOrThrow(MEMBERS_COLUMN_ROLE));

                if(sid.equals(oldLeaderSid)) {
                    deleteMember(new GroupMember(groupName, oldLeaderSid, "LEADER", oldLeaderSid, ""));
                    continue;
                } else if(sid.equals(newLeaderSid)) {
                    role = "LEADER";
                }

                ContentValues contentValues = new ContentValues();
                contentValues.put(MEMBERS_COLUMN_GROUP_NAME, groupName);
                contentValues.put(MEMBERS_COLUMN_ROLE, role);
                contentValues.put(MEMBERS_COLUMN_SID, sid);
                contentValues.put(MEMBERS_COLUMN_MEMBER_NAME, "");
                contentValues.put(MEMBERS_COLUMN_LEADER, newLeaderSid);
                db.update(MEMBERS_TABLE_NAME, contentValues, MEMBERS_COLUMN_ID+ " = " + id, null);

            }
            c.close();
        }
    }

    public void updateGroup(Group group) {
        Group oldGroup = getGroup(group.getName(), group.getLeader());
        if (oldGroup != null) {
            for(GroupMember gm: oldGroup.getMembers()) {
                if(!group.getMembers().contains(gm))
                    deleteMember(gm);
            }
            for(GroupMember gm: group.getMembers()) {
                if(!oldGroup.getMembers().contains(gm))
                    insertMember(gm);
            }
        } else {
            for(GroupMember gm : group.getMembers()) {
                insertMember(gm);
            }
            insertMember(new GroupMember(group.getName(), group.getLeader(),"LEADER", group.getLeader(), ""));
        }
    }

    public Group getGroup(String groupName, String leaderSid) {
        ArrayList<GroupMember> gmList = new ArrayList<GroupMember>();
        Cursor c = db.query(MEMBERS_TABLE_NAME, new String[] {MEMBERS_COLUMN_SID, MEMBERS_COLUMN_ROLE}, MEMBERS_COLUMN_GROUP_NAME + " = ? AND " + MEMBERS_COLUMN_LEADER + " = ? ",
                            new String[] {groupName, leaderSid}, null, null, null, null);
        while(c.moveToNext()) {
            String memberSid = c.getString(c.getColumnIndexOrThrow(MEMBERS_COLUMN_SID));
            String memberRole = c. getString(c.getColumnIndexOrThrow(MEMBERS_COLUMN_ROLE));
            if (memberRole.equals("MEMBER"))
                gmList.add(new GroupMember(groupName, leaderSid, memberRole, memberSid, ""));
        }
        c.close();
        if (gmList.size() > 0)
            return new Group(groupName, gmList, leaderSid, false);
        else
            return null;
    }

    public boolean deleteMember(GroupMember gm) {
        Cursor c = db.query(MEMBERS_TABLE_NAME, new String[] {MEMBERS_COLUMN_ID},
                            MEMBERS_COLUMN_ROLE + "= ? AND " + MEMBERS_COLUMN_SID + "= ? AND " + MEMBERS_COLUMN_GROUP_NAME + " = ? AND " + MEMBERS_COLUMN_LEADER + " = ? ", new String[] {gm.getRole(), gm.getSid(), gm.getGroupName(), gm.getLeader()}, null, null, null);
        c.moveToFirst();
        if(c.getCount() > 0) {
            Integer id = c.getInt(c.getColumnIndexOrThrow(MEMBERS_COLUMN_ID));

            return db.delete(MEMBERS_TABLE_NAME, MEMBERS_COLUMN_ID + " = " + Integer.toString(id), null) > 0;

        }
        c.close();
        return false;
    }

    public ArrayList<String> getMemberList(String groupName, String groupLeader) {
        ArrayList<String> list = new ArrayList<String>();

        Cursor c = db.query(MEMBERS_TABLE_NAME, null,
                            MEMBERS_COLUMN_GROUP_NAME + "= ? AND " + MEMBERS_COLUMN_LEADER + " = ? ",
                            new String[] {groupName, groupLeader}, null, null, null, null);
        while(c.moveToNext()) {
            String member = c.getString(c.getColumnIndexOrThrow("sid"));
            if (!member.equals(mySid)) {
                list.add(member);
            }
        }
        c.close();
        return list;
    }

    public ArrayList<String> getAbbreviationMemberList(String groupName, String groupLeader) {
        ArrayList<String> list = new ArrayList<String>();

        Cursor c = db.query(MEMBERS_TABLE_NAME, null,
                            MEMBERS_COLUMN_GROUP_NAME + "= ? AND " + MEMBERS_COLUMN_LEADER + " = ? ",
                            new String[] {groupName, groupLeader}, null, null, null, null);
        while(c.moveToNext()) {
            String member = c.getString(c.getColumnIndexOrThrow("sid"));
            if (!member.equals(mySid) && member.equals(groupLeader)) {
                list.add(member.substring(0,5) + "* (Leader)");
            } else if (!member.equals(mySid)) {
                list.add(member.substring(0,5) + "* ");
            } else if (member.equals(groupLeader)) {
                list.add(member.substring(0,5) + "* (Me, Leader)");
            } else {
                list.add(member.substring(0,5) + "* (Me)");
            }
        }
        c.close();
        return list;
    }

    public void createGroup(String groupName, String mySid, String myName) {

        GroupMember leader = new GroupMember(groupName, mySid, "LEADER", mySid, myName);
        insertMember(leader);

    }

    public void deleteGroup(String groupName, String groupLeader) {

        Cursor c = db.query(MEMBERS_TABLE_NAME, new String[] {MEMBERS_COLUMN_ID},
                            MEMBERS_COLUMN_GROUP_NAME + " = ? AND " + MEMBERS_COLUMN_LEADER + " = ? ", new String[] {groupName, groupLeader}, null, null, null);
        while(c.moveToNext()) {
            Integer id = c.getInt(c.getColumnIndexOrThrow(MEMBERS_COLUMN_ID));
            db.delete(MEMBERS_TABLE_NAME, MEMBERS_COLUMN_ID + " = " + Integer.toString(id), null);
        }
        c.close();
    }

    public ArrayList<Group> getMyGroupList() {
        ArrayList<Group> groupList = new ArrayList<Group>();
        Cursor c = db.query(MEMBERS_TABLE_NAME, new String[] {MEMBERS_COLUMN_GROUP_NAME},
                            MEMBERS_COLUMN_ROLE + "= ? AND " + MEMBERS_COLUMN_SID + "= ?", new String[] {"LEADER", mySid}, null, null, null);
        while(c.moveToNext()) {
            String groupName = c.getString(c.getColumnIndexOrThrow(MEMBERS_COLUMN_GROUP_NAME));
            Group group = new Group(groupName, new ArrayList<GroupMember>(), mySid, true);
            groupList.add(group);
        }
        return groupList;
    }

    public ArrayList<Group> getOtherGroupList() {
        ArrayList<Group> groupList = new ArrayList<Group>();
        Cursor c = db.query(MEMBERS_TABLE_NAME, new String[] {MEMBERS_COLUMN_GROUP_NAME, MEMBERS_COLUMN_SID},
                            MEMBERS_COLUMN_ROLE + "= ? ", new String[] {"LEADER"}, null, null, null);
        while(c.moveToNext()) {
            String groupName = c.getString(c.getColumnIndexOrThrow(MEMBERS_COLUMN_GROUP_NAME));
            String leaderSid = c.getString(c.getColumnIndexOrThrow(MEMBERS_COLUMN_SID));
            if (!leaderSid.equals(mySid)) {
                Group group = new Group(groupName, new ArrayList<GroupMember>(), leaderSid, false);
                groupList.add(group);
            }
        }
        return groupList;
    }
    public boolean isMyGroup(String groupName) {
        boolean result = false;
        Cursor c = db.query(MEMBERS_TABLE_NAME, new String[] {MEMBERS_COLUMN_GROUP_NAME},
                            MEMBERS_COLUMN_ROLE + "= ? AND " + MEMBERS_COLUMN_SID + "= ? AND " + MEMBERS_COLUMN_GROUP_NAME + " = ?", new String[] {"LEADER", mySid, groupName}, null, null, null);
        if (c.getCount() > 0) {
            result = true;
        }
        return result;

    }
}
