import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class App {

    // creating static variables
    private LdapContext ctx = null;
    private String username;
    private String password;
    private String address;
    private String baseDN;
    private String groupBaseDN;
    private String groupSisId;
    private String sis;
    private int port;
    private boolean ldaps;
    private String outputPath;
    private final String[] attrIDs = {"samaccountname",
            "cn",
            "name",
            "givenname",
            "sn",
            "mail",
            "distinguishedName",
            "member"};

    // create array of user objects
    protected static ArrayList<User> listOfUsers = new ArrayList<User>();

    // create array of group objects
    protected static ArrayList<Group> listOfGroups = new ArrayList<Group>();

    public  String[] getAttrIds(){
        return attrIDs;
    }

    public static void main(String[] args) throws NamingException, IOException{
        App app = new App();
        app.run();
    }

    public void run() throws  NamingException, IOException{
        School school = askForServerDetails();
        Server server = new Server(username, password, address, ldaps, port, baseDN, groupBaseDN, attrIDs);
        server.run(school);
        try {
            CSVWriter csv = new CSVWriter(outputPath, school, true, true, false, true);
            csv.run();
        } catch (IOException ex){
            ex.printStackTrace();
        }

    }


    private School askForServerDetails() {
        System.out.println("Read from config file? (yes/no)");
        String ouToAdd = new String();
        Scanner sc = new Scanner(System.in);
        School school;
        if (sc.nextLine().equals("yes")) {
            readConfigFile();
            school = new School(sis,"a", "parent");
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
            System.out.println("What is the root base DN for users?");
            baseDN = sc.nextLine();

            System.out.println("What is the root base DN for groups?");
            groupBaseDN = sc.nextLine();

            System.out.println("What is the school SIS ID in Lightspeed?");
            sis = sc.nextLine();

            System.out.println("What is the parent group SIS ID in Lightspeed?");
            groupSisId = sc.nextLine();

            school = new School(sis, sis, groupSisId);

            System.out.println("Finally, where should we save the exported files?");
            outputPath = sc.nextLine();
            System.out.println("*****");
            System.out.println("---Saving your settings---");
            System.out.println("*****");
            writeConfigFile();
            }
        sc.close();
        return school;
        }

    private void readConfigFile(){
        System.out.println("Starting read from config.txt");
        try(FileInputStream fi = new FileInputStream("config.txt")){
            ObjectInputStream os = new ObjectInputStream(fi);
            address = (String)os.readObject();
            username = (String)os.readObject();
            password = (String)os.readObject();
            baseDN = (String)os.readObject();
            groupBaseDN = (String)os.readObject();
            sis = (String)os.readObject();

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
            os.writeObject(groupBaseDN);
            os.writeObject(sis);
            os.writeObject(outputPath);
            System.out.println("Config file written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}





