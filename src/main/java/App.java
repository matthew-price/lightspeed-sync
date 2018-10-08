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
    private ArrayList<String> staffSearchOUs = new ArrayList<String>();
    private ArrayList<String> studentSearchOUs = new ArrayList<String>();
    private ArrayList<String> groupSearchOUs = new ArrayList<String>();
    private HashMap schoolToSisMap = new HashMap<String, String>();
    private String studentStaffIDMethod;
    private String userTypeSearch;
    private String groupTypeSearch;
    private String oUTypeSearch;
    private String outputPath;
    public static int schoolChars = 10;
    public static String userToSchoolMethod;

    // create array of user objects
    private ArrayList<User> listOfUsers = new ArrayList<User>();
    private HashSet<String> userIsPresent = new HashSet<String>();

    // create array of group objects
    private ArrayList<Group> listOfGroups = new ArrayList<Group>();

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    public void run() {
        askForServerDetails();
        createInitialContext(username, password, address); // creates the LDAP context
        runCreateUserObjects();
        runCreateGroupObjects();
        getGroupMembers("DC=rvkskolar,DC=is");
        usersCsvWrite();
        groupCSVWrite();
        membershipsCSVWrite();
        Server server = new Server();
        server.run();
    }


    private void askForServerDetails() {
        System.out.println("Read from config file? (yes/no)");
        String ouToAdd = new String();
        Scanner sc = new Scanner(System.in);
        if (sc.nextLine().equals("yes")) {
            readConfigFile();
        } else {
            // get server details from user
            System.out.println("Enter server address, e.g. ldaps://192.168.0.1:636 or ldap://192.168.0.1:389");
            address = sc.nextLine();
            System.out.println("Enter LDAP username (e.g. DOMAIN\\Username: ");
            username = sc.nextLine();
            System.out.println("Enter password for: " + username);
            password = sc.nextLine();

            // get base DN details from user
            System.out.println("What is the root base DN? (The search base will be set later): ");
            baseDN = sc.nextLine();
            System.out.println("Please enter the OU you would like to search for STUDENT USERS (e.g. OU=Students,DC=Domain,DC=local: ");
            studentSearchOUs.add(sc.nextLine());
            System.out.println("Please enter the OU you would like to search for STAFF USERS (e.g. OU=Students,DC=Domain,DC=local: ");
            staffSearchOUs.add(sc.nextLine());
            System.out.println("Please enter the base DN you would like to search for GROUPS (e.g. OU=Groups,DC=Domain,DC=local: ");
            groupSearchOUs.add(sc.nextLine());

            // ask whether we should add any more OUs
            String shouldWeAddMoreOUs = new String();
            String ouTypeToAdd = "";
            while (!shouldWeAddMoreOUs.equals("DONE")){
                System.out.println("Would you like to add any more OUs? Please enter an option: \n STUDENT to add another student OU \n STAFF to add another staff OU \n GROUP to add another group OU: \n DONE if finished");
                shouldWeAddMoreOUs = sc.nextLine();
                if(shouldWeAddMoreOUs.equals("STUDENT")){
                    ouTypeToAdd = "studentSearchOUs";
                } else if (shouldWeAddMoreOUs.equals("STAFF")){
                    ouTypeToAdd = "staffSearchOUs";
                } else if (shouldWeAddMoreOUs.equals("GROUP")){
                    ouTypeToAdd = "groupSearchOUs";
                }
                System.out.println("Please enter the OU name: ");
                ouToAdd = sc.nextLine();
                addAnOU(ouTypeToAdd, ouToAdd);

            }




            // assign users to school SIS IDs based on the users preferred method
            System.out.println("How should users be placed into schools? Enter OU to sort by user OU, USERNAME to search based on a string in the user's name, or NONE to place all users into the top level: ");
            userToSchoolMethod = sc.nextLine();

            if(userToSchoolMethod.equals("OU")){
                String addAnother;
                do {
                    System.out.println("Enter the first OU you would like to search for. Please enter the full distinguished name (including CN= etc): \n Enter DONE when finished adding.");
                    String userOU = sc.nextLine();
                    System.out.println("Enter the Lightspeed School SIS ID that you'd like to assign to this OU: ");
                    String SISSchoolID = sc.nextLine();
                    System.out.println("Mapping OU: " + userOU + "\n to SIS ID: " + SISSchoolID + "\n Enter OK to confirm, anything else to cancel.");
                    String confirmMap = sc.nextLine();
                    if (confirmMap.equals("OK")){
                        schoolToSisMap.put(userOU, SISSchoolID);
                        System.out.println("OU to SIS ID map added.");
                    } else {
                        System.out.println("Map not added.");
                    }
                    System.out.println("******* \n Add another 'OU to School SIS ID' mapping? \n Enter DONE if finished anything else to add another.");
                    addAnother = sc.nextLine();
                } while (!addAnother.equals("DONE"));
                System.out.println("Finished adding maps. Total number of maps: " + schoolToSisMap.size());

            } else if(userToSchoolMethod.equals("USERNAME")){
                do {
                    System.out.println("How many characters should we search for in the username (e.g. enter 5 for the first 5 characters");
                    while (!sc.hasNextInt()) { //checking for negative or non-integer input
                        System.out.println("Please enter a number.");
                        sc.next(); //to avoid an infinite loop
                    }
                    schoolChars = sc.nextInt();
                } while (schoolChars <= 0);

            } else {
                System.out.println("All users will be placed into 'TopLevel");
            }
            System.out.println("Finally, where should we save the exported files?");
            outputPath = sc.nextLine();
            System.out.println("*****");
            System.out.println("---Saving your settings---");
            System.out.println("*****");
            writeConfigFile();
            }
            sc.close();
        }

    private void addAnOU(String ouType, String ou){
        Scanner sc = new Scanner(System.in);
        String confirmAdd = "";
        System.out.println("About to add the following OU: \n" + ou + "\n as a " + ouType + " ou. Does this look correct? Enter \"YES\" to accept, anything else to cancel.");
        confirmAdd = sc.nextLine();
        if(confirmAdd.equals("YES")) {
            if (ouType.equals("studentSearchOUs")) {
                studentSearchOUs.add(ou);
                System.out.println("OU Added");
            } else if (ouType.equals("staffSearchOUs")) {
                staffSearchOUs.add(ou);
                System.out.println("OU Added");
            } else if (ouType.equals("groupSearchOUs")) {
                groupSearchOUs.add(ou);
                System.out.println("OU Added");
            } else {
                System.out.println("Failed to add OU - bad OU type");
            }
        } else{
            System.out.println("Not added.");
        }
    }


    private void createInitialContext(String username, String password, String address) {
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "Simple");
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.PROVIDER_URL, address);
            ctx = new InitialLdapContext(env, null);

        } catch (AuthenticationException e){
            System.out.println("Problem with authentication. Bad username/password/security settings?");
            System.out.println("Stack trace follows:");
            e.printStackTrace();
        }
        catch(Exception e){
            System.out.println("Connection to AD failed");
            e.printStackTrace();
        }
    }


    private void askForUserTypeDetails(){
        Scanner sc = new Scanner(System.in);
        System.out.println("How should the system identify student vs staff users?: \n USERNAME for a string at the start of their username \n GROUP for a particular group membership \n OU for users in a particular OU \n NONE to make no attempt to identify student vs staff users: ");
        String identifyDecision = sc.nextLine();
        if(identifyDecision.equals("USERNAME")){
            studentStaffIDMethod = "Username";
            System.out.println("Please enter the string you'd like to search for at the start of each username. Anyone matching the string will be a student user: ");
            userTypeSearch = sc.nextLine();
        }
        if(identifyDecision.equals("GROUP")){
            studentStaffIDMethod = "Group";
            System.out.println("Please enter the full group that you'd like to search for. Anyone in the group will be marked as a student user: ");
            groupTypeSearch = sc.nextLine();
        }
        if(identifyDecision.equals("OU")) {
            studentStaffIDMethod = "OU";
            System.out.println("Please enter the full OU that you'd like to search for. Anyone in the OU will be marked as a student user: ");
            oUTypeSearch = sc.nextLine();
        }
        if(identifyDecision.equals("Title")){
            studentStaffIDMethod = "Title";
            System.out.println("Please enter the full job title that you'd like to search for, e.g. 'Student' (do not enter quotes)");
        }
        if(identifyDecision.equals("NONE")) {
            studentStaffIDMethod = "None";
        }

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
           /* os.writeObject(studentUserOU);
            os.writeObject(staffUserOU);
            os.writeObject(groupDN); */
            os.writeObject(outputPath);
            System.out.println("Config file written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void createUserObjects(String baseDN){
        try {
            // Activate paged results - this is done in case more than a certain number of results are returned (AD by default limits the size of LDAP lookup returns)
            int pageSize = 1000;
            byte[] cookie = null;
            ctx.setRequestControls(new Control[] { new PagedResultsControl(pageSize, Control.NONCRITICAL) });
            int total;
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {"samaccountname",
                    "givenname",
                    "sn",
                    "mail",
                    "title",
                    "distinguishedName"};
            constraints.setReturningAttributes(attrIDs);
            do {
                NamingEnumeration answer = ctx.search(baseDN, "(& (samaccountname=*) (givenname=*) (sn=*) (mail=*) (title=*))", constraints);
                while (answer.hasMoreElements()) {
                    Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                    String samaccountname = attrs.get("samaccountname").get().toString();
                    if(!userIsPresent.contains(samaccountname)) { //check whether the samaccountname already exists, to avoid duplicates
                        String givenname = attrs.get("givenname").get().toString();
                        String sn = attrs.get("sn").get().toString();
                        String mail = attrs.get("mail").get().toString();
                        String title = attrs.get("title").get().toString();
                        String dn = attrs.get("distinguishedName").get().toString();
                        //String tokenGroups = attrs.get("tokenGroups").get().toString();
                        //System.out.println(tokenGroups);
                        User user = new User(sn, givenname, samaccountname, mail, title, dn);
                        listOfUsers.add(user);
                        userIsPresent.add(samaccountname);
                    }
                }

                for (int i = 0; i < listOfUsers.size(); i++) {
                    System.out.println(listOfUsers.get(i));
                }
                Control[] controls = ctx.getResponseControls();
                if (controls != null) {
                    for (int i = 0; i < controls.length; i++) {
                        if (controls[i] instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[i];
                            total = prrc.getResultSize();
                            if (total != 0) {
                                System.out.println("***************** END-OF-PAGE "
                                        + "(total : " + total + ") *****************\n");
                            } else {
                                System.out.println("***************** END-OF-PAGE "
                                        + "(total: unknown) ***************\n");
                            }
                            cookie = prrc.getCookie();
                        }
                    }
                } else {
                    System.out.println("No controls were sent from the server");
                }
                // Re-activate paged results
                ctx.setRequestControls(new Control[] { new PagedResultsControl(
                        pageSize, cookie, Control.CRITICAL) });
            } while (cookie != null);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        }
    private void runCreateUserObjects(){
        for(int i = 0; i < studentSearchOUs.size(); i++){
            createUserObjects(studentSearchOUs.get(i));
        }
    }

    private void runCreateGroupObjects(){
        for (int i = 0; i < groupSearchOUs.size(); i++){
            createGroupObjects(groupSearchOUs.get(i));
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
    private void createGroupObjects(String groupbaseDN){ //creates group objects, and adds memberships
        try {
            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {"cn", "distinguishedName"};
            constraints.setReturningAttributes(attrIDs);
            NamingEnumeration answer = ctx.search(groupbaseDN, "(& (objectCategory=Group) (member=*))", constraints);
            while (answer.hasMoreElements()) {
                // Define attrs as current search return object
                Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                // Set temporary variables
                String cn = attrs.get("cn").get().toString();
                String dn = attrs.get("distinguishedName").get().toString();
                System.out.println("DN is: " + dn);
                System.out.println("CN is: " + cn);
                // Create the new group object, and add it to the ArrayList of groups
                Group group = new Group(cn, dn);
                listOfGroups.add(group);
                // Get number of members for current group and then iterate over it
              //  System.out.println("Size is: " + attrs.get("member").size());
              /*  for (int i = 0; i < attrs.get("member").size(); i++){
                    String member = attrs.get("member").get(i).toString();
                    group.addMember(member);
                } */
            }
        } catch (Exception e){
            System.out.println("LDAP Connection: FAILED");
            e.printStackTrace();
        }
        }

    private void getGroupMembers(String baseDN){
        for(int i = 0; i < listOfGroups.size(); i++){
            // get user members and add them
            String groupDN = listOfGroups.get(i).getDn();
            try {
                SearchControls constraints = new SearchControls();
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                String[] attrIDs = {"samaccountname", "distinguishedName"};
                constraints.setReturningAttributes(attrIDs);
                NamingEnumeration answer = ctx.search(baseDN, "(& (objectCategory=User) (memberOf=" + groupDN + "))", constraints);
                while(answer.hasMoreElements()){
                    Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                    String member = attrs.get("samaccountname").get().toString();
                    listOfGroups.get(i).addMember(member);
                    System.out.println("Group name: " + listOfGroups.get(i).toString());
                    System.out.println(" contains: " + member);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

    }

    private void getSubGroups(){
        for(int i = 0; i<listOfGroups.size(); i++){
            // get list of sub groups
            String groupCN = listOfGroups.get(i).toString();
            System.out.println("Printing current group: " + groupCN);
            try{
                SearchControls constraints = new SearchControls();
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                String[] attrIDs = {"cn"};
                constraints.setReturningAttributes(attrIDs);
                NamingEnumeration answer = ctx.search("DC=darkspeed,DC=local", "(& (objectCategory=Group) (memberOf=" + groupCN + ",OU=Testing Level 2,OU=Testing,DC=darkspeed,DC=local))", constraints);
                while (answer.hasMoreElements()){
                    Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                    String subGroupName = (attrs.get("cn").get().toString());
                    System.out.println("Adding member: " + groupCN);
                    listOfGroups.get(i).setSubGroup(subGroupName);
                    System.out.println(subGroupName);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }


}





