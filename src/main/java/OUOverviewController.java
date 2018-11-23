import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class OUOverviewController {


    @FXML
    private TableView<OU> ouTable;
    @FXML
    private TableColumn<OU, String> nameColumn;
    @FXML
    private TableColumn<OU, String> typeColumn;

    @FXML
    private Label nameLabel;
    @FXML
    private Label distinguishedNameLabel;
    @FXML
    private Label serverLabel;
    @FXML
    private Label importUsersLabel;
    @FXML
    private Label importGroupsLabel;
    @FXML
    private Label typeLabel;

    private School testSchool;
    private OU testOu;
    private String[] attrIDs = {
        "samaccountname",
        "cn",
        "name",
        "givenname",
        "sn",
        "mail",
        "distinguishedName",
        "member"
    };

    public static ObservableList serverList = FXCollections.observableArrayList();

    // Reference to the main application.
    private MainUI mainApp;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */

    public OUOverviewController(){

    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Initialize the person table with the two columns.
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().getTypeProperty());
        // Clear person details.
        showOuDetails(null);

        // Listen for selection changes and show the person details when changed.
        ouTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showOuDetails(newValue));
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainUI mainApp) {
        this.mainApp = mainApp;
        testSchool = new School("testSchool","testSis","testParent");
        Server server = new Server("DARKSPEED\\Administrator","Jstv979a!!","192.168.101.9",true,636,"dc=darkspeed,dc=local","dc=darkspeed,dc=local",attrIDs);
        testOu = new OU("testOu","testDN",testSchool, true, server);
        mainApp.getOuData().add(testOu);
        ouTable.setItems(mainApp.getOuData());
    }


    private void showOuDetails(OU ou) {
        if (ou != null) {
            // Fill the labels with info from the person object.
            nameLabel.setText(ou.getName());
            //serverLabel.setText(ou.getServer().toString());
            if(ou.returnIsAnOu()){
                typeLabel.setText("Organizational Unit");
            } else{
                typeLabel.setText("Group");
            }
            serverLabel.setText(ou.getServer().toString());
        } else {
            // Person is null, remove all the text.
            nameLabel.setText("");
            typeLabel.setText("");
            serverLabel.setText("");
        }
    }

    @FXML
    private void handleDeletePerson() {
        int selectedIndex = ouTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            ouTable.getItems().remove(selectedIndex);
        } else{
            // Nothing selected.
            Alert alert = new Alert (Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Person Selected");
            alert.setContentText("Please select an OU/group in the table.");

            alert.showAndWait();
        }
    }

    @FXML
    private void handleNewPerson() {
        School school = new School("","","");
        Server server = new Server("DARKSPEED\\Administrator","Jstv979a!!","192.168.101.9",true,636,"dc=darkspeed,dc=local","dc=darkspeed,dc=local",attrIDs);
        OU tempOU = new OU("a","a",school,true, server);
        String[] attrIDs = {"one", "two"};
        boolean okClicked = mainApp.showOuEditDialog(tempOU, serverList);
        if (okClicked) {
            mainApp.getOuData().add(tempOU);
        }
    }

    public void handleAddServer(){
        mainApp.showServerEditDialog();
    }

    /**
     * Called when the user clicks the edit button. Opens a dialog to edit
     * details for the selected person.
     */
    @FXML
    private void handleEditPerson() {
        OU selectedOU = ouTable.getSelectionModel().getSelectedItem();
        if (selectedOU != null) {
            boolean okClicked = mainApp.showOuEditDialog(selectedOU, serverList);
            if (okClicked) {
                showOuDetails(selectedOU);
            }

        } else {
            // Nothing selected.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(mainApp.getPrimaryStage());
            alert.setTitle("No Selection");
            alert.setHeaderText("No Person Selected");
            alert.setContentText("Please select a person in the table.");

            alert.showAndWait();
        }
    }



}
