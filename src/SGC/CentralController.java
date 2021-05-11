package SGC;

import common.Coordinate;
import common.RandomTokenGenerator;
import common.device.state.FenceState;
import common.device.state.InitializationState;
import communication.base.Message;
import communication.command.*;
import communication.message.*;
import simulation.Simulation;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class CentralController implements Runnable{
    private BlockingQueue<Message> messageQueue;

    // The messaging queue to send messages to the GUI to update it
    private BlockingQueue<Message> guiMessageQueue;
    private boolean shutDown;
    private boolean isEmergency = false;
    private int password;
    private HashMap<String, SelfDrivingCarController> vehicleControllerMap;
    private HashMap<Integer, TokenKioskController> tokenKioskControllerMap;
    private LoudSpeakerController loudSpeakerController;
    private ExecutorService executorService;
    private Timer timer;
    private TokenStorageManager tokenStorageManager;
    private HashMap<Integer,Integer> receiptIDMap;
    private HashMap<Integer, TokenController> tokenControllerMap;

    public CentralController(BlockingQueue<Message> guiMessageQueue){
        this.messageQueue = new LinkedBlockingQueue<>();
        this.guiMessageQueue = guiMessageQueue;
        this.shutDown = false;
        this.password = 123456789;
        this.vehicleControllerMap = new HashMap<>();
        this.tokenKioskControllerMap = new HashMap<>();
        this.tokenControllerMap = new HashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.timer = new Timer();
        this.loudSpeakerController = new LoudSpeakerController();
        this.tokenStorageManager = new TokenStorageManager();
        this.receiptIDMap = RandomTokenGenerator.getInstance().getTokenIDMap();
    }

    public void start(){
        int vehicleCount = Simulation.getInstance().getTotalCars();
        for (int i = 0; i < vehicleCount; i++){
            String identity = "ID" + i;
            SelfDrivingCarController selfDrivingCarController = new SelfDrivingCarController(identity,messageQueue);
            vehicleControllerMap.put(identity,selfDrivingCarController);
            executorService.submit(selfDrivingCarController);
        }

        executorService.submit(this::initializeCars);

        int numKiosks = 1;
        for (int i = 0; i < numKiosks; i++){
            TokenKioskController tokenKioskController = new TokenKioskController(i,messageQueue);
            tokenKioskControllerMap.put(i, tokenKioskController);
        }


        executorService.submit(loudSpeakerController);

        new TrexTrackerController(messageQueue);

        new ElectricFenceController(messageQueue);

        new RFIDScannerController(messageQueue);

    }

    private void initializeCars() {
        int initialCars  = Simulation.getInstance().getInitialCars();
        for (int i = 0; i < initialCars; i++){
            vehicleControllerMap.get("ID" + i).sendMessage(new VehicleInitialization(InitializationState.LOADING_EAST));
            try {
                TimeUnit.SECONDS.sleep(Simulation.getInstance().getDefaultViewingTime());
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Send a message to the Central Controller
     *
     * This allows any component to send the Central Controller a message such as a status update or other message.
     * This function will be called from the thread of the component. This will allow the central controller to wake
     * up and handle the message.
     */
    public BlockingQueue<Message> getMessageQueue(){
        return this.messageQueue;
    }

    @Override
    public void run(){
        // message handling stuff
        Message message;
        while(!shutDown){
            try{
                message = messageQueue.take();
                processMessage(message);
            } catch(InterruptedException e){
                continue;
            }

        }
    }

    private void processMessage(Message message){
        switch (message.getMessageType()){
            case FENCE_STATUS:
                handleFenceStatusMessage((FenceStatus) message);
                break;
            case T_REX_STATUS:
                handleTrexStatusMessage((TrexStatus) message);
                break;
            case VEHICLE_STATUS:
                handleVehicleStatusMessage(message);
                break;
            case REQUEST_RECEIPTS:
                handleReceiptRequestMessage((RequestReceipts) message);
                break;
            case EMERGENCY_DEESCALATION://Brandon can pop it into my message queue
                handleEmergencyDeEscalationRequest();
                break;
            case EMERGENCY_HANDLED: //Brandon can pop it in my message queue
                sendEmergencyHandled();
                break;
            case TOKEN_INITIALIZED:
                handleTokenInitializedMessage((TokenInitialized) message);
                break;
            case TOKEN_KIOSK_STATUS:
                handleTokenKioskStatusMessage(message);
                break;
            case PASSENGER_REGISTRATION:
                handlePassengerRegisteredMessage((PassengerRegistered) message);
                break;
            case PASSENGER_DEREGISTRATION:
                handlePassengerDeRegistrationMessage((PassengerDeRegistration) message);
                break;
            case PASSENGER_ENTERED_VEHICLE:
                handlePassengerEnteredVehicleMessage((PassengerEntered) message);
                break;
            case PASSENGER_EXITED_VEHICLE:
                handlePassengerExitedVehicleMessage((PassengerExited) message);
                break;
            case GUEST_LOCATION:
                GuestLocation guestLocation = (GuestLocation) message;
                updateDisplay(guestLocation);
                break;
            case AUDIO_BROADCAST:
                handleAudioBroadcastMessage(message);
                break;
            case TOKEN_STATUS:
                handleTokenStatusMessage((TokenStatus)message);
                break;
            case EMERGENCY_OVERRIDE:
                sendEmergencyOverride();
                timer.cancel();
                break;
            case TOKEN_DISPLAY_MESSAGE:
                handleTokenDisplayMessage((TokenDisplayMessage) message);
                break;
            default:
                System.out.println("BIG OOF: " + message.getMessageType());
                break;
        }
    }

    private void handleTokenStatusMessage(TokenStatus tokenStatus) {
        DeviceStatusUpdate deviceStatusUpdate = new DeviceStatusUpdate("Bracelet" + tokenStatus.getIdentity(),
                tokenStatus.getTokenState().toString(), tokenStatus.getTimestamp());
        updateDisplay(deviceStatusUpdate);
    }

    private void handleTokenDisplayMessage(TokenDisplayMessage tokenDisplayMessage) {
        System.out.println(tokenDisplayMessage.getTokenID());
        tokenControllerMap.get(tokenDisplayMessage.getTokenID()).sendMessage(tokenDisplayMessage);
    }

    private void handlePassengerDeRegistrationMessage(PassengerDeRegistration message) {
        PassengerDeRegistration passengerDeRegistration = message;
        System.out.println(passengerDeRegistration.getPassengerToken());
        tokenStorageManager.deRegisterPassenger(passengerDeRegistration.getVehicleName(), passengerDeRegistration.getPassengerToken());
        updateDisplay(message);
    }

    private void handlePassengerRegisteredMessage(PassengerRegistered message) {
        PassengerRegistered passengerRegistered = message;
        tokenStorageManager.registerTokenIdToVehicle(passengerRegistered.getVehicleName(), passengerRegistered.getPassengerToken());
    }

    private void handlePassengerEnteredVehicleMessage(PassengerEntered message){
        updateDisplay(new PassengerEntered(message.getPassengerToken(), "Vehicle " + message.getVehicleName()));
    }

    private void handlePassengerExitedVehicleMessage(PassengerExited message){
        updateDisplay(new PassengerExited(message.getPassengerToken(), "Vehicle " + message.getVehicleName()));
    }

    private void handleTokenKioskStatusMessage(Message message) {
        TokenKioskStatus tokenKioskStatus = (TokenKioskStatus) message;
        DeviceStatusUpdate deviceStatusUpdate4 = new DeviceStatusUpdate("Kiosk " + tokenKioskStatus.getIdentity(),
                tokenKioskStatus.getTokenKioskStatus().toString(), message.getTimestamp());
        updateDisplay(deviceStatusUpdate4);
    }

    private void handleEmergencyDeEscalationRequest() {
        timer.cancel();
    }

    private void handleReceiptRequestMessage(RequestReceipts message) {
        Receipts receipts = new Receipts(receiptIDMap);
        RequestReceipts request = message;
        tokenKioskControllerMap.get(request.getIdentity()).sendMessage(receipts);
    }

    private void handleVehicleStatusMessage(Message message) {
        VehicleStatus vehicleStatus = (VehicleStatus) message;
        String vehicleName = "Vehicle " + vehicleStatus.getVehicleName();
        String vehicleCondition = vehicleStatus.getVehicleCondition().toString();
        Coordinate vehicleCoordinate = vehicleStatus.getCoordinate();
        DeviceStatusUpdate deviceStatusUpdate = new DeviceStatusUpdate(vehicleName,
                vehicleCondition,vehicleCoordinate,message.getTimestamp());
        updateDisplay(deviceStatusUpdate);
    }

    private void handleTokenInitializedMessage(TokenInitialized message) {
        TokenController tokenController = message.getTokenController();
        int tokenID = tokenController.getTokenID();
        tokenControllerMap.put(tokenID, tokenController);
    }

    private void handleTrexStatusMessage(TrexStatus message) {
        String deviceName = "T.rexMonitor";
        String tRexStatus = message.getTrexCondition().toString();
        String bounds = message.getTrexState().toString();
        DeviceStatusUpdate deviceStatusUpdate = new DeviceStatusUpdate(deviceName,
                tRexStatus, bounds, message.getTimestamp());
        updateDisplay(deviceStatusUpdate);
    }

    private void handleFenceStatusMessage(FenceStatus message) {
        FenceStatus fenceStatus = message;
        if (fenceStatus.getStatusType() == FenceState.CRITICAL_FAULT && !isEmergency){
            isEmergency = true;
            updateDisplay(new RequestOperatorEmergency());
            startAutoEvac();
        }
        DeviceStatusUpdate deviceStatusUpdate = new DeviceStatusUpdate("Fence",
                fenceStatus.getStatusType().toString(), message.getTimestamp());
        updateDisplay(deviceStatusUpdate);
    }

    private void handleAudioBroadcastMessage(Message message)
    {try {
        loudSpeakerController.getMessageQueue().put((AudioBroadcast) message);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }}

    private void updateDisplay(Message message){
        try {
            guiMessageQueue.put(message);
        } catch (InterruptedException e) {
            updateDisplay(message);
        }
    }


    private void startAutoEvac(){
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                sendEmergencyOverride();
                timer.cancel();
            }
        };
        timer.schedule(task, 60000);

    }

    private void sendEmergencyHandled(){
        isEmergency = false;
        vehicleControllerMap.values().forEach((vehicle)->vehicle.sendMessage(new EmergencyHandled()));
        tokenKioskControllerMap.values().forEach((kiosk) -> kiosk.sendMessage(new EmergencyHandled()));
        tokenControllerMap.values().forEach((token) -> token.sendMessage(new EmergencyHandled()));

    }

    private void sendEmergencyOverride(){
        vehicleControllerMap.values().forEach((vehicle)->vehicle.sendMessage(new EmergencyOverride()));
        tokenKioskControllerMap.values().forEach((kiosk) -> kiosk.sendMessage(new EmergencyOverride()));
        tokenControllerMap.values().forEach((token) -> token.sendMessage(new EmergencyOverride()));
        try {
            loudSpeakerController.getMessageQueue().put(new AudioBroadcast("resources/audio-broadcast/alarm.mp3"));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void shutDownPark(){
        shutDown = true;
    }



    //Emergency override and emergency de-escalation status,
    // emergency handled, vehicle initialization, emergency override, heartbeat status, passenger list update
}
