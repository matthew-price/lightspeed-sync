import java.util.ArrayList;

public class Group {
    static int id = 0;
    int memberCount = 0;
    String cn;
    String dn;
    String schoolSisId;
    ArrayList<User> member = new ArrayList<User>();
    ArrayList<String> subGroup = new ArrayList<String>();

    public String getMember(int i) {
        return member.get(i).toString();
    }

    public String getDn() {
        return dn;
    }

    public String getCN() {
        return cn;
    }


    public String getSchoolSisId() {
        return schoolSisId;
    }

    public void setSubGroup(String subGroup){
        this.subGroup.add(subGroup);
    }




    public Group(String cn, String dn){
        this.cn = cn;
        this.dn = dn;
    }

    public void addMember(User user) {
        member.add(user);
    }



    public String toString(){
        return cn;
    }

}
