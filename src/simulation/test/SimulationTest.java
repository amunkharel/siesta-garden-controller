package simulation.test;

import SGC.CentralController;
import communication.base.Message;
import communication.message.DeviceStatusUpdate;
import javafx.application.Application;
import javafx.stage.Stage;
import simulation.Simulation;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimulationTest extends Application {



    CentralController centralController;
    BlockingQueue<Message> guiMessageQueue = new LinkedBlockingQueue<>();
    BlockingQueue<Message> centralMessagingQueue;


    @Override
    public void start(Stage primaryStage){
        Simulation.getInstance().initialize();
        centralController = new CentralController(guiMessageQueue);
        centralMessagingQueue = centralController.getMessageQueue();
        new Thread(centralController).start();
        centralController.start();
        new Thread(this::readTerminal).start();


    }

    private void readTerminal(){
        Scanner scanner = new Scanner(System.in);
        Simulation.getInstance().start();
        while(true){
            String in = scanner.nextLine();
            switch (in.toUpperCase()){
                case "S":
                    Simulation.getInstance().start();
                    break;
                case "W":
                    Simulation.getInstance().sendNewWave();
                    break;
                default:
                    System.out.println("UNKNOWN INPUT");

            }
        }
    }

    private void readGuiQueue()  {
        while(true){
            Message message = null;
            try {
                message = guiMessageQueue.take();
                processMessage(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }
    private void processMessage(Message message){
        switch(message.getMessageType()){
            case DEVICE_STATUS_UPDATE:
                DeviceStatusUpdate deviceStatusUpdate = (DeviceStatusUpdate)message;

            default:
                //System.out.println("NOT HANDLING MESSAGE OF TYPE: " + message.getMessageType());
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}
