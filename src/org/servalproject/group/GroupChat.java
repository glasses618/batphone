package org.servalproject.group;

public class GroupChat {

    private String groupName;
    private String sid;
    private String content;
    private Long timestamp;
    private boolean isRead;
    private boolean isMine;

    public GroupChat(String groupName, String sid, String content, Long timestamp, boolean isRead, boolean isMine) {
        this.groupName = groupName;
        this.sid = sid;
        this.content = content;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.isMine = isMine;
    }

    public String getContent() {
        return content;
    }
    public String getShortSid() {
        String shortSid = sid.substring(0, 5) + "*";
        return shortSid;
    }
    public boolean getIsMine() {
        return isMine;
    }
}
