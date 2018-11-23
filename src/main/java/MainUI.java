import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainUI extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private ObservableList<OU> ouData = FXCollections.observableArrayList();
    private School school = new School("school", "sis", "parent");
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

    public MainUI(){
        Server server = new Server("DARKSPEED\\Administrator","Jstv979a!!","192.168.101.9",true,636,"dc=darkspeed,dc=local","dc=darkspeed,dc=local", attrIDs);
        ouData.add(new OU("name", "distinguishedName", school, true, server));
    }


    public ObservableList<OU> getOuData(){
        return ouData;
    }

    public void start(Stage primaryStage){
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AD Sync Tool");

        initRootLayout();

        showOUOverview();
    }



    public void initRootLayout(){
        try{
            //Load root layout from fxml file
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainUI.class.getResource("RootLayout.fxml"));
            rootLayout = loader.load();

            //Show the scene containing the root layout
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public void showOUOverview(){
        try{
            //Load OU overview
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainUI.class.getResource("OUOverview.fxml"));
            AnchorPane ouOverview = loader.load();

            //Set OU overview into the centre of the root layout
            rootLayout.setCenter(ouOverview);

            //Give the controller access to the main app
            OUOverviewController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public boolean showOuEditDialog(OU ou, ObservableList<Server> serverList) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainUI.class.getResource("OUEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit OU");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            OUEditDialogController controller = loader.getController();
            controller.setMainApp(this);
            controller.setDialogStage(dialogStage);
            controller.setPerson(ou);
            controller.setListOfServers(serverList);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void showServerEditDialog(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainUI.class.getResource("ServerEditDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Server");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            ServerEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void showOuSearchDialog(Server server){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainUI.class.getResource("OUSearchDialog.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Object Search");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            OUSearchDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setServer(server);

            dialogStage.showAndWait();

        } catch (IOException e){
            e.printStackTrace();
        }
    }


    public Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
