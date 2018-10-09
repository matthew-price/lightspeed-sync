import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

public class Testing {


        // creating static variables
        private static final String COMMA_DELIMITER = ",";
        private static final String NEW_LINE_DELIMITER = "\n";
        private static final String FILE_HEADER = "unique_sis_user_id,username,first_name,last_name,unique_sis_school_id,grade,email,user_type,password,authentication";
        static LdapContext ctx = null;
        static String username = "DARKSPEED\\Administrator";
        static String password = "Jstv979a"; //need to encrypt for real customer environments
        static String address = "ldaps://192.168.19.135:636";
        //static String  groupbaseDN = "DC=rvkskolar,DC=is";


        // create array of user objects
        static ArrayList<User> listOfUsers = new ArrayList<User>();

        // create array of group objects
        static ArrayList<Group> listOfGroups = new ArrayList<Group>();

        public static void createInitialContext(String username, String password, String address) {
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
                System.out.println("Uh oh!");
                e.printStackTrace();
            }
        }

        public static void getServerDetails(){
            //System.out.println("Read from config file? (yes/no)");
            Scanner sc = new Scanner(System.in);
        /*if (sc.nextLine().equals("yes")){
            //DO THE READ THING
        } else {*/
            // System.out.println("Enter server address, e.g. ldaps://192.168.0.1:636 or ldap://192.168.0.1:389");
            // address = sc.nextLine();
            // System.out.println("Enter LDAP username (e.g. DOMAIN\\Username: ");
            // username = sc.nextLine();
            //  System.out.println("Enter password for: " + username);
            // password = sc.nextLine();
            // System.out.println("Enter full search base including OU and DN: ");
            // baseDN = sc.nextLine();
            System.out.println("Enter the GROUP search base including OU and DN: ");
            //  groupbaseDN = sc.nextLine();
            sc.close();
        }

    /*public static void loadServerDetails(){
        try(FileReader fileReader = new FileReader("sync.txt")){
            address = fileReader.read
        }
    }*/

    /*public static void saveServerDetails(){
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter("sync.txt");
            fileWriter.append(address);
            fileWriter.append(NEW_LINE_DELIMITER);
            fileWriter.append(username);
            fileWriter.append(password);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //getListOfGroups();
    }*/

        public static void main(String[] args) {

            // variables for LDAPContext
        /*String username = "DARKSPEED\\Administrator";
        String password = "Jstv979a";
        String address = "ldaps://172.16.254.166:636";*/

            //getServerDetails();
            createInitialContext(username, password, address); // creates the LDAP context
            createUserObjects("DC=darkspeed, DC=local");
            createGroupObjects("DC=darkspeed, DC=local");
            getGroupMembers("DC=darkspeed, DC=local");
            usersCsvWrite();
            groupCSVWrite();
            membershipsCSVWrite();
            //getSubGroups();

        /* temporary group testing
        for (int i = 0; i < listOfGroups.size(); i++){
            System.out.println(listOfGroups.get(i));
        }
        */

        }
        public static void createUserObjects(String baseDN){
            try {
                // Activate paged results
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
                        "title"};
                constraints.setReturningAttributes(attrIDs);
                do {
                    NamingEnumeration answer = ctx.search(baseDN, "(& (samaccountname=*) (givenname=*) (sn=*) (mail=*) (title=*))", constraints);
                    while (answer.hasMoreElements()) {
                        Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                        String samaccountname = attrs.get("samaccountname").get().toString();
                        String givenname = attrs.get("givenname").get().toString();
                        String sn = attrs.get("sn").get().toString();
                        String mail = attrs.get("mail").get().toString();
                        String title = attrs.get("title").get().toString();
                        String cn = "testing";
                        //String tokenGroups = attrs.get("tokenGroups").get().toString();
                        //System.out.println(tokenGroups);
                        User user = new User(sn, givenname, samaccountname, mail, title, cn);
                        listOfUsers.add(user);
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
        public static void usersCsvWrite() {
            System.out.println("Starting CSV write...");
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter("X:\\sis_import\\users-v2.csv");
                fileWriter.append(FILE_HEADER);
                fileWriter.append(NEW_LINE_DELIMITER);
                System.out.println("Size of users array is: " + listOfUsers.size());
                for (int i = 0; i < listOfUsers.size(); i++) {
                    fileWriter.append(listOfUsers.get(i).getSamaccountname());
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append(listOfUsers.get(i).getSamaccountname());
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append(listOfUsers.get(i).getGivenname());
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append(listOfUsers.get(i).getSn());
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append("TopLevel");
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append("1");
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append(listOfUsers.get(i).getMail());
                    fileWriter.append(COMMA_DELIMITER);
                    //user_type field below
                    if(listOfUsers.get(i).title.equals("Nemandi")){
                        fileWriter.append("1");
                    } else {
                        fileWriter.append("2");
                    }
                    fileWriter.append(COMMA_DELIMITER);
                    //password would go below, leaving blank, assuming directory authentication
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append(NEW_LINE_DELIMITER);
                    //auth type would go here, not required
                    System.out.println("CSV appended successfully!");
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //getListOfGroups();
        }
        public static void groupCSVWrite() {
            System.out.println("Starting CSV write...");
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter("groups.csv");
                //fileWriter.append(FILE_HEADER);
                //fileWriter.append(NEW_LINE_DELIMITER);
                System.out.println("Size of groups array is: " + listOfGroups.size());
                for (int i = 0; i < listOfGroups.size(); i++) {
                    // unique SIS group ID
                    fileWriter.append(listOfGroups.get(i).getCN());
                    fileWriter.append(COMMA_DELIMITER);
                    // group name
                    fileWriter.append(listOfGroups.get(i).getCN());
                    fileWriter.append(COMMA_DELIMITER);
                    // group owner SIS ID
                    fileWriter.append(listOfUsers.get(0).getSamaccountname());
                    fileWriter.append(COMMA_DELIMITER);
                    // school SIS ID
                    fileWriter.append(listOfGroups.get(i).getSchoolSisId());
                    fileWriter.append(COMMA_DELIMITER);
                    // private, leaving blank
                    fileWriter.append(COMMA_DELIMITER);
                    fileWriter.append(NEW_LINE_DELIMITER);
                    System.out.println("Group CSV appended successfully!");
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //getListOfGroups();
        }
        public static void membershipsCSVWrite() {
            System.out.println("Starting memberships CSV write...");
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter("X:\\sis_import\\memberships.csv");
                //fileWriter.append(FILE_HEADER);
                //fileWriter.append(NEW_LINE_DELIMITER);
                for (int i = 0; i < listOfGroups.size(); i++) {
                    // get number of members and iterate through them
                    for (int g = 0; g < listOfGroups.get(i).memberCount; g++){
                        // unique SIS group ID
                        fileWriter.append(listOfGroups.get(i).getCN());
                        fileWriter.append(COMMA_DELIMITER);
                        // unique SIS user (member) ID
                        fileWriter.append(listOfGroups.get(i).getMember(g));
                        fileWriter.append(COMMA_DELIMITER);
                        // unique SIS school ID
                        fileWriter.append(listOfGroups.get(i).getSchoolSisId());
                        fileWriter.append(NEW_LINE_DELIMITER);
                        System.out.println("Memberships CSV appended successfully!");
                    }
                }
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //getListOfGroups();
        }
        public static void createGroupObjects(String groupbaseDN){ //creates group objects, and adds memberships
            try {
                SearchControls constraints = new SearchControls();
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                String[] attrIDs = {"cn", "distinguishedName"};
                constraints.setReturningAttributes(attrIDs);
                NamingEnumeration answer = ctx.search(groupbaseDN, "(& (objectCategory=Group) (member=*) (cn=G_ADG*))", constraints);
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

        public static void getGroupMembers(String baseDN){
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
                       // listOfGroups.get(i).addMember(member);
                        System.out.println("Group name: " + listOfGroups.get(i).toString());
                        System.out.println(" contains: " + member);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }

        }

        public static void getSubGroups(){
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


