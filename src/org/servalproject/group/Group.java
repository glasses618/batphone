package org.servalproject.group;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group {
  private String name = "";
  private String leader;
  private boolean isMyGroup;
  private ArrayList<GroupMember> members = new ArrayList<GroupMember>();

  Group(String name) {
    this.name = name;
  }
  
  Group(String name, ArrayList<GroupMember> members, String leader, boolean isMyGroup) {
   this.name = name;
   this.members = members;
   this.leader = leader;
   this.isMyGroup = isMyGroup;
  }
    
  public String getName(){
    return name;
  }

  public ArrayList<GroupMember> getMembers() {
    return members;
  }

  public String getLeader(){
    return leader;
  }

  public boolean getIsMyGroup(){
    return isMyGroup;
  } 

  public String getLeaderAbbreviation(){
    return leader.substring(0,5) + "*";
  }

  @Override 
  public boolean equals(Object o) {
    if(o instanceof Group){
      Group toCompare = (Group) o;
      return this.leader.equals(toCompare.getLeader()) &&
        this.name.equals(toCompare.getName());
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(name, leader);
  }



} 
