import com.sun.org.apache.bcel.internal.generic.NEW;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CSVWriter {

    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_DELIMITER = "\n";
    private static final String FILE_HEADER_USERS = "unique_sis_user_id,username,first_name,last_name,unique_sis_school_id,grade,email,user_type,password,authentication";
    private static final String FILE_HEADER_GROUPS = "unique_sis_group_id,group_name,unique_sis_user_id,unique_sis_school_id,sis_parent_group_id,apple_classroom";


    private String outputPath;

    OutputStreamWriter osUsers = null;
    OutputStreamWriter osGroups = null;
    OutputStreamWriter osMemberships = null;

    School school;

    private boolean writingUsers;
    private boolean writingGroups;
    private boolean writingMemberships;
    private boolean writingOUMemberships;


    public CSVWriter(String outputPath, School school, boolean writingUsers, boolean writingGroups, boolean writingMemberships, boolean writingOUMemberships) throws FileNotFoundException, IOException {
        this.outputPath = outputPath;
        this.writingUsers = writingUsers;
        this.writingGroups = writingGroups;
        this.writingMemberships = writingMemberships;
        this.writingOUMemberships = writingOUMemberships;
        this.school = school;
        if (writingUsers == true) {
            osUsers = new OutputStreamWriter(new FileOutputStream(outputPath + "users-v2.csv"), StandardCharsets.UTF_8);
            osUsers.append(FILE_HEADER_USERS);
            osUsers.append(NEW_LINE_DELIMITER);
        }
        if (writingGroups == true){
            osGroups = new OutputStreamWriter(new FileOutputStream(outputPath + "groups.csv"), StandardCharsets.UTF_8);
            osGroups.append(FILE_HEADER_GROUPS);
            osGroups.append(NEW_LINE_DELIMITER);
        }
        if (writingMemberships == true | writingOUMemberships == true){
            osMemberships = new OutputStreamWriter(new FileOutputStream(outputPath + "memberships.csv"), StandardCharsets.UTF_8);
        }
    }



    public void run() throws IOException {

        if (writingUsers == true) {
            HashMap<String, User> users = school.getUsers();
            Iterator it = users.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                appendUsers((User)pair.getValue());
            }
            osUsers.close();
        }

        if (writingGroups == true) {
            ArrayList<Group> groups = school.getGroups();
            for (int i = 0; i < school.countGroups() ; i++){
                Group group = school.getGroup(i);
                appendGroups(group);
            }
            osGroups.close();
        }

        if (writingMemberships == true){
            ArrayList<Group> groups = school.getGroups();
            for (int i = 0; i < school.countGroups(); i++){
                Group group = school.getGroup(i);
                System.out.println("Group " + school.getGroup(i).getCN() + " has " + school.getGroup(i).getMemberCount() + " members.");
                for(int m = 0; m < group.getMemberCount(); m++){
                    appendMemberships(group.getMember(m), group.getCN());
                }
            }
            osMemberships.close();
        }

        if (writingOUMemberships == true){
            ArrayList<OU> ous = school.getOUs();
            for(int i = 0; i < ous.size(); i++){
                OU ou = ous.get(i);
                for(int m = 0; m < ou.getMemberCount(); m++){
                    appendMemberships(ou.getMember(m), ou.getName());
                }
            }
            osMemberships.close();
        }

    }

    private void appendUsers(User user) throws IOException{
        osUsers.append(user.getSamaccountname() + COMMA_DELIMITER + user.getSamaccountname() + COMMA_DELIMITER + user.getGivenname() + COMMA_DELIMITER + user.getSn() + COMMA_DELIMITER + school.getSisID() + COMMA_DELIMITER + user.getGrade() + COMMA_DELIMITER + user.getMail() + COMMA_DELIMITER + user.getType() + COMMA_DELIMITER + user.getPassword() + COMMA_DELIMITER + user.getAuthType());
        osUsers.append(NEW_LINE_DELIMITER);
        System.out.println("APPENDING USER");
    }

    private void appendGroups(Group group) throws IOException{
        osGroups.append(group.getCN() + COMMA_DELIMITER + group.getCN() + COMMA_DELIMITER + "groupOwner" +  COMMA_DELIMITER + school.getSisID() + COMMA_DELIMITER + school.getParentGroupId() + COMMA_DELIMITER);
        osGroups.append(NEW_LINE_DELIMITER);
    }

    public void appendMemberships(String username, String group) throws IOException{
        osMemberships.append(group + COMMA_DELIMITER + username + COMMA_DELIMITER + school.getSisID() + COMMA_DELIMITER + 0);
        osMemberships.append(NEW_LINE_DELIMITER);
        System.out.println("APPENDING MEMBERSHIP");
    }

}
