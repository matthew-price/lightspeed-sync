public class User {

    String dn;
    String sn;
    String givenname;
    String samaccountname;
    String mail;
    String title;
    int userType;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public User(String sn, String givenname, String samaccountname, String mail, String title, String dn){
        this.sn = sn;
        this.givenname = givenname;
        this.samaccountname = samaccountname;
        this.mail = mail;
        this.title = title;
        this.dn = dn;
        System.out.println("******** DN is: " + dn);
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getGivenname() {
        return givenname;
    }

    public void setUserType(int userType){
        this.userType = userType;
    }

    public void setGivenname(String givenname) {
        this.givenname = givenname;
    }

    public String getSamaccountname() {
        return samaccountname;
    }

    public void setSamaccountname(String samaccountname) {
        this.samaccountname = samaccountname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    @Override
    public String toString() {
        return "SN: " + sn + " Given Name: " + givenname + " SAMAccountName: "  + samaccountname + " Email: " + mail + " Title: " + title;
    }
}
