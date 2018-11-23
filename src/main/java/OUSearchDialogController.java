import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class OUSearchDialogController {

    private Stage dialogStage;

    @FXML
    private TableView<OU> ouTable;
    @FXML
    private TableColumn<OU, String> nameColumn;
    @FXML
    private TableColumn<OU, String> distinguishedNameColumn;
    private Server server;
    @FXML
    private TextField searchQueryField;

    private boolean isAnOU;

    private ObservableList<String> options = FXCollections.observableArrayList(
        "Organisation Unit",
        "Group"
    );

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    @FXML
    private ComboBox typeComboBox;


    @FXML
    private void initialize(){
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().getNameProperty());
        distinguishedNameColumn.setCellValueFactory(cellData -> cellData.getValue().getDistinguishedNameProperty());
        typeComboBox.setItems(options);
    }

    public void setServer(Server server){
        this.server = server;
    }
    @FXML
    private void handleSearch() {
        // Check whether we're searching for an OU or a Group
        if (typeComboBox.getSelectionModel().getSelectedIndex()==0) {
            isAnOU = true;
        } else isAnOU = false;

        ObservableList<OU> searchResults = server.uiSearchForOUs(searchQueryField.getText(), isAnOU);
        ouTable.setItems(searchResults);
    }

}
