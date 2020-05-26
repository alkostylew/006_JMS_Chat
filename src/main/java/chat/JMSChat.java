package chat;

import java.io.File;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.sun.xml.internal.ws.message.stream.StreamMessage;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
 
public class JMSChat extends Application {

	private MessageProducer messageProducer;
	private Session session;
	private String codeUser;
	
	public static void main(String[] args) {
		Application.launch(JMSChat.class);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("JMS Chat");
		BorderPane borderPane = new BorderPane();
		
		HBox xBox = new HBox(); 
		xBox.setPadding(new Insets(10)); 
		xBox.setSpacing(10);
		xBox.setBackground(new Background(new BackgroundFill(Color.YELLOW, 
				CornerRadii.EMPTY, Insets.EMPTY)));
		Label labelCode = new Label("Code:");
		TextField textFieldCode = new TextField("C1");
		textFieldCode.setPromptText("Code");
		
		Label labelHost = new Label("Host:");
		TextField textFieldHost = new TextField("localhost");
		textFieldHost.setPromptText("Host");
		
		Label labelPort = new Label("Port:");
		TextField textFieldPort = new TextField("61616");
		textFieldPort.setPromptText("Port");
		
		Button buttonConnecter = new Button("Connecter");
		xBox.getChildren().add(labelCode);
		xBox.getChildren().add(textFieldCode);
		xBox.getChildren().add(labelHost);
		xBox.getChildren().add(textFieldHost);
		xBox.getChildren().add(labelPort);
		xBox.getChildren().add(textFieldPort);
		xBox.getChildren().add(buttonConnecter);
		 
		borderPane.setTop(xBox);
		
		VBox vBox = new VBox();
		GridPane gridPane = new GridPane();
		HBox xBox2 = new HBox();
		vBox.getChildren().addAll(gridPane, xBox2); 
		borderPane.setCenter(vBox);
		
		Label labelTo = new Label("To:");
		TextField textFieldTo = new TextField("C1"); textFieldTo.setPrefWidth(250);
		Label labelMessage = new Label("Message:");
		TextArea textAreaMessage = new TextArea(); textAreaMessage.setPrefWidth(250);
		Button buttonEnvoyer = new Button("Envoyer");
		Label labelImage = new Label("Image:");
		
		File f = new File("images");
		ObservableList<String> observableListImages = FXCollections.observableArrayList(f.list());		
		ComboBox<String> comboBoxImages = new ComboBox<String>(observableListImages);
		comboBoxImages.getSelectionModel().select(0);
		Button buttonEnvoyerImage = new Button("Envoyer Image");
		
		gridPane.setPadding(new Insets(10)); 
		textAreaMessage.setPrefRowCount(2);
		gridPane.setVgap(10); gridPane.setHgap(10);
		gridPane.add(labelTo, 0, 0); gridPane.add(textFieldTo, 1, 0);
		gridPane.add(labelMessage, 0, 1); gridPane.add(textAreaMessage, 1, 1); gridPane.add(buttonEnvoyer,2,1);
		gridPane.add(labelImage, 0, 2); gridPane.add(comboBoxImages, 1, 2); gridPane.add(buttonEnvoyerImage, 2, 2);
		
		ObservableList<String> observableListMessages = FXCollections.observableArrayList();
		ListView<String> listViewMessages = new ListView<String>(observableListMessages);
		
		File f2 = new File("images/"+comboBoxImages.getSelectionModel().getSelectedItem());
		Image image = new Image(f2.toURI().toString()); 
		ImageView imageView = new ImageView(image);
		imageView.setFitWidth(320); imageView.setFitHeight(240);
		xBox2.getChildren().addAll(listViewMessages, imageView);
		xBox2.setPadding(new Insets(10)); xBox2.setSpacing(10);		
		
		Scene scene = new Scene(borderPane, 800, 500);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		comboBoxImages.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				File f3 = new File("images/"+newValue);
				Image image = new Image(f3.toURI().toString());
				imageView.setImage(image);
			}
		});
		
		buttonEnvoyer.setOnAction(evt -> {
			try {
				TextMessage textMessage = session.createTextMessage();
				textMessage.setStringProperty("code", textFieldTo.getText());
				textMessage.setText(textAreaMessage.getText());
				messageProducer.send(textMessage);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		buttonConnecter.setOnAction(event -> {
			try {
				codeUser = textFieldCode.getText();
				String host = textFieldHost.getText();
				int port = Integer.parseInt(textFieldPort.getText());
				ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
						"tcp://"+host+":"+port);
				Connection connection = connectionFactory.createConnection();
				connection.start();
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				Destination destination = session.createTopic("enset.chat");
				MessageConsumer messageConsumer = session.createConsumer(destination, "code='"+codeUser+"'");
				messageProducer = session.createProducer(destination);
				messageProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				messageConsumer.setMessageListener(message -> {
					try {
						if (message instanceof TextMessage) {
							TextMessage textMessage = (TextMessage) message;
							observableListMessages.add(textMessage.getText());
						} else if (message instanceof StreamMessage) {
							
						}
					} catch (JMSException e) {
						e.printStackTrace();
					}
				});
				xBox.setDisable(true);
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}); 
	}

}
