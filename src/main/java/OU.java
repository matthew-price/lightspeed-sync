import java.util.ArrayList;

public class OU {

    private String name;
    private String distinguishedName;
    private School school;
    ArrayList<User> member;


    public OU(String name, String distinguishedName, School school){
        this.name = name;
        this.distinguishedName = distinguishedName;
        this.school = school;
        member = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public String getDistinguishedName(){
        return distinguishedName;
    }

    public void addMember(User user){
        member.add(user);
    }

    public String getMember (int i){
        return member.get(i).samaccountname;
    }

    public ArrayList<User> getMembers(){
        return member;
    }

    public int getMemberCount(){
        return member.size();
    }

}
