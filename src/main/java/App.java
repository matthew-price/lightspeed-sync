import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class App {

    // creating static variables
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_DELIMITER = "\n";
    private static final String FILE_HEADER = "unique_sis_user_id,username,first_name,last_name,unique_sis_school_id,grade,email,user_type,password,authentication";
    private LdapContext ctx = null;
    private String username;
    private String password;
    private String address;
    private String baseDN;
    private int port;
    private boolean ldaps;
    private ArrayList<String> staffSearchOUs = new ArrayList<String>();
    private ArrayList<String> studentSearchOUs = new ArrayList<String>();
    private ArrayList<String> groupSearchOUs = new ArrayList<String>();
    private HashMap schoolToSisMap = new HashMap<String, String>();
    private String studentStaffIDMethod;
    private String userTypeSearch;
    private String groupTypeSearch;
    private String oUTypeSearch;
    private String outputPath;

    // create array of user objects
    protected static ArrayList<User> listOfUsers = new ArrayList<User>();

    // create array of group objects
    protected static ArrayList<Group> listOfGroups = new ArrayList<Group>();

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    public void run() {
        askForServerDetails();
        Server server = new Server(username, password, address, ldaps, port, baseDN);
        server.run();
        usersCsvWrite();
        groupCSVWrite();
        membershipsCSVWrite();
    }


    private void askForServerDetails() {
        System.out.println("Read from config file? (yes/no)");
        String ouToAdd = new String();
        Scanner sc = new Scanner(System.in);
        if (sc.nextLine().equals("yes")) {
            readConfigFile();
        } else {
            // get server details from user
            System.out.println("Enter server IP address or hostname, e.g. 192.168.0.1 or dc1.local");
            address = sc.nextLine();

            System.out.println("Enter the port number (usually 389 or 636): ");
            port = sc.nextInt();
            sc.nextLine();

            System.out.println("Are you using a secure connection to your LDAP server (LDAPS)?: ");
            ldaps = sc.nextBoolean();
            sc.nextLine();

            System.out.println("Enter LDAP username (e.g. DOMAIN\\Username: ");
            username = sc.nextLine();

            System.out.println("Enter password for: " + username);
            password = sc.nextLine();

            // get base DN details from user
            System.out.println("What is the root base DN? (The search base will be set later): ");
            baseDN = sc.nextLine();
            System.out.println("Finally, where should we save the exported files?");
            outputPath = sc.nextLine();
            System.out.println("*****");
            System.out.println("---Saving your settings---");
            System.out.println("*****");
            writeConfigFile();
            }
            sc.close();
        }

    private void readConfigFile(){
        System.out.println("Starting read from config.txt");
        try(FileInputStream fi = new FileInputStream("config.txt")){
            ObjectInputStream os = new ObjectInputStream(fi);
            address = (String)os.readObject();
            username = (String)os.readObject();
            password = (String)os.readObject();
            baseDN = (String)os.readObject();
            /*studentUserOU = (String)os.readObject();
            staffUserOU = (String)os.readObject();
            groupDN = (String)os.readObject(); */
            outputPath = (String)os.readObject();
            System.out.println("Config file read successfully.");
            System.out.println("****Address is: " + address);
            System.out.println("*****Base DN is: " + baseDN);


        } catch(IOException e){
            System.out.println("Reading from config file failed.");
            askForServerDetails();
        } catch (ClassNotFoundException e){
            e.printStackTrace();
            System.out.println("Attempting to continue");
            askForServerDetails();
        }
    }

    private void writeConfigFile(){
        System.out.println("Starting config write...");
        try (FileOutputStream fs = new FileOutputStream("config.txt")) {
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(address);
            os.writeObject(username);
            os.writeObject(password);
            os.writeObject(baseDN);
            os.writeObject(outputPath);
            System.out.println("Config file written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void usersCsvWrite() {
        System.out.println("Starting CSV write...");
        OutputStreamWriter os = null;
        try {
            os = new OutputStreamWriter(new FileOutputStream(outputPath+"users-v2.csv"), StandardCharsets.UTF_8);
            os.append(FILE_HEADER);
            os.append(NEW_LINE_DELIMITER);
            System.out.println("Size of users array is: " + listOfUsers.size());
            for (int i = 0; i < listOfUsers.size(); i++) {
                os.append(listOfUsers.get(i).getSamaccountname());
                os.append(COMMA_DELIMITER);
                os.append(listOfUsers.get(i).getSamaccountname());
                os.append(COMMA_DELIMITER);
                os.append(listOfUsers.get(i).getGivenname());
                os.append(COMMA_DELIMITER);
                os.append(listOfUsers.get(i).getSn());
                os.append(COMMA_DELIMITER);
                os.append("TopLevel");
                os.append(COMMA_DELIMITER);
                os.append("1");
                os.append(COMMA_DELIMITER);
                os.append(listOfUsers.get(i).getMail());
                os.append(COMMA_DELIMITER);
                //user_type field below
                if(listOfUsers.get(i).title.equals("Nemandi")){
                    os.append("1");
                } else {
                    os.append("2");
                }
                os.append(COMMA_DELIMITER);
                //password would go below, leaving blank, assuming directory authentication
                os.append(COMMA_DELIMITER);
                os.append(NEW_LINE_DELIMITER);
                //auth type would go here, not required
                System.out.println("CSV appended successfully!");
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //getListOfGroups();
    }

    private void groupCSVWrite() {
        System.out.println("Starting CSV write...");
        OutputStreamWriter os = null;
        try {
            os = new OutputStreamWriter(new FileOutputStream(outputPath+"groups.csv"), StandardCharsets.UTF_8);
            //fileWriter.append(FILE_HEADER);
            //fileWriter.append(NEW_LINE_DELIMITER);
            System.out.println("Size of groups array is: " + listOfGroups.size());
            for (int i = 0; i < listOfGroups.size(); i++) {
                // unique SIS group ID
                os.append(listOfGroups.get(i).getCN());
                os.append(COMMA_DELIMITER);
                // group name
                os.append(listOfGroups.get(i).getCN());
                os.append(COMMA_DELIMITER);
                // group owner SIS ID
                os.append(listOfUsers.get(0).getSamaccountname());
                os.append(COMMA_DELIMITER);
                // school SIS ID
                os.append(listOfGroups.get(i).getSchoolSisId());
                os.append(COMMA_DELIMITER);
                // private, leaving blank
             //   os.append(COMMA_DELIMITER);
                os.append(NEW_LINE_DELIMITER);
                System.out.println("Group CSV appended successfully!");
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //getListOfGroups();
    }

    private void membershipsCSVWrite() {
        System.out.println("Starting memberships CSV write...");
        OutputStreamWriter os = null;
        try {
            os = new OutputStreamWriter(new FileOutputStream(outputPath+"memberships.csv"), StandardCharsets.UTF_8);
            //fileWriter.append(FILE_HEADER);
            //fileWriter.append(NEW_LINE_DELIMITER);
            for (int i = 0; i < listOfGroups.size(); i++) {
                // get number of members and iterate through them
                for (int g = 0; g < listOfGroups.get(i).memberCount; g++){
                    // unique SIS group ID
                    os.append(listOfGroups.get(i).getCN());
                    os.append(COMMA_DELIMITER);
                    // unique SIS user (member) ID
                    os.append(listOfGroups.get(i).getMember(g));
                    os.append(COMMA_DELIMITER);
                    // unique SIS school ID
                    os.append(listOfGroups.get(i).getSchoolSisId());
                    os.append(NEW_LINE_DELIMITER);
                    System.out.println("Memberships CSV appended successfully!");
                }
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //getListOfGroups();
    }



}





