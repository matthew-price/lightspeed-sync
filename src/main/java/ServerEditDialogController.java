import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ServerEditDialogController {

    @FXML
    private TextField hostnameField;
    @FXML
    private CheckBox ldapsCheckBox;
    @FXML
    private TextField portField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField baseDNField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    private Stage dialogStage;


    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleSave(){
        String[] attrIDs = new String[]{
                "samaccountname",
                "cn",
                "name",
                "givenname",
                "sn",
                "mail",
                "distinguishedName",
                "member"};
        OUOverviewController.serverList.add(new Server(usernameField.getText(), passwordField.getText(), hostnameField.getText(), ldapsCheckBox.isSelected(), 636, baseDNField.getText(), baseDNField.getText(),attrIDs));
        dialogStage.close();
    }
}
