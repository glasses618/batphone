package org.servalproject.group;

import java.util.ArrayList;
import java.util.List;

public class Group {
  private String name = "";
  private GroupMember leader;
  private ArrayList<GroupMember> members = new ArrayList<GroupMember>();
//  public GroupType type;

  Group(String name) {
    this.name = name;
  }
  
  Group(String name, ArrayList<GroupMember> members, GroupMember leader) {
   this.name = name;
   this.members = members;
   this.leader = leader;
  }
    
  public String getName(){
    return name;
  }

  public ArrayList<GroupMember> getMembers() {
    return members;
  }

  public GroupMember getLeader(){
    return leader;
  }
//  public enum GroupType {
//    OWN, MEMBER_OF, OTHER
//  }
//  
//  public void setType(GroupType type) {
//    this.type = type;
//  }
//
//  public void addMember(GroupMember member) {
//      this.members.add(member);
//  }
//
//  public void removeMember(GroupMember member) {
//    this.members.remove(member);
//  }
//  
//  public void clearAllMember() {
//    this.members.clear();
//  }
//  public ArrayList<String> getMemberSidList() {
//    ArrayList<String> memberSidList = new ArrayList<String>();
//    for (int i = 0; i < members.size(); i++) {
//      memberSidList.add(members.get(i).getSid());
//    }
//    return memberSidList;
//  }
} 
