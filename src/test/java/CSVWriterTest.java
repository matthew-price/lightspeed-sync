import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;


class CSVWriterTest {

    @Test
    void userAdministratorShouldBePresentInUsersHashMap() throws NamingException, IOException {
         final String[] attrIDs = {"samaccountname",
                "cn",
                "name",
                "givenname",
                "sn",
                "mail",
                "distinguishedName",
                "member"};
        School school = new School("sis", "sis", "parent");
        Server server = new Server("DARKSPEED\\Administrator","Testpass!", "192.168.1.110", false, 389, "DC=Darkspeed, DC=Local", "DC=Darkspeed, DC=Local", attrIDs);
        server.run(school);
        assertTrue(school.hasUser("Administrator"));
    }

    @Test
    void shouldBeMoreThan1000UserObjects() throws NamingException, IOException{
         final String[] attrIDs = {"samaccountname",
                "cn",
                "name",
                "givenname",
                "sn",
                "mail",
                "distinguishedName",
                "member"};
        School school = new School("sis", "sis", "parent");
        Server server = new Server("DARKSPEED\\Administrator","Testpass!", "192.168.1.110", false, 389, "DC=Darkspeed, DC=Local", "DC=Darkspeed, DC=Local", attrIDs);
        server.run(school);
        assertTrue(school.countUsers() > 1000);
    }


}