package org.servalproject.group;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

public class GroupMessage {

  private String type;
  private String fromWho;
  private String toWho;
  private String objectGroup;
  private Long timestamp;
  private Integer done;
  private String content;

  public GroupMessage(String type, String fromWho, String toWho, String objectGroup,
      Long timestamp, Integer done, String content) {
    
    this.type = type;
    this.fromWho = fromWho;
    this.toWho = toWho;
    this.objectGroup = objectGroup;
    this.timestamp = timestamp;
    this.done = done;
    this.content = content;
  }
  
  public String getType() {
    return type;
  }

  public String getFromWho() {
    return fromWho;
  }

  public String getToWho() {
    return toWho;
  }

  public String getObjectGroup() {
    return objectGroup;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public Integer getDone() {
    return done;
  }
    
  public String getContent() {
    return content;
  }
  

  public static ArrayList<String> extractGroupMessage(String message) {
    ArrayList<String> result = new ArrayList<String>();
    Pattern pattern = Pattern.compile("^Group Message:.*");
    Matcher matcher = pattern.matcher(message);
    if(matcher.matches()) {
      String groupMessage = message.substring(14, message.length());
      String[] groupMessages = groupMessage.split(","); 
      for(int i = 0; i < groupMessages.length; i++) {
        result.add(groupMessages[i]);
      }
      return result;
    } 
    return null;
  }

}
