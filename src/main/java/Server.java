import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.util.Hashtable;

public class Server {

    String username;
    String password;
    String hostname;
    boolean ldaps;
    int port;
    String school;
    String basedn;


    LdapContext ctx = null;


    public Server(String username, String password, String hostname, boolean ldaps, int port, String school, String basedn){
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.ldaps = ldaps;
        this.port = port;
        this.school = school;
        this.basedn = basedn;
    }

    protected void run(){
        connectToServer();



    }

    private void connectToServer(){

        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, "Simple");
            env.put(Context.SECURITY_PRINCIPAL, username);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.PROVIDER_URL, hostname);
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

    private void LDAPLookup(String objectType, String searchBase){

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


    }


}
