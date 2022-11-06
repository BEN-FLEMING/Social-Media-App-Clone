package jabberserver;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class JabberController implements Initializable {
    @FXML
    private Label lblStatus;
    @FXML
    private Label timelineLbl;
    @FXML
    private Label loginLbl;
    @FXML
    private Button signInBtn;
    @FXML
    private TextField txtUser;
    @FXML
    private AnchorPane refreshPane;
    @FXML
    private Button refreshBtn;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private ScrollPane whoToFollowPane;
    @FXML
    private HBox jabHBox;
    @FXML
    private TextArea jabText;
    @FXML
    private Button registerBtn;
    @FXML
    private Label whoToFollowLbl;
    private final int PORT =44444;
    public Socket socket = new Socket("localhost", PORT);

    public JabberController() throws IOException {


    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    signInBtn.setText("Sign In");
    timelineLbl.setText("");
    setAllVisible(false);
    jabText.clear();


    }

    public void signInAndSignOut(ActionEvent event) {

        if(signInBtn.getText().equals("Sign In")&& !txtUser.getText().isEmpty())
        {
            try {

                //getting sign in as jabbermessage
                JabberMessage getSignIn = new JabberMessage("signin "+ txtUser.getText());
                //message sent to the server
                 ObjectOutputStream send = new ObjectOutputStream( socket.getOutputStream());

                send.writeObject(getSignIn);
                send.flush();
                //System.out.println(getSignIn.getMessage());

                //getting the response from the server
                JabberMessage getResponse = null;
                ObjectInputStream response = new ObjectInputStream(socket.getInputStream());
                getResponse = (JabberMessage) response.readObject();

                if(getResponse.getMessage().equals("signedin")){
                    lblStatus.setText("You are signed in");
                    signInBtn.setText("Sign Out");
                    timelineLbl.setText("Timeline");
                    loginLbl.setText("Welcome "+ txtUser.getText());
                    txtUser.setVisible(false);
                    setAllVisible(true);
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                        updateTimeline();
                        updateWhoToFollow();
                    }));
                    timeline.setCycleCount(Animation.INDEFINITE);
                    timeline.play();
                }
                else if(getResponse.getMessage().equals("unknown-user")){
                    lblStatus.setText("Login is incorrect");
                    Stage dialogStage = new Stage();
                    FXMLLoader loader = new FXMLLoader();
                    try {
                        Pane root = loader.load(Objects.requireNonNull(getClass().getResource("IncorrectUsername.fxml")).openStream());
                        dialogStage.setTitle("JABBER");
                        dialogStage.setScene(new Scene(root, 250, 100));
                        dialogStage.show();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
                else
                {
                    lblStatus.setText("You are NOT signed in");
                    Stage stage = new Stage();
                    Stage dialogStage = new Stage();
                    FXMLLoader loader = new FXMLLoader();
                    try {
                        Pane root = loader.load(Objects.requireNonNull(getClass().getResource("IncorrectUsername.fxml")).openStream());
                        dialogStage.setTitle("JABBER");
                        dialogStage.setScene(new Scene(root, 250, 100));
                        dialogStage.show();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else if(signInBtn.getText().equals("Sign Out")&& !txtUser.getText().isEmpty()) {
            JabberMessage sendSignOut = new JabberMessage("signout");

            try {
                //message sent to the server
                ObjectOutputStream sendCredentials = new ObjectOutputStream(socket.getOutputStream());
                sendCredentials.writeObject(sendSignOut);
                sendCredentials.flush();
                ((Stage) (((Button) event.getSource()).getScene().getWindow())).close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            lblStatus.setText("An error has occured");
    }
    public void register(ActionEvent event){
        if(!txtUser.getText().isEmpty()) {
            try {
                //getting textfield as new user
                JabberMessage sendNewUser = new JabberMessage("register " + txtUser.getText());

                //message sent to the server
                ObjectOutputStream sendCredentials = new ObjectOutputStream(socket.getOutputStream());
                sendCredentials.writeObject(sendNewUser);
                sendCredentials.flush();

                //getting the response from the server
                ObjectInputStream response = new ObjectInputStream(socket.getInputStream());
                JabberMessage getResponse = null;

                getResponse = (JabberMessage) response.readObject();

                if (getResponse.getMessage().equals("signedin")) {
                    lblStatus.setText("You are now signed in and registered");
                    signInBtn.setText("Sign Out");
                    timelineLbl.setText("Timeline");
                    loginLbl.setText("Welcome " + txtUser.getText());
                    txtUser.setVisible(false);
                    setAllVisible(true);
                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                        updateTimeline();
                        updateWhoToFollow();
                    }));
                    timeline.setCycleCount(Animation.INDEFINITE);
                    timeline.play();
                } else {
                    lblStatus.setText("An error has occured");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
            lblStatus.setText("An error has occured");
    }


    public ArrayList<ArrayList<String>> getTimeline(){
        JabberMessage timelineMsg = new JabberMessage("timeline");
        ArrayList<ArrayList<String>> data =  new ArrayList<ArrayList<String>>();

        try {
            //message sent to the server
            ObjectOutputStream send = new ObjectOutputStream(socket.getOutputStream());
            send.writeObject(timelineMsg);
            send.flush();

            //getting the response from the server
            ObjectInputStream response = new ObjectInputStream(socket.getInputStream());
            JabberMessage getResponse = (JabberMessage) response.readObject();
            for(int i= getResponse.getData().size()-1 ; i>=0; i--){
                data.add(getResponse.getData().get(i));
            }
        }
        catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return data;
    }


    public void updateTimeline(){
        GridPane gridPane = new GridPane();
        ArrayList<ArrayList<String>> timeline=getTimeline();
        if(getTimeline()!=null) {
            for (int i = 0; i < timeline.size(); i++) {
                Label user = new Label();
                user.setText(timeline.get(i).get(0)+": ");
                user.wrapTextProperty();
                user.setStyle("-fx-font: 20 arial;");
                gridPane.add(user,0,i);
                Button likeButton = new Button();
                ImageView heart = new ImageView("heart.png");
                likeButton.setText(timeline.get(i).get(3));
                heart.setPreserveRatio(true);
                heart.setFitHeight(20);
                likeButton.setGraphic(heart);
                likeButton.setId(timeline.get(i).get(2));
                gridPane.add(likeButton, 1, i);
                Label userJab = new Label();
                userJab.setStyle("-fx-font: 20 arial;");
                userJab.setText(timeline.get(i).get(1));
                userJab.wrapTextProperty();
                gridPane.add(userJab,2,i);
                likeButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent)
                    {
                        String jabid = likeButton.getId();
                        lblStatus.setText("You have just liked a jab");
                        try {
                            //message sent to the server
                            JabberMessage likeJab = new JabberMessage("like "+jabid);
                            ObjectOutputStream send = new ObjectOutputStream(socket.getOutputStream());
                            send.writeObject(likeJab);
                            send.flush();

                            //getting the response from the server
                            ObjectInputStream response = new ObjectInputStream(socket.getInputStream());
                            JabberMessage getResponse = (JabberMessage) response.readObject();
                            if(getResponse.getMessage().equals("posted")) {
                               updateTimeline();
                            }
                        }
                        catch(IOException | ClassNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        timeline.clear();
        scrollPane.setContent(gridPane);
    }

    public void updateWhoToFollow(){
        GridPane followPane = new GridPane();
        ArrayList<ArrayList<String >> nonFollowers  =getWhoToFollow();
        if(nonFollowers!=null) {
            for (int i = 0; i < nonFollowers.size(); i++) {
                Label user = new Label();
                user.setText(nonFollowers.get(i).get(0)+": ");
                user.wrapTextProperty();
                followPane.add(user,0,i);
                Button followButton = new Button();
                ImageView heart = new ImageView("follow.jpg");
                heart.setPreserveRatio(true);
                heart.setFitHeight(20);
                followButton.setGraphic(heart);
                followButton.setId(nonFollowers.get(i).get(0));
                followPane.add(followButton, 1, i);
                followButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent)
                    {
                        String userName = followButton.getId();
                        lblStatus.setText("You have just followed "+userName);
                        try {
                            //message sent to the server
                            JabberMessage follow = new JabberMessage("follow "+ userName);
                            ObjectOutputStream send = new ObjectOutputStream(socket.getOutputStream());
                            send.writeObject(follow);
                            send.flush();

                            //getting the response from the server
                            ObjectInputStream response = new ObjectInputStream(socket.getInputStream());
                            JabberMessage getResponse = (JabberMessage) response.readObject();
                            if(getResponse.getMessage().equals("posted")) {
                                updateTimeline();
                                updateWhoToFollow();
                            }
                        }
                        catch(IOException | ClassNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                });

            }
        }
        whoToFollowPane.setContent(followPane);
        nonFollowers.clear();

    }

    public ArrayList<ArrayList<String>> getWhoToFollow(){
        JabberMessage newFollower = new JabberMessage("users");
        ArrayList<ArrayList<String>> data =  new ArrayList<ArrayList<String>>();

        try {
            //message sent to the server
            ObjectOutputStream send = new ObjectOutputStream(socket.getOutputStream());
            send.writeObject(newFollower);
            send.flush();

            //getting the response from the server
            ObjectInputStream response = new ObjectInputStream(socket.getInputStream());
            JabberMessage getResponse = (JabberMessage) response.readObject();
            data= getResponse.getData();
        }
        catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
        return data;
    }

    public void addJab(ActionEvent event){
        JabberMessage newJab = new JabberMessage("post " +jabText.getText());

        if(!jabText.getText().isEmpty()) {
            try {
                //message sent to the server
                ObjectOutputStream send = new ObjectOutputStream(socket.getOutputStream());
                send.writeObject(newJab);
                send.flush();
                //getting the response from the server
                ObjectInputStream response = new ObjectInputStream(socket.getInputStream());
                JabberMessage getResponse = (JabberMessage) response.readObject();
                if (getResponse.getMessage().equals("posted")) {
                    updateTimeline();
                    jabText.clear();
                    lblStatus.setText("You have just posted a jab");
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            lblStatus.setText("An error occured please wait");
        }

    }

    public void setAllVisible(boolean value){
        scrollPane.setVisible(value);
        whoToFollowPane.setVisible(value);
        jabHBox.setVisible(value);
        whoToFollowLbl.setVisible(value);
        registerBtn.setVisible(!value);
    }

}
