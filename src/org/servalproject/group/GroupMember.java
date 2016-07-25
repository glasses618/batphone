package org.servalproject.group;

public class GroupMember {

  private String groupName;
  private String role;
  private String sid;
  private String memberName;

  public GroupMember(String groupName, String role, String sid, String memberName) {
    this.groupName = groupName;
    this.role = role;
    this.sid = sid;
    this.memberName = memberName;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getRole() {
    return role;
  }

  public String getSid() {
    return sid;
  }

  public String getMemberName() {
    return memberName;
  }
  public void setSid(String sid){
    this.sid = sid;
  }
  @Override 
  public boolean equals(Object o) {
    if(o instanceof GroupMember){
      GroupMember toCompare = (GroupMember) o;
      return this.sid.equals(toCompare.getSid()) &&
        this.role.equals(toCompare.getRole());
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return sid.hashCode();
  }

}
