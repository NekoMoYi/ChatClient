package com.nekomoyi.chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import java.io.*;
import java.net.Socket;
import java.util.*;

import com.google.gson.Gson;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class mainWindowController {
    @FXML
    private TextArea textInput;
    @FXML
    private TextField serverInput;
    @FXML
    private TextField portInput;
    @FXML
    private TextField userInput;
    @FXML
    private TextField pwdInput;
    @FXML
    private ScrollPane friendListPane;
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab chatTab;
    @FXML
    private ScrollPane messagePane;
    @FXML
    private Button connectBtn;
    @FXML
    private Button sendBtn;


    private Socket socket;
    private Scanner scanner;
    private PrintWriter writer;
    private Thread thread;
    private static ArrayList<Thread> threads = new ArrayList<>();
    private Gson g = new Gson();
    private String token = "";
    private Integer userId = 0;
    private String currentSession = "";
    private ArrayList<Entities.Friend> friends = new ArrayList<>();
    private ArrayList<Entities.Group> groups = new ArrayList<>();
    private ArrayList<Entities.Message> messages = new ArrayList<>();
    private ArrayList<Entities.Message> tmpMessages = new ArrayList<>();
    private String tmpMessageSession;


    @FXML
    protected void onSendButtonClicked() {
        String msg = textInput.getText();
        sendTextMessage(currentSession ,msg);
        textInput.clear();
    }
    @FXML
    protected void onFileSendButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String path = selectedFile.getAbsolutePath();
            String base64File = ChatUtils.file2b64(path);
            String fileName = selectedFile.getName();
            sendFileMessage(fileName, base64File);
        }
    }
    @FXML
    protected void onTabSelectionChanged() {
        if(tabPane.getSelectionModel().getSelectedItem() == chatTab){
            Platform.runLater(this::updateFriendList);
        }
    }

    @FXML
    protected void updateFriendList() {
        getFriendList();
        getGroupList();
        updateFriendListView();
    }
    @FXML
    protected void updateFriendListView() {
        friendListPane.setContent(new Label("Friend list"));
        VBox vbox = new VBox();
        for (Entities.Friend friend : friends) {
            Button btn = new Button(friend.username);
            btn.prefWidthProperty().bind(friendListPane.widthProperty());
            btn.setOnAction(e -> {
                changeCurrentSession(ChatUtils.getFriendSessionId(userId, friend.id));
            });
            vbox.getChildren().add(btn);
        }
        for (Entities.Group group : groups) {
            Button btn = new Button(group.name);
            btn.prefWidthProperty().bind(friendListPane.widthProperty());
            btn.setStyle("-fx-font-weight: bold");
            btn.setOnAction(e -> {
                changeCurrentSession(ChatUtils.getGroupSessionId(group.id));
            });
            vbox.getChildren().add(btn);
        }

        Button addFriendBtn = new Button("+");
        addFriendBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 10");
        addFriendBtn.prefWidthProperty().bind(friendListPane.widthProperty());
        addFriendBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add friend");
            dialog.setHeaderText("Add friend");
            dialog.setContentText("Add user to contact: ");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(this::addFriend);
        });
        vbox.getChildren().add(addFriendBtn);
        friendListPane.setContent(vbox);
    }
    @FXML
    protected void updateSessionHistory() {
        VBox vbox = new VBox();
        for (Entities.Message message : messages) {
            vbox.getChildren().add(getSingleMessageVBox(message));
        }
        messagePane.setContent(vbox);
        messagePane.vvalueProperty().bind(vbox.heightProperty());
    }
    @FXML
    protected void onImageSendButtonClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.bmp")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            String path = selectedFile.getAbsolutePath();
            String base64Image = ChatUtils.file2b64(path);
            sendImageMessage(base64Image);
        }
    }
    private void sendFileMessage(String fileName, String base64File) {
        Request.SendFileMessageRequest sendFileMessageRequest = new Request.SendFileMessageRequest(token, currentSession, fileName,base64File);
        String json = g.toJson(sendFileMessageRequest);
        writer.println("filemsg;" + Base64.getEncoder().encodeToString(json.getBytes()));
    }
    private void sendImageMessage(String base64Image) {
        // 构建发送图像消息的请求对象
        Request.SendImageMessageRequest sendImageMessageRequest = new Request.SendImageMessageRequest(token, currentSession, base64Image);
        String json = g.toJson(sendImageMessageRequest);
        writer.println("imgmsg;" + Base64.getEncoder().encodeToString(json.getBytes()));
    }

    @FXML
    protected VBox getSingleMessageVBox(Entities.Message message) {
        boolean isSelf = message.sender.id == userId;
        VBox vbox = new VBox();
        Label dateTimeLabel = new Label(ChatUtils.getDatetimeString(message.timestamp));
        dateTimeLabel.setStyle("-fx-text-fill: grey");
        Label senderLabel = new Label(message.sender.username);
        senderLabel.setStyle("-fx-font-weight: bold");
        Label emptyLabel = new Label("");
        switch (message.type) {
            case "text":
                Label contentLabel = new Label(message.content);
                vbox.getChildren().addAll(dateTimeLabel, senderLabel, contentLabel, emptyLabel);
                break;
            case "image":
                byte[] imageBytes = Base64.getDecoder().decode(message.content);
                ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
                Image image = new Image(bis);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(200);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                vbox.getChildren().addAll(dateTimeLabel, senderLabel, imageView, emptyLabel);
                break;
            case "file":
                String fileName = message.content.split("\\|")[0];
                String fileB64 = message.content.split("\\|")[1];
                Label fileLabel = new Label("File: " + fileName);
                Button saveBtn = new Button("Save");
                saveBtn.setOnAction(e -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save file");
                    fileChooser.setInitialFileName(fileName);
                    File selectedFile = fileChooser.showSaveDialog(null);
                    if (selectedFile != null) {
                        try {
                            FileOutputStream fos = new FileOutputStream(selectedFile);
                            fos.write(Base64.getDecoder().decode(fileB64));
                            fos.close();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                });
                vbox.getChildren().addAll(dateTimeLabel, senderLabel, fileLabel, saveBtn, emptyLabel);
                break;
        }
        vbox.prefWidthProperty().bind(messagePane.widthProperty().subtract(20));
        if (isSelf) {
            senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: blue");
            vbox.setAlignment(Pos.CENTER_RIGHT);
        }
        return vbox;
    }
    @FXML
    protected void addMessageToHistory(String sessionId, Entities.Message message){
        if(sessionId.equals(currentSession)){
            messages.add(message);
            VBox messageVBox = getSingleMessageVBox(message);
            VBox vbox = (VBox) messagePane.getContent();
            vbox.getChildren().add(messageVBox);
            messagePane.vvalueProperty().bind(vbox.heightProperty());
        }
    }
    @FXML
    protected void changeCurrentSession(String sessionId) {
        System.out.println("Switched to " + sessionId);
        currentSession = sessionId;
        getSessionHistory(sessionId);
    }
    @FXML
    protected void connectToServer(){
        System.out.println("Connecting to server...");
        System.out.println(userInput.getText() + "@" + serverInput.getText() + ":" + portInput.getText() + " " + pwdInput.getText());
        try{
            if(socket != null) socket.close();
            if(scanner != null) scanner.close();
            if(writer != null) writer.close();
            if(thread != null) thread.interrupt();
            socket = new Socket(serverInput.getText(), Integer.parseInt(portInput.getText()));
            scanner = new Scanner(socket.getInputStream());
            writer = new PrintWriter(socket.getOutputStream(), true);
            thread = new Thread(() -> {
                while (true) {
                    String msg = scanner.nextLine();
                    serverMessageHandler(msg);
                }
            });
            threads.add(thread);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Failed to connect to server.");
        }
        login(userInput.getText(), pwdInput.getText());
    }



    protected void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    protected void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText("Success");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    protected void serverMessageHandler(String msg) {
        String op = msg.split(";")[0];
        String data = msg.split(";")[1];
        data = new String(Base64.getDecoder().decode(data));
        switch(op){
            case "login":
                loginResponse(data);
                break;
            case "getfriends":
                getFriendListResponse(data);
                break;
            case "getgroups":
                getGroupListResponse(data);
                break;
            case "getsession":
                getSessionHistoryResponse(data);
                break;
            case "servermsg":
                serverMessage(data);
                break;
            case "addfriend":
                addFriendResponse(data);
                break;
        }
    }
    protected void login(String username, String password) {
        Request.LoginRequest loginRequest = new Request.LoginRequest(username, password);
        String json = g.toJson(loginRequest);
        writer.println("login;" + Base64.getEncoder().encodeToString(json.getBytes()));
    }

    protected void loginResponse(String data){
        Response.LoginResponse loginResponse = g.fromJson(data, Response.LoginResponse.class);
        if(loginResponse.code == 0)
            Platform.runLater(() -> showAlert("Login failed"));
        else{
            token = loginResponse.token;
            userId = loginResponse.code;
            Platform.runLater(() -> showSuccess("Login success"));
            Platform.runLater(() -> connectBtn.setDisable(true));
            Platform.runLater(() -> userInput.setDisable(true));
            Platform.runLater(() -> pwdInput.setDisable(true));
            Platform.runLater(() -> serverInput.setDisable(true));
            Platform.runLater(() -> portInput.setDisable(true));
            Platform.runLater(() -> sendBtn.setDisable(false));
            Platform.runLater(() -> chatTab.setDisable(false));
            Platform.runLater(() -> tabPane.getSelectionModel().select(chatTab));
            Platform.runLater(this::updateFriendList);
        }
    }


    protected void sendTextMessage(String session, String msg) {
        if (msg.isEmpty() || currentSession.isEmpty()) return;
        Request.SendTextMessageRequest sendTextMessageRequest = new Request.SendTextMessageRequest(token ,session, msg);
        String json = g.toJson(sendTextMessageRequest);
        writer.println("txtmsg;" + Base64.getEncoder().encodeToString(json.getBytes()));
    }

    protected void getFriendList() {
        Request.getFriendListRequest getFriendListRequest = new Request.getFriendListRequest(token);
        String json = g.toJson(getFriendListRequest);
        writer.println("getfriends;" + Base64.getEncoder().encodeToString(json.getBytes()));
    }
    protected void getFriendListResponse(String data){
        Response.getFriendListResponse getFriendListResponse = g.fromJson(data, Response.getFriendListResponse.class);
        if(getFriendListResponse.code == 0)
            Platform.runLater(() -> showAlert("Failed to get friend list"));
        else{
            friends = getFriendListResponse.friends;
            Platform.runLater(this::updateFriendListView);
        }
    }

    protected void getGroupList() {
        Request.getGroupListRequest getGroupListRequest = new Request.getGroupListRequest(token);
        String json = g.toJson(getGroupListRequest);
        writer.println("getgroups;" + Base64.getEncoder().encodeToString(json.getBytes()));
        System.out.println(json);
    }
    protected void getGroupListResponse(String data){
        Response.getGroupListResponse getGroupListResponse = g.fromJson(data, Response.getGroupListResponse.class);
        if(getGroupListResponse.code == 0)
            Platform.runLater(() -> showAlert("Failed to get group list"));
        else{
            groups = getGroupListResponse.groups;
        }
    }
    
    protected void getSessionHistory(String sessionId){
        Request.getSessionHistoryRequest getSessionHistoryRequest = new Request.getSessionHistoryRequest(token, sessionId);
        String json = g.toJson(getSessionHistoryRequest);
        writer.println("getsession;" + Base64.getEncoder().encodeToString(json.getBytes()));
    }

    protected void getSessionHistoryResponse(String data){
        Response.getSessionHistoryResponse getSessionHistoryResponse = g.fromJson(data, Response.getSessionHistoryResponse.class);
        if(getSessionHistoryResponse.code == 0)
            Platform.runLater(() -> showAlert("Failed to get session history"));
        else{
            messages = getSessionHistoryResponse.messages;
            Platform.runLater(this::updateSessionHistory);
        }
    }

    protected void serverMessage(String data) {
        Response.serverMessageResponse serverMessageResponse = g.fromJson(data, Response.serverMessageResponse.class);
        String sessionId = serverMessageResponse.sessionId;
        if(sessionId.equals(currentSession)){
            Platform.runLater(() -> addMessageToHistory(sessionId, serverMessageResponse.message));
        }
    }

    protected void addFriend(String username) {
        Request.addFriendRequest addFriendRequest = new Request.addFriendRequest(token, username);
        String json = g.toJson(addFriendRequest);
        writer.println("addfriend;" + Base64.getEncoder().encodeToString(json.getBytes()));
    }

    protected void addFriendResponse(String data) {
        Response.addFriendResponse addFriendResponse = g.fromJson(data, Response.addFriendResponse.class);
        if(addFriendResponse.code == 0)
            Platform.runLater(() -> showAlert(addFriendResponse.msg));
        else{
            Platform.runLater(() -> showSuccess("Friend added!"));
            Platform.runLater(this::updateFriendList);
        }
    }

    @FXML
    protected void applyFilterToCurrentSession() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Apply filter");
        dialog.setHeaderText("Apply filter");
        dialog.setContentText("Filter: ");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::applyFilter);
    }

    protected void applyFilter(String filter) {
        tmpMessages = new ArrayList<>(messages);
        tmpMessageSession = currentSession;
        messages.clear();
        for (Entities.Message message : tmpMessages) {
            switch (message.type) {
                case "text":
                    if (message.content.contains(filter)) {
                        messages.add(message);
                    }
                    break;
                case "image":
                    break;
                case "file":
                    if (message.content.split("\\|")[0].contains(filter))
                        messages.add(message);
                    break;
            }
        }
        updateSessionHistory();
    }

    @FXML
    protected void cancelFilterToCurrentSession() {
        if (tmpMessages.isEmpty()) return;
        if (tmpMessageSession.equals(currentSession)) {
            messages = new ArrayList<>(tmpMessages);
            updateSessionHistory();
        }
    }
}
