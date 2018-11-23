import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.IOException;
import java.util.*;

public class Server {

    private String username;
    private String password;
    private String hostname;
    private String address;
    private String groupBaseDN;
    private boolean ldaps;
    private String prefix;
    private int port;
    private String basedn;
    private School school;
    private Group group;
    private String[] attrIDs;
    LdapContext ctx = null;



    public Server(String username, String password, String hostname, boolean ldaps, int port, String basedn, String groupBaseDN, String[] attrIDs){
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.ldaps = ldaps;
        this.port = port;
        this.basedn = basedn;
        this.groupBaseDN = groupBaseDN;
        if (ldaps == true){
            prefix = "ldaps://";
        } else {
            prefix = "ldap://";}
        this.address = prefix + hostname + ":" + port;
        group = new Group("import", "import");
        this.attrIDs = attrIDs;

        //setting up attributes that are allowed to be returned by the server


    }



    protected void run(School school)throws NamingException, IOException{
        connect();
        makeUsers(school);
        makeGroups(school);
        makeOUs(school);
        makeOUMemberships(school);
        makeMemberships(school);
    }

    private void connect(){
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "Simple");
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.PROVIDER_URL, address);
            ctx = new InitialLdapContext(env, null);
            System.out.println("Connection successful");

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

    private void makeUsers(School school) throws NamingException, IOException{

        try {
            // Activate paged results - this is done in case more than a certain number of results are returned (AD by default limits the size of LDAP lookup returns)
            int pageSize = 1000;
            byte[] cookie = null;
            ctx.setRequestControls(new Control[] {new PagedResultsControl(pageSize, Control.NONCRITICAL) });
            int total;
            do {
                SearchControls constraints = new SearchControls();
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
                constraints.setReturningAttributes(attrIDs);
                NamingEnumeration answer = ctx.search(basedn, "(&(samaccountname=*)(givenname=*)(sn=*)(mail=*))", constraints);
                while (answer.hasMoreElements()) {

                    Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                    String samaccountname = attrs.get("samaccountname").get().toString();
                    String givenname = attrs.get("givenname").get().toString();
                    String sn = attrs.get("sn").get().toString();
                    String mail = attrs.get("mail").get().toString();
                    String dn = attrs.get("distinguishedName").get().toString();
                    System.out.println("Found user: " + samaccountname);
                    User user = new User(samaccountname, givenname, samaccountname, mail, dn);
                    school.addUser(samaccountname, user);
                    System.out.println("Adding user: " + samaccountname);
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
                    ctx.setRequestControls(new Control[]{new PagedResultsControl(
                            pageSize, cookie, Control.CRITICAL)});
                } while (cookie != null) ;
        }
        catch (NamingException ex){
            ex.printStackTrace();
        }
        catch (IOException io){
            io.printStackTrace();
        }

    }

    private void makeGroups(School school)throws NamingException, IOException {
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrIDs);
        NamingEnumeration answer = ctx.search(basedn, "(objectClass=Group)", constraints);
        while (answer.hasMoreElements()) {
            Attributes attrs = ((SearchResult) answer.next()).getAttributes();
            String dn = attrs.get("distinguishedName").get().toString();
            String cn = attrs.get("name").get().toString();
            Group newGroup = new Group (cn, dn);
            school.addGroup(newGroup);
            System.out.println("****** ADDED GROUP: " + cn);
        }
    }

    private void makeOUs(School school) throws NamingException{
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrIDs);
        NamingEnumeration answer = ctx.search(basedn, "(objectClass=OrganizationalUnit)", constraints);
        while (answer.hasMoreElements()){
            Attributes attrs = ((SearchResult) answer.next()).getAttributes();
            String name = attrs.get("name").get().toString();
            String dn = attrs.get("distinguishedName").get().toString();
            OU ou = new OU (name, dn, school);
            System.out.println("Adding OU: " + name);
            school.addOU(ou);
        }
    }

    private void makeOUMemberships(School school) throws NamingException, IOException{
        // Activate paged results - this is done in case more than a certain number of results are returned (AD by default limits the size of LDAP lookup returns)
        int pageSize = 1000;
        byte[] cookie = null;
        ctx.setRequestControls(new Control[] {new PagedResultsControl(pageSize, Control.NONCRITICAL) });
        int total;
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrIDs);

        ArrayList<OU> ous = school.getOUs();
        for (int i = 0; i < ous.size(); i++) {
            String ouToCheck = ous.get(i).getDistinguishedName();
            do{
                System.out.println("DISTINGUISHED NAME TO CHECK IS: " + ouToCheck);
                NamingEnumeration answer = ctx.search(ouToCheck, "(&(objectClass=User)(samaccountname=*)(givenname=*)(sn=*)(mail=*))", constraints);
                while (answer.hasMoreElements()) {
                    Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                    String samaccountname = attrs.get("samaccountname").get().toString();
                    System.out.println("TRYING TO ADD AN OU MEMBERSHIP");
                    System.out.println("SEARCHING FOR USER: " + samaccountname);
                    if (school.hasUser(samaccountname) == true) {
                        System.out.println("USER FOUND!");
                        ous.get(i).addMember(school.getUser(samaccountname));
                    }
                }
                Control[] controls = ctx.getResponseControls();
                if (controls != null) {
                    for (int l = 0; l < controls.length; l++) {
                        if (controls[l] instanceof PagedResultsResponseControl) {
                            PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[l];
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
                ctx.setRequestControls(new Control[]{new PagedResultsControl(
                        pageSize, cookie, Control.CRITICAL)});
            } while (cookie != null);
        }
    }


    private void makeMemberships(School school) throws NamingException, IOException{
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrIDs);
        ArrayList<Group> groups = school.getGroups();
        for(int i = 0; i< groups.size(); i++) {
            String groupToCheck = groups.get(i).getDn();
            NamingEnumeration answer = ctx.search(basedn, "(&(objectClass=User)(samaccountname=*)(givenname=*)(sn=*)(mail=*) (memberOf=" + groupToCheck +"))", constraints);
            while (answer.hasMoreElements()) {
                Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                String samaccountname = attrs.get("samaccountname").get().toString();
                System.out.println("TRYING TO ADD A MEMBERSHIP");
                System.out.println("SEARCHING FOR USER: " + samaccountname);
                if (school.hasUser(samaccountname) == true) {
                    System.out.println("USER FOUND!");
                    groups.get(i).addMember(school.getUser(samaccountname));
                }
            }
        }
    }

    private ObservableList<OU> searchForOU(String searchQuery)throws Exception{
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrIDs);
        NamingEnumeration answer = ctx.search(basedn, "(&(ObjectClass=OU)(cn=*"+searchQuery+"*))", constraints);

        ObservableList<OU> searchResults = FXCollections.observableArrayList();
        School searchSchool = new School("searchSchool", "", "");

        while(answer.hasMoreElements()){
            Attributes attrs = ((SearchResult) answer.next()).getAttributes();
            String ouName = attrs.get("distinguishedName").get().toString();
            OU ou = new OU(ouName, ouName, searchSchool);
            searchResults.add(ou);
        }

        return searchResults;
    }

    public ObservableList<OU> uiSearchForOUs(String searchQuery, Boolean isAnOu){
        SearchControls constraints = new SearchControls();
        constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        constraints.setReturningAttributes(attrIDs);

        ObservableList<OU> ouData = FXCollections.observableArrayList();

        // Searching for an OU or a group?
        String type = "";
        if(isAnOu == true){
            type = "OrganizationalUnit";
        } else {
            type = "Group";
        }

        try {
            System.out.println("HOSTNAME is: " + hostname);
            System.out.println("BASEDN is: " + basedn);
            System.out.println("TYPE is: " + type);
            System.out.println("QUERY is: " + searchQuery);
            connect();
            NamingEnumeration answer = ctx.search(basedn, "(&(ObjectClass=" + type + ")(name=" + searchQuery + "))", constraints);
            School searchSchool = new School("searchSchool", "", "");
            System.out.println("After school created");
            while(answer.hasMoreElements()){
                System.out.println("Answer has element!");
                Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                //String ouName = attrs.get("name").get().toString();
                String dn = attrs.get("distinguishedName").get().toString();
                Server server = new Server("DARKSPEED\\Administrator","Jstv979a!!","192.168.101.9",true,636,"dc=darkspeed,dc=local","dc=darkspeed,dc=local", attrIDs);
                OU ou = new OU(dn, dn, searchSchool, isAnOu, server);
                System.out.println("Found OU: "+dn);
                ouData.add(ou);
            }
        } catch (NamingException e){
            e.printStackTrace();
        }

        return ouData;
    }
}

