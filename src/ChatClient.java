
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("chatClientGUI.fxml"));
        primaryStage.setTitle("Chatroom");
        primaryStage.setScene(new Scene(root, 700, 400));
        primaryStage.show();

    }

    @FXML
    Label labelName;
    @FXML
    TextField textFieldHost;
    @FXML
    TextField textFieldPort;
    @FXML
    Button buttonConnectToServer;
    @FXML
    ToggleGroup textSizeGroup;
    @FXML
    TextArea textAreaChat;
    @FXML
    TextArea textAreaInput;
    @FXML
    Button buttonSendMessage;

    @FXML
    public void initialize() {

        /*

            textSizeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov, Toggle t, Toggle t1) {
                    ...
                }
            });

            <--------------- After lambda conversion --------------->

            textSizeGroup.selectedToggleProperty().addListener((ov, t, t1) -> {
                ...
            });

        */

        textSizeGroup.selectedToggleProperty().addListener((ov, t, t1) -> {

            RadioButton chk = (RadioButton)t1.getToggleGroup().getSelectedToggle(); // Cast object to radio button
            Font font;
            switch (chk.getText()) {
                case "14px" -> {
                    font = new Font("System", 14);
                    textAreaChat.setFont(font);
                    textAreaInput.setFont(font);
                }
                case "16px" -> {
                    font = new Font("System", 16);
                    textAreaChat.setFont(font);
                    textAreaInput.setFont(font);
                }
                case "18px" -> {
                    font = new Font("System", 18);
                    textAreaChat.setFont(font);
                    textAreaInput.setFont(font);
                }
                default -> {
                    font = new Font("System", 12);
                    textAreaChat.setFont(font);
                    textAreaInput.setFont(font);
                }
            }

        });

        buttonConnectToServer.setOnAction(e -> startClient(textFieldHost.getText(), Integer.parseInt(textFieldPort.getText())));

        Button defaultEmojiButton = new Button();
        defaultEmojiButton.setFont(new Font("System", 16));

    }

    public void startClient(String host, int port) {

        try {
            Socket socket = new Socket(host, port);

            Scanner inputFromServer = new Scanner(socket.getInputStream());
            PrintWriter outputToServer = new PrintWriter(socket.getOutputStream(), true);

            Thread inputFromServerThread = new InputThread(inputFromServer, textAreaChat);
            Thread outputToServerThread = new OutputThread(outputToServer, textAreaInput, buttonSendMessage, labelName, textAreaChat);
            inputFromServerThread.start();
            outputToServerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

    class InputThread extends Thread {

        Scanner inputStream;
        TextArea chatTextArea;
        String lastLine = "";

        public InputThread(Scanner inputStream, TextArea chatTextArea) {
            this.inputStream = inputStream;
            this.chatTextArea = chatTextArea;
        }

        public void run() {

            while (true) {
                try {
                    String line = inputStream.nextLine();
                    if (!line.equals(lastLine)) {
                        chatTextArea.appendText("\n" + line);
                        lastLine = line;
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class OutputThread extends Thread {

        PrintWriter outputStream;
        TextArea inputTextArea;
        TextArea outputTextArea;
        Button sendMessageButton;
        Label labelName;
        boolean nameAcceptance = false;

        public OutputThread(PrintWriter outputStream, TextArea inputTextArea, Button sendMessageButton, Label labelName, TextArea outputTextArea) {
            this.outputStream = outputStream;
            this.inputTextArea = inputTextArea;
            this.sendMessageButton = sendMessageButton;
            this.labelName = labelName;
            this.outputTextArea = outputTextArea;
        }

        public void run() {

            sendMessageButton.setOnAction(event -> {
                try {
                    // int amountOfSpaces = inputTextArea.getText().length() - inputTextArea.getText().replaceAll(" ", "").length();
                    int amountOfSpaces = 0;
                    for(int i = 0; i < inputTextArea.getText().length(); i++) {
                        if(Character.isWhitespace(inputTextArea.getText().charAt(i))) amountOfSpaces++;
                    }

                    /*
                        The first time connecting to a server it will ask for the username,
                        and the rules i have set is that the name can max be 3 words,
                        this i checked through how many spaces exist in the first time writing to the server.
                    */
                    if (outputTextArea.getText().endsWith("SUBMITNAME") && !inputTextArea.getText().isEmpty() && amountOfSpaces < 3) {
                        labelName.setText("Your name: " + inputTextArea.getText());
                        outputStream.println(inputTextArea.getText());
                        sleep(5000);
                        if (outputTextArea.getText().contains("NAMEACCEPTED")) {
                            nameAcceptance = true;
                        }
                    } else if (nameAcceptance) {
                        outputStream.println(inputTextArea.getText());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }

    }
