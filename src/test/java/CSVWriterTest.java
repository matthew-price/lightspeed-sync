import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class CSVWriterTest {
    @Test
    void userAdministratorShouldBePresentInUsersHashMap(){
        App app = new App();
        School school = new School("sis", "sis");
        Server server = new Server("DARKSPEED\\Administrator","Testpass!", "172.17.69.63", true, 636, "DC=Darkspeed, DC=Local", school);
        server.run();
        assertTrue(school.hasUser("Administrator"));
    }
}