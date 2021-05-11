package SGC;


import common.Coordinate;
import common.Location;
import common.device.proxy.VehicleProxy;
import common.device.state.BraceletNotification;
import common.device.state.InitializationState;
import common.device.state.VehicleCondition;
import communication.base.Message;
import communication.command.VehicleInitialization;
import communication.message.*;
import simulation.Simulation;

import java.util.concurrent.*;


public class SelfDrivingCarController implements Runnable{
    private final BlockingQueue<Message> CENTRAL_CONTROL_MESSAGE_QUEUE;
    private final BlockingQueue<Message> INTERNAL_MESSAGE_QUEUE;
    private final ExecutorService operationExecutor = Executors.newSingleThreadExecutor();
    private FutureTask<Void> currentOperationTask = null;
    private String identity = "NONE";
    private VehicleOperation currentVehicleOperation;
    private final VehicleProxy vehicleProxy = Simulation.getInstance().getVehicle();
    private boolean shutDown;
    private boolean isEmergency;
    private boolean isOperationAborted = false;
    private final ConcurrentHashMap<Integer,Integer> masterPassengerMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer,Integer> currentPassengerMap = new ConcurrentHashMap<>();
    private final int vehicleCapacity = Simulation.getInstance().getVehicleCapacity();;
    private final int SAFETY_LOADING_PERIOD = Simulation.getInstance().getSafetyLoadingPeriod();
    private final int DEFAULT_LOADING_PERIOD = Simulation.getInstance().getLoadingPeriod();
    private final int VIEWING_PERIOD = Simulation.getInstance().getDefaultViewingTime();
    private Coordinate previousLocation = null;


    public SelfDrivingCarController(String identity,BlockingQueue<Message> messageBlockingQueue){
        this.identity = identity;
        this.CENTRAL_CONTROL_MESSAGE_QUEUE = messageBlockingQueue;
        this.INTERNAL_MESSAGE_QUEUE = new LinkedBlockingQueue<>();
        this.shutDown = false;
        this.isEmergency = false;
        currentVehicleOperation = VehicleOperation.NONE;
        ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeatStatus,0,1000,TimeUnit.MILLISECONDS);
    }



    private void init(InitializationState initializationState){
        switch (initializationState){
            case LOADING_EAST:
                changeOperation(()-> startDrive(Location.EASTSIDE_LOADING_ZONE));
                break;
            case LOADING_WEST:
                changeOperation(()-> startDrive(Location.WESTSIDE_LOADING_ZONE));
                break;
        }
    }

    /**
     * Command a vehicle to drive to a defined destination
     * @param destination
     */
    public void startDrive(Location destination){
        vehicleProxy.lockDoors();
        switch (destination){
            case EASTSIDE_LOADING_ZONE:
                currentVehicleOperation = VehicleOperation.DRIVING_EAST;
                break;
            case WESTSIDE_LOADING_ZONE:
                currentVehicleOperation = VehicleOperation.DRIVING_WEST;
                break;
            case EASTSIDE_PARKING:
                currentVehicleOperation = VehicleOperation.PARKING_EAST;
                break;
            case WESTSIDE_PARKING:
                currentVehicleOperation = VehicleOperation.PARKING_WEST;
                break;
        }
        drive(destination);
    }

    private void drive(Location destination){
        vehicleProxy.drive(destination.getCoordinate(),previousLocation,isEmergency);
        while(!shutDown && !getVehicleLocation().equals(destination.getCoordinate())
                && !isOperationAborted()){
            threadSleep(1);
        }
        if(isOperationAborted()) return;
        arrival(destination);
    }

    private void threadSleep(int i) {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ignored) {}
    }


    private void arrival(Location location) {
        switch (location){
            case WESTSIDE_LOADING_ZONE:
                previousLocation = Location.WESTSIDE_LOADING_ZONE.getCoordinate();
                dropOffGuests(location);
                startViewing();
                break;
            case EASTSIDE_LOADING_ZONE:
                previousLocation = Location.EASTSIDE_LOADING_ZONE.getCoordinate();
                dropOffGuests(location);
                break;
            case EASTSIDE_PARKING:
            case WESTSIDE_PARKING:
                previousLocation = null;
                currentVehicleOperation = VehicleOperation.NONE;
                break;
        }
    }

    private void dropOffGuests(Location location){
        vehicleProxy.unlockDoors();
        switch (location){
            case WESTSIDE_LOADING_ZONE:
                currentVehicleOperation = VehicleOperation.DROPPING_OFF_WESTSIDE;
                dropOff();
                startViewing();
                break;
            case EASTSIDE_LOADING_ZONE:
                currentVehicleOperation = VehicleOperation.DROPPING_OFF_EASTSIDE;
                deregisterPassengers();
                dropOff();
                if(isEmergency) startDrive(Location.EASTSIDE_PARKING);
                else loadGuests(Location.EASTSIDE_LOADING_ZONE);
                break;
        }
    }

    private void dropOff(){
        while (!shutDown && !isOperationAborted()){
            updatePassengers(readRFIDTokens());
            if(currentPassengerMap.size() == 0) break;
        }
        if(isOperationAborted())return;
    }

    private void startViewing(){
        currentVehicleOperation = VehicleOperation.IDLE_VIEWING;
        notifyViewingPeriod(BraceletNotification.VIEWING_PERIOD_START);
        for(int i=VIEWING_PERIOD;i>0;i--){
            if(isOperationAborted())break;
            threadSleep(1);
        }
        if(isOperationAborted()) return;
        notifyViewingPeriod(BraceletNotification.VIEWING_PERIOD_END);
        loadGuests(Location.WESTSIDE_LOADING_ZONE);
    }



    private void notifyViewingPeriod(BraceletNotification braceletNotification) {
        masterPassengerMap.values().forEach((id)->{
            TokenDisplayMessage message = new TokenDisplayMessage(id,braceletNotification);
            try {
                CENTRAL_CONTROL_MESSAGE_QUEUE.put(message);
            } catch (InterruptedException ignored){}
        });
    }

    private void loadGuests(Location location){
        switch(location){
            case WESTSIDE_LOADING_ZONE:
                if(!isEmergency) startAuthenticatedLoading();
                else startEmergencyLoading();
                break;
            case EASTSIDE_LOADING_ZONE:
                startNewLoading();
                break;
        }
    }

    private void startAuthenticatedLoading(){
        currentVehicleOperation = VehicleOperation.LOADING_WESTSIDE;
        while (!shutDown && !isOperationAborted()){
            updatePassengers(readRFIDTokens());
            if(authenticatePassengers() && checkOriginalPassengerCount()){
                break;
            }
        }
        if(isOperationAborted())return;
        startDrive(Location.EASTSIDE_LOADING_ZONE);
    }

    private void startEmergencyLoading(){
        currentVehicleOperation = VehicleOperation.LOADING_WESTSIDE;
        int timeout = 0;
        while (!shutDown && !isOperationAborted()){
            updatePassengers(readRFIDTokens());
            if(timeout > SAFETY_LOADING_PERIOD
                    && currentPassengerMap.size() == 0) timeout = 0;
            if(isFullLenient() || currentPassengerMap.size() > 0
                    && timeout > SAFETY_LOADING_PERIOD) break;
            threadSleep(1);
            timeout++;
        }
        if(isOperationAborted()) return;
        startDrive(Location.EASTSIDE_LOADING_ZONE);
    }

    private void startNewLoading(){
        currentVehicleOperation = VehicleOperation.LOADING_EASTSIDE;
        int timeout = 0;
        while (!shutDown && !isOperationAborted()){
            updatePassengers(readRFIDTokens());
            registerPassengers();
            if(isFullStrict() && timeout > DEFAULT_LOADING_PERIOD
                    || currentPassengerMap.size() > 0 &&  timeout > DEFAULT_LOADING_PERIOD) break;
            threadSleep(1);
            timeout++;
        }
        if(isOperationAborted()) return;
        startDrive(Location.WESTSIDE_LOADING_ZONE);
    }

    private boolean authenticatePassengers() {
        boolean isOriginal = true;
        for(int passenger:currentPassengerMap.values()){
            if(!checkRegistration(passenger)){
                isOriginal = false;
                break;
            }
        }
        return isOriginal;
    }

    private boolean isFullLenient(){
        return currentPassengerMap.size() >= vehicleCapacity;
    }

    private void registerPassengers() {
        currentPassengerMap.values().forEach((passenger)->{
            if(!checkRegistration(passenger)){
                registerPassenger(passenger);
            }
        });
    }

    private void registerPassenger(int tokenID) {
        masterPassengerMap.put(tokenID,tokenID);
        notifyPassengerRegistration(tokenID);
    }

    private void deregisterPassengers(){
        masterPassengerMap.values().forEach((passenger)->{
            try {
                CENTRAL_CONTROL_MESSAGE_QUEUE.put(new PassengerDeRegistration(passenger, identity));
            } catch (InterruptedException ignored) { }
        });
        masterPassengerMap.clear();
    }

    private boolean isFullStrict(){
        return currentPassengerMap.size() == vehicleCapacity;
    }

    private void notifyPassengerRegistration(int tokenID) {
        try {
            CENTRAL_CONTROL_MESSAGE_QUEUE.put(new PassengerRegistered(tokenID, identity));
        } catch (InterruptedException ignored) {}
    }


    private boolean checkRegistration(int tokenID){
        return masterPassengerMap.getOrDefault(tokenID,null) != null;
    }


    private void changeOperation(Operable newOperation){
        endCurrentOperation();
        currentOperationTask = new FutureTask<>(() -> {
            newOperation.execute();
            return null;
        });
        operationExecutor.submit(currentOperationTask);
    }

    private void endCurrentOperation(){
        isOperationAborted = true;
        if(currentOperationTask != null) {
            try {
                currentOperationTask.get();
            } catch (Exception ignored) {}
        }
        isOperationAborted = false;
    }

    private Coordinate getVehicleLocation() {
        return vehicleProxy.getCurrentLocation();
    }

    private boolean isOperationAborted(){
        return isOperationAborted;
    }

    private ConcurrentHashMap<Integer, Integer> readRFIDTokens(){
        return vehicleProxy.readRFIDScanner();
    }

    private void sendHeartbeatStatus(){
        Coordinate coordinate = vehicleProxy.getCurrentLocation();
        VehicleCondition vehicleCondition = vehicleProxy.getCurrentVehicleCondition();
        System.out.println(identity +" "+ currentVehicleOperation.name() + coordinate +" "+currentPassengerMap.size());
        try {
            CENTRAL_CONTROL_MESSAGE_QUEUE.put(new VehicleStatus(identity,vehicleCondition,coordinate));
        } catch (InterruptedException ignored) {}
    }

    private boolean checkOriginalPassengerCount() {
        return currentPassengerMap.size() == masterPassengerMap.size();
    }

    /**
     * Clears and updates the current passenger map.
     * @param scannedPassengers id's scanned by the installed RFID scanner
     */
    private void updatePassengers(ConcurrentHashMap<Integer,Integer> scannedPassengers){
        if(currentPassengerMap.size() < scannedPassengers.size()){
            scannedPassengers.values().forEach((i -> {
                if(currentPassengerMap.getOrDefault(i,null)==null) {
                    try {
                        CENTRAL_CONTROL_MESSAGE_QUEUE.put(new PassengerEntered(i, identity));
                    } catch (InterruptedException ignored) {}
                }
            }));
        }else if(currentPassengerMap.size() > scannedPassengers.size()){
            currentPassengerMap.values().forEach((i -> {
                if(scannedPassengers.getOrDefault(i,null)==null) {
                    try {
                        CENTRAL_CONTROL_MESSAGE_QUEUE.put(new PassengerExited(i, identity));
                    } catch (InterruptedException ignored) {}
                }
            }));
        }
        currentPassengerMap.clear();
        int k = 0;
        for(int i : scannedPassengers.values()){
            if(k < 10){
                currentPassengerMap.put(i, i);
                k++;
            }
        }

    }

    /**
     * This is used by central control to send messages to this component
     * @param message The message being sent from central control
     */
    public void sendMessage(Message message){
        try{
            INTERNAL_MESSAGE_QUEUE.put(message);
        } catch(InterruptedException e){
            // This should not happen, but try again if it did
            sendMessage(message);
        }
    }


    @Override
    public void run(){
        // Do some message handling stuff
        Message message;
        while(!shutDown){
            try{
                message = INTERNAL_MESSAGE_QUEUE.take();
                processMessage(message);
            } catch(InterruptedException ignored){}
            // Blah Blah handling.
        }
    }

    private void processMessage(Message message){
        switch (message.getMessageType()){
            case EMERGENCY_OVERRIDE:
                startEmergency();
                break;
            case EMERGENCY_HANDLED:
                isEmergency = false;
                break;
            case VEHICLE_INITIALIZATION:
                init(((VehicleInitialization) message).getInitializationState());
                break;
            default:
                System.out.println();
        }
    }

    private void startEmergency() {
        isEmergency = true;
        switch (currentVehicleOperation){
            case DRIVING_WEST:
                changeOperation(() -> startDrive(Location.EASTSIDE_LOADING_ZONE));
                break;
            case LOADING_WESTSIDE:
            case DROPPING_OFF_WESTSIDE:
            case IDLE_VIEWING:
                changeOperation(() -> loadGuests(Location.WESTSIDE_LOADING_ZONE));
                break;
        }
    }


    private enum VehicleOperation {
        LOADING_WESTSIDE,
        LOADING_EASTSIDE,
        DRIVING_WEST,
        DRIVING_EAST,
        PARKING_EAST,
        PARKING_WEST,
        DROPPING_OFF_WESTSIDE,
        DROPPING_OFF_EASTSIDE,
        IDLE_VIEWING,
        NONE
    }

    private interface Operable {
        void execute();
    }
}
