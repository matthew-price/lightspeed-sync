import java.util.ArrayList;
import java.util.HashMap;

public class School {
    private String name;
    private String schoolSisID;
    private String parentGroupId;
    private HashMap<String, User> users;
    private ArrayList<Group> groups;
    private ArrayList<OU> ous;

    public School(String name, String schoolSisID, String parentGroupId){
        this.name = name;
        this.schoolSisID = schoolSisID;
        this.parentGroupId = parentGroupId;
        users = new HashMap<String, User>();
        groups = new ArrayList<Group>();
        ous = new ArrayList<OU>();
    }

    public void addUser(String username, User user){
        users.put(username, user);
    }

    public void addGroup(Group group){
        System.out.println("Group started");
        System.out.println("Group name: " + group);
        groups.add(group);
    }

    public void addOU(OU ou){
        ous.add(ou);
    }

    public HashMap<String, User> getUsers(){
        return users;
    }

    public User getUser(String sn){
        return users.get(sn);
    }

    public ArrayList<Group> getGroups(){
        return groups;
    }

    public ArrayList<OU> getOUs(){
        return ous;
    }

    public Group getGroup(int i){
        return groups.get(i);
    }

    public String getSisID(){
        return schoolSisID;
    }

    public String getParentGroupId(){
        return parentGroupId;
    }

    public int countUsers(){
        return users.size();
    }

    public int countGroups(){
        return groups.size();
    }

    public boolean hasUser(String cn){
        return users.containsKey(cn);
    }

}
