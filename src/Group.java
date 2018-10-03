import java.util.ArrayList;

public class Group {
    static int id = 0;
    int memberCount = 0;
    String cn;
    String dn;
    String schoolSisId;
    ArrayList<String> member = new ArrayList<String>();
    ArrayList<String> subGroup = new ArrayList<String>();

    public String getMember(int i) {
        return member.get(i);
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
        if (App.schoolToSisMap.containsKey(cn)){
            this.schoolSisId = App.schoolToSisMap.get(cn).toString();
            this.schoolSisId = "leikskolar";
        }
        else{
            this.schoolSisId = cn.substring(0, App.schoolChars);
       }
        schoolSisId = "TopLevel";
        id++;
    }

    public void addMember(String member) {
        this.member.add(member);
        memberCount++;
    }



    public String toString(){
        return cn;
    }

}
