import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class OUEditDialogController {

    @FXML
    private TextField ouNameField;
    @FXML
    private TextField distinguishedNameField;
    @FXML
    private Label serverNameLabel;
    @FXML
    private ComboBox<Server> serverComboBox;
    @FXML
    private TextField schoolSisIdField;
    @FXML
    private TextField parentGroupSisIdField;
    @FXML
    private CheckBox shouldImportUsersCheckBox;
    @FXML
    private CheckBox shouldImportGroupsCheckBox;
    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    private ObservableList<Server> listOfServers = FXCollections.observableArrayList();

    private Stage dialogStage;
    private OU ou;
    private boolean okClicked = false;
    private Server activeServer;

    private MainUI mainApp;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

    }

    private ObservableList<String> typesOfOUs = FXCollections.observableArrayList("Organization Unit", "Group");


    @FXML
    private final ComboBox ouTypeComboBox = new ComboBox(typesOfOUs);

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Sets the person to be edited in the dialog.
     *
     * @param ou
     */
    public void setPerson(OU ou) {
        this.ou = ou;

        ouNameField.setText(ou.getName());
        distinguishedNameField.setText(ou.getDistinguishedName());
        ouTypeComboBox.setItems(typesOfOUs);
        if(ou.returnIsAnOu()){
            ouTypeComboBox.getSelectionModel().select(0);
        } else{
            ouTypeComboBox.getSelectionModel().select(1);
        }
    }

    public void setListOfServers(ObservableList<Server> serverList){
        this.listOfServers = serverList;

        serverComboBox.setItems(listOfServers);
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    /**
     * Called when the user clicks ok.
     */
    @FXML
    private void handleOk() {
        if (isInputValid()) {
            ou.setName(ouNameField.getText());
            ou.setDistinguishedName(distinguishedNameField.getText());
            ou.setServer(serverComboBox.getValue());
            okClicked = true;
            dialogStage.close();
        }
    }

    public void setActiveServer(){
        activeServer = serverComboBox.getSelectionModel().getSelectedItem();
    }

    /**
     * Called when the user clicks cancel.
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private void handleSearch(){

    }

    /**
     * Validates the user input in the text fields.
     *
     * @return true if the input is valid
     */
    private boolean isInputValid() {
        String errorMessage = "";
        /*
        if (ouNameField.getText() == null || ouNameField.getText().length() == 0) {
            errorMessage += "No valid OU or group name!\n";
        }
        if (distinguishedNameField.getText() == null || distinguishedNameField.getText().length() == 0) {
            errorMessage += "No valid Distinguished Name!\n";
        }
        if (serverNameField.getText() == null || serverNameField.getText().length() == 0) {
            errorMessage += "No valid server name!\n";
        }

        if (cityField.getText() == null || cityField.getText().length() == 0) {
            errorMessage += "No valid city!\n";
        }
        */

        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Show the error message.
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(dialogStage);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);

            alert.showAndWait();

            return false;
        }
    }

    public void setMainApp(MainUI mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleSearchForOu(){
        setActiveServer();
        mainApp.showOuSearchDialog(activeServer);
    }

}
