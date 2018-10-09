import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

public class Server {

    String username;
    String password;
    String hostname;
    String address;
    boolean ldaps;
    String prefix;
    int port;
    String basedn;
    School school;
    Group group;

    LdapContext ctx = null;


    public Server(String username, String password, String hostname, boolean ldaps, int port, String basedn, School school){
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.ldaps = ldaps;
        this.port = port;
        this.basedn = basedn;
        this.school = school;
        if (ldaps == true){ prefix = "ldaps://";
        } else { prefix = "ldap://";}
        this.address = prefix + hostname + ":" + port;
        group = new Group("import", "import");
    }



    protected void run(){
        connectToServer();
        LDAPLookup("user", basedn, "(& (samaccountname=*) (givenname=*) (sn=*) (mail=*) (title=*))", school, group);
        LDAPLookup("group", basedn,"(& (objectCategory=Group) (member=*))", school, group);
        ArrayList <Group> groups = school.getGroups();
        for(int i = 0; i < school.countGroups(); i++){
            Group group = school.getGroup(i);
            LDAPLookup("membership", basedn, "(& (samaccountname=*) (objectCategory=User) (memberOf=" + groups.get(i).getDn() + "))", school, groups.get(i));
        }

    }

    private void connectToServer(){

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

    private void LDAPLookup(String searchType, String searchBase, String filter, School school, Group group){

        try {
            // Activate paged results - this is done in case more than a certain number of results are returned (AD by default limits the size of LDAP lookup returns)
            int pageSize = 1000;
            byte[] cookie = null;
            ctx.setRequestControls(new Control[]{new PagedResultsControl(pageSize, Control.NONCRITICAL)});
            int total;


            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {"samaccountname",
                    "cn",
                    "givenname",
                    "sn",
                    "mail",
                    "title",
                    "distinguishedName",
                    "member"};
            constraints.setReturningAttributes(attrIDs);


            do {
                NamingEnumeration answer = ctx.search(basedn, filter, constraints);
                while (answer.hasMoreElements()) {
                    Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                    String samaccountname = attrs.get("samaccountname").get().toString();

                    if (searchType.equals("user")) {
                        String givenname = attrs.get("givenname").get().toString();
                        String sn = attrs.get("sn").get().toString();
                        String mail = attrs.get("mail").get().toString();
                        String title = attrs.get("title").get().toString();
                        String dn = attrs.get("distinguishedName").get().toString();
                        System.out.println("Found user: " + samaccountname);
                        User user = new User(samaccountname, givenname, samaccountname, mail, title, dn);
                        school.addUser(samaccountname, user);

                    } else if (searchType.equals("group")) {
                        String cn = attrs.get("cn").get().toString();
                        String dn = attrs.get("distinguishedName").get().toString();
                        Group newGroup = new Group(cn, dn);
                        school.addGroup(newGroup);

                    } else if (searchType.equals("membership")){
                        System.out.println("TRYING TO ADD A MEMBERSHIP");
                        System.out.println("SEARCHING FOR USER: " + samaccountname);
                        if(school.hasUser(samaccountname) == true){
                            System.out.println("USER FOUND!");
                            group.addMember(school.getUser(samaccountname));
                        }
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
                }
            } while (cookie != null) ;
        }
        catch (NamingException ex){
            ex.printStackTrace();
        }
        catch (IOException io){
            io.printStackTrace();
        }

    }


    }

