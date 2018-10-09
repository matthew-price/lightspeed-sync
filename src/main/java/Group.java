import java.util.ArrayList;

public class Group {
    static int id = 0;
    int memberCount = 0;
    String cn;
    String dn;
    String schoolSisId;
    ArrayList<User> member;


    public String getMember(int i) {
        return member.get(i).getSamaccountname();
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
        //this.subGroup.add(subGroup);
    }

    public int getMemberCount(){
        return member.size();
    }


    public Group(String cn, String dn){
        this.cn = cn;
        this.dn = dn;
        member = new ArrayList<User>();
    }

    public void addMember(User user) {
        member.add(user);
        memberCount++;
    }



    public String toString(){
        return cn;
    }

}
