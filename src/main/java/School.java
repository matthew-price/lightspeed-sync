import java.util.ArrayList;
import java.util.HashMap;

public class School {
    private String name;
    private String schoolSisID;
    private HashMap<String, User> users;
    private ArrayList<Group> groups;

    public School(String name, String schoolSisID){
        this.name = name;
        this.schoolSisID = schoolSisID;
        users = new HashMap<String, User>();
        groups = new ArrayList<Group>();
    }

    public void addUser(String username, User user){
        users.put(username, user);
    }

    public void addGroup(Group group){
        System.out.println("Group started");
        System.out.println("Group name: " + group);
        groups.add(group);
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

    public Group getGroup(int i){
        return groups.get(i);
    }

    public String getSisID(){
        return schoolSisID;
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
