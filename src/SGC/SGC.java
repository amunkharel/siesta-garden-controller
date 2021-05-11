package SGC;

import common.Coordinate;
import communication.base.Message;
import communication.command.EmergencyOverride;
import communication.message.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import simulation.Simulation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SGC extends Application implements Runnable{

    private Stage primaryStage;
    private CentralController centralController;
    private BlockingQueue<Message> guiMessageQueue;
    private BlockingQueue<Message> centralMessagingQueue;
    private HashMap<String, DeviceStatusUpdate> deviceStatuses;
    private int nextRow;

    private AnchorPane simulationPane;
    private GridPane statusTable;
    private Button deEscalateButton;
    private Button evacuateButton;

    double mappingScale = 4.125;
    int mappingXOffset = 60;
    int mappingYOffset = 135;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Simulation.fxml"));
        primaryStage.setTitle("Siesta Gardens Controller");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        this.primaryStage = primaryStage;

        deEscalateButton = (Button) root.lookup("#de-escalate");
        deEscalateButton.setOnMouseClicked(new DeEscalate());
        evacuateButton = (Button) root.lookup("#evacuate");
        evacuateButton.setOnMouseClicked(new Evacuate());
        Button alarmButton = (Button) root.lookup("#triggerAlarm");
        alarmButton.setOnMouseClicked(new PlayAlarm());
        Button announcementButton = (Button) root.lookup("#playAnnouncement");
        announcementButton.setOnMouseClicked(new PlayAnnouncement());
        Button criticalFault = (Button) root.lookup("#triggerCriticalEvent");
        criticalFault.setOnMouseClicked(new TriggerCriticalFault());
        Button guestsArrive = (Button) root.lookup("#guestsArrive");
        guestsArrive.setOnMouseClicked(new NewGuests());


        simulationPane = (AnchorPane) root.lookup("#simulationPane");
        statusTable = (GridPane) root.lookup("#statusTable");
        nextRow = 1;
        deviceStatuses = new HashMap<>();

        this.guiMessageQueue = new LinkedBlockingQueue<>();

        Simulation.getInstance().initialize();
        centralController = new CentralController(guiMessageQueue);
        centralMessagingQueue = centralController.getMessageQueue();
        centralController.start();
        new Thread(centralController).start();
        Simulation.getInstance().start();

        new Thread(this, "GUI-communication").start();


    }


    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This is called when the application GUI stops. It will exit the system
     * and all the associated threads
     */
    @Override
    public void stop(){
        System.exit(0);
    }


    @Override
    public void run(){
        while(!Thread.interrupted()){
            Message message = null;
            try{
                // Grab the latest
                message = guiMessageQueue.take();
            } catch(InterruptedException ignored){}

            if(message == null){
                continue;
            }

            // Do some internal message processing
            switch(message.getMessageType()){
                case DEVICE_STATUS_UPDATE:
                    DeviceStatusUpdate deviceStatusUpdate = (DeviceStatusUpdate) message;
                    updateStatusTable(deviceStatusUpdate);
                    // If this is a vehicle, then grab the coordinates and draw the car
                    if(deviceStatusUpdate.getDeviceName().contains("Vehicle")){
                        updateVehicleLocation(deviceStatusUpdate);
                    }
                    break;
                case GUEST_LOCATION:
                    GuestLocation guestLocation = (GuestLocation) message;
                    updateGuestLocation(guestLocation);
                    break;
                case PASSENGER_ENTERED_VEHICLE:
                    hidePassenger((PassengerEntered) message);
                    break;
                case PASSENGER_EXITED_VEHICLE:
                    showPassenger((PassengerExited) message);
                    break;
                case OPERATOR_EMERGENCY_REQUEST:
                    notifyOperator();
                    break;
                case PASSENGER_DEREGISTRATION:
                    deregisterPassenger((PassengerDeRegistration) message);
                    break;
            }
        }
    }

    private void updateStatusTable(DeviceStatusUpdate message){
        Platform.runLater(() ->{
            String deviceName = message.getDeviceName();
            String deviceNameNoSpaces = deviceName.replace(" ", "_").replace(".", "");
            String deviceStatus = message.getDeviceStatus();
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String updatedTime = dateFormat.format(message.getTimestamp());
            String update = "-updated";
            String stat = "-status";

            // Smart color coding based on status type
            Color statusColor;
            switch(deviceStatus){
                case "OKAY":
                case "HEALTHY":
                case "ONLINE":
                    statusColor = Color.DARKGREEN;
                    break;
                case "STALLED":
                case "SICK":
                case "EMPTY":
                    statusColor = Color.DARKGOLDENROD;
                    break;
                case "BROKEN":
                case "DEAD":
                case "CRITICAL_FAULT":
                    statusColor = Color.RED;
                    break;
                default:
                    statusColor = Color.BLACK;
            }

            if(deviceStatuses.containsKey(deviceNameNoSpaces)){
                // Update the existing GUI component for this device
                Text statusText = (Text) statusTable.lookup("#" + deviceNameNoSpaces + stat);
                Text updatedText = (Text) statusTable.lookup("#" + deviceNameNoSpaces + update);
                statusText.setText(deviceStatus);
                statusText.setFill(statusColor);
                updatedText.setText(updatedTime);
            }
            else{
                Text nameText = new Text(deviceName);
                nameText.setId(deviceNameNoSpaces);

                Text statusText = new Text(deviceStatus);
                statusText.setId(deviceNameNoSpaces + stat);
                statusText.setFill(statusColor);

                Text updatedText = new Text(updatedTime);
                updatedText.setId(deviceNameNoSpaces + update);

                statusTable.add(nameText, 0, nextRow);
                statusTable.add(statusText, 1, nextRow);
                statusTable.add(updatedText, 2, nextRow);

                nextRow++;
            }
            deviceStatuses.put(deviceNameNoSpaces, message);
        });
    }

    private void updateVehicleLocation(DeviceStatusUpdate message){
        Platform.runLater(() -> {
            String deviceName = message.getDeviceName();
            String deviceNameNoSpaces = deviceName.replace(" ", "_").replace(".", "");

            Coordinate coords = message.getCoordinate();

            ImageView car;
            car = (ImageView) simulationPane.lookup("#" + deviceNameNoSpaces + "-img");
            if(car == null){
                car = new ImageView(getClass().getClassLoader().getResource("Car.png").toString());
                car.setId(deviceNameNoSpaces + "-img");
                car.setFitHeight(30);
                car.setFitWidth(30);
                car.setSmooth(true);
                car.setPreserveRatio(true);
                simulationPane.getChildren().add(car);
                // If no location supplied, put them in the main parking lot
                if(coords == null){
                    coords = new Coordinate(50, 30);
                }
            }

            // If there are not coordinates in the message, then do not move the vehicle
            if(coords != null){
                positionElement(coords, car);
            }
        });
    }

    private void updateGuestLocation(GuestLocation guestLocation){
        Platform.runLater(() -> {
            int guestId = guestLocation.getIdentity();

            Coordinate coords = guestLocation.getCoordinate();

            ImageView person;
            person = (ImageView) simulationPane.lookup("#person" + guestId + "-img");
            if(person == null){
                if(guestId % 2 == 0){

                    person = new ImageView(getClass()
                            .getClassLoader()
                            .getResource("male.png")
                            .toString());
                }
                else{

                    person = new ImageView(getClass()
                            .getClassLoader()
                            .getResource("female.png")
                            .toString());
                }
                person.setId("person" + guestId + "-img");
                person.setFitHeight(20);
                person.setFitWidth(20);
                person.setSmooth(true);
                person.setPreserveRatio(true);
                simulationPane.getChildren().add(person);
            }

            positionElement(coords, person);
        });
    }

    private void positionElement(Coordinate coords, ImageView person){
        AnchorPane.setLeftAnchor(person, coords.xCoord() * mappingScale + mappingXOffset);
        AnchorPane.setTopAnchor(person, coords.yCoord() * mappingScale + mappingYOffset);
    }

    private void hidePassenger(PassengerEntered message){
        int guestId = message.getPassengerToken();
        ImageView person = (ImageView) simulationPane.lookup("#person" + guestId + "-img");
        if(person != null){
            person.setVisible(false);
        }
    }

    private void showPassenger(PassengerExited message){
        int guestId = message.getPassengerToken();
        String vehicleName = message.getVehicleName();
        Coordinate vehicleCoords = deviceStatuses.get(vehicleName.replace(" ", "_")).getCoordinate();

        ImageView person = (ImageView) simulationPane.lookup("#person" + guestId + "-img");
        if(person != null){
            // Move the person to where the vehicle is and then make them "pop out" of the vehicle
            positionElement(vehicleCoords, person);
            person.setVisible(true);
        }
    }

    private void deregisterPassenger(PassengerDeRegistration message){
        Platform.runLater(()-> {
            int guestId = message.getPassengerToken();
            ImageView person = (ImageView) simulationPane.lookup("#person" + guestId + "-img");
            if(person != null){
                person.setVisible(false);
                person.setFitWidth(0);
                person.setFitHeight(0);
                positionElement(new Coordinate(-100, -100), person);
                simulationPane.getChildren().remove(person);
            }
        });
    }

    private void notifyOperator(){
        Platform.runLater(() -> {
            // Create a popup telling the operator that the system has detected an emergency and ask them to make a decision
            Alert emergencyAlert = new Alert(Alert.AlertType.ERROR, "The park has detected a failure. Please check the "
                    + "device statuses to determine if this is an actual emergency that warrants an evacuation, or if it "
                    + "was a simple failure that needs to be de-escalated. The system will automatically start evacuating "
                    + "guests if you do not interact manually within sixty seconds.", ButtonType.OK);
            emergencyAlert.setTitle("Emergency Detected");
            emergencyAlert.setHeaderText("Emergency Detected");
            emergencyAlert.initOwner(primaryStage);
            // Make the dialog grow to fit the text
            emergencyAlert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            emergencyAlert.show();
        });

        // Enable the Evacuate and de-escalate buttons
        deEscalateButton.setDisable(false);
        evacuateButton.setDisable(false);
    }

    private class DeEscalate implements EventHandler<MouseEvent> {

        @Override
        public void handle(MouseEvent event){
            try{
                centralMessagingQueue.put(new EmergencyDeesclation());
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private class Evacuate implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event){
            try{
                centralMessagingQueue.put(new EmergencyOverride());
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private class PlayAnnouncement implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event){
            try{
                centralMessagingQueue.put(new AudioBroadcast("resources/audio-broadcast/announcement.mp3"));
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private class PlayAlarm implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event){
            try{
                centralMessagingQueue.put(new AudioBroadcast("resources/audio-broadcast/alarm.mp3"));
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    private class TriggerCriticalFault implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event){
            Simulation.getInstance().breakFence();
        }
    }

    private class NewGuests implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event){
            Simulation.getInstance().sendNewWave();
        }
    }
}
