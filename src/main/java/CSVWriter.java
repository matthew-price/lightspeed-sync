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

    private String outputPath;

    private String samaccountname;
    private String givenname;
    private String sn;
    private String schoolSisId;
    private String grade;
    private String mail;
    private String userType;
    private String password;
    private String userAuthType;
    OutputStreamWriter osUsers = null;
    OutputStreamWriter osGroups = null;
    OutputStreamWriter osMemberships = null;

    School school;

    private boolean writingUsers;
    private boolean writingGroups;
    private boolean writingMemberships;


    public CSVWriter(String outputPath, School school, boolean writingUsers, boolean writingGroups, boolean writingMemberships) throws FileNotFoundException {
        this.outputPath = outputPath;
        this.writingUsers = writingUsers;
        this.writingGroups = writingGroups;
        this.writingMemberships = writingMemberships;
        this.school = school;
        if (writingUsers == true) {
            osUsers = new OutputStreamWriter(new FileOutputStream(outputPath + "users-v2.csv"), StandardCharsets.UTF_8);
        }
        if (writingGroups == true){
            osGroups = new OutputStreamWriter(new FileOutputStream(outputPath + "groups.csv"), StandardCharsets.UTF_8);
        }
        if (writingMemberships == true){
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
    }

    private void appendUsers(User user) throws IOException{
        System.out.println("APPENDUSER");
        System.out.println("APPENDUSER IS: " + user.toString());
        osUsers.append(user.getSamaccountname() + COMMA_DELIMITER + user.getSamaccountname() + COMMA_DELIMITER + user.getGivenname() + COMMA_DELIMITER + user.getSn() + COMMA_DELIMITER + school.getSisID() + COMMA_DELIMITER + user.getGrade() + COMMA_DELIMITER + user.getMail() + COMMA_DELIMITER + user.getType() + COMMA_DELIMITER + user.getPassword() + COMMA_DELIMITER + user.getAuthType());
        osUsers.append(NEW_LINE_DELIMITER);
    }

    private void appendGroups(Group group) throws IOException{
        osGroups.append(group.getCN() + COMMA_DELIMITER + group.getCN() + COMMA_DELIMITER + "groupOwner" +  COMMA_DELIMITER + school.getSisID() + COMMA_DELIMITER);
        osGroups.append(NEW_LINE_DELIMITER);
    }
}
