import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;

public class OU {

    private StringProperty name;
    private StringProperty distinguishedName;
    private School school;
    ArrayList<User> member;
    private StringProperty type;
    private Server server;
    private boolean isAnOu;


    public OU(String name, String distinguishedName, School school, boolean isAnOu, Server server){
        this.name = new SimpleStringProperty(name);
        this.distinguishedName = new SimpleStringProperty(distinguishedName);
        this.school = school;
        this.isAnOu = isAnOu;
        this.server = server;
        member = new ArrayList<>();
    }

    public OU(String name, String distinguishedName, School school){
        this.name = new SimpleStringProperty(name);
        this.distinguishedName = new SimpleStringProperty(distinguishedName);
        this.school = school;
        member = new ArrayList<>();
    }

    public String getName(){
        return name.get();
    }

    public String getDistinguishedName(){
        return distinguishedName.get();
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

    public StringProperty getNameProperty(){
        return name;
    }

    public StringProperty getDistinguishedNameProperty(){
        return distinguishedName;
    }

    public StringProperty getTypeProperty(){
        return type;
    }

    public boolean returnIsAnOu(){
        return isAnOu;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void setName(String name){
        this.name = new SimpleStringProperty(name);
    }

    public void setDistinguishedName (String distinguishedName){
        this.distinguishedName = new SimpleStringProperty(distinguishedName);
    }
}
