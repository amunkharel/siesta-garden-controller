package simulation;


import common.Coordinate;
import common.Location;
import common.RandomTokenGenerator;
import common.device.proxy.*;
import communication.base.Message;
import simulation.global.FenceNode;
import simulation.global.RFIDScanner;
import simulation.occupant.delagator.GuestLoadingDelegator;
import simulation.occupant.delagator.VehicleLoadingZoneDelegator;
import simulation.occupant.entiy.Guest;
import simulation.occupant.entiy.Kiosk;
import simulation.occupant.entiy.Vehicle;
import simulation.world.World;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class Simulation {
    private static Simulation simulation;
    private BlockingQueue<Message> commandQueue = new LinkedBlockingQueue<>();
    private LinkedList<TokenKioskProxy> kiosks = new LinkedList<>();
    private LinkedList<VehicleProxy> vehicles = new LinkedList<>();
    private ConcurrentHashMap<Integer,TokenBraceletProxy> tokenBraceletProxyMap = new ConcurrentHashMap<>();
    private LinkedList<FenceNodeProxy> fenceNodeProxies = new LinkedList<>();
    private RFIDScannerProxy rfidScannerProxy;



    private Coordinate westPoint = new Coordinate(50,30);
    private LinkedList<Coordinate> pathToWest = new LinkedList<>();
    private LinkedList<Coordinate> pathToEast = new LinkedList<>();

    private LinkedList<Coordinate> eastSideLoadingNodes = new LinkedList<>();
    private LinkedList<Coordinate> westSideLoadingNodes = new LinkedList<>();

    private LinkedList<Coordinate> eastSideParkingNodes = new LinkedList<>();

    private LinkedList<Guest> guestList = new LinkedList<>();
    private World world;
    private Coordinate guestDelegatorLocation;

    private int guestWaveSize = 40;
    private int totalKiosks = 1;
    private int totalGuests = 100;
    private int totalCars = 2;
    private int initialCars = 2;
    private int defaultViewingTime = 15;
    private int vehicleCapacity = 10;
    private int safetyLoadingPeriod = 15;
    private int loadingPeriod = 15;


    public ConcurrentHashMap<Integer, TokenBraceletProxy> getTokenBraceletProxyMap() {
        return tokenBraceletProxyMap;
    }

    private Simulation(){
        pathToWest.add(new Coordinate(90,80));
        pathToWest.add(new Coordinate(57,84));
        pathToWest.add(new Coordinate(37,63));
        pathToWest.add(new Coordinate(19,62));

        pathToEast.add(new Coordinate(13,21));
        pathToEast.add(new Coordinate(18,13));
        pathToEast.add(new Coordinate(36,6));
        pathToEast.add(new Coordinate(50,19));
        pathToEast.add(new Coordinate(60,40));


        westSideLoadingNodes.add(new Coordinate(11,57));
        westSideLoadingNodes.add(new Coordinate(14,58));
        westSideLoadingNodes.add(new Coordinate(15,61));
        westSideLoadingNodes.add(new Coordinate(21,60));

        eastSideLoadingNodes.add(new Coordinate(65,50));
        eastSideLoadingNodes.add(new Coordinate(63,47));
        eastSideLoadingNodes.add(new Coordinate(63,44));
        eastSideLoadingNodes.add(new Coordinate(61,41));

        eastSideParkingNodes.add(new Coordinate(50,28));
        eastSideParkingNodes.add(new Coordinate(53,32));
        eastSideLoadingNodes.add(new Coordinate(50,36));
        eastSideLoadingNodes.add(new Coordinate(53,40));
    }

    public Coordinate getWestPoint() {
        return westPoint;
    }

    public int getTotalKiosks() {
        return totalKiosks;
    }

    public int getTotalCars() {
        return totalCars;
    }

    public static Simulation getInstance(){
        if(simulation == null) simulation = new Simulation();
        return simulation;
    }
    public void initialize() {
        simulation.addFenceNodeProxy(new FenceNode());
        simulation.addFenceNodeProxy(new FenceNode());
        simulation.addFenceNodeProxy(new FenceNode());

        simulation.setRfidScannerProxy(new RFIDScanner());
        world = new World();
        guestDelegatorLocation = new Coordinate(71,37);




        GuestLoadingDelegator guestLoadingDelegator = new GuestLoadingDelegator();
        guestLoadingDelegator.setWorld(world);
        world.get(guestDelegatorLocation).enterNode(guestLoadingDelegator);
        guestLoadingDelegator.setTargetZones(eastSideLoadingNodes);


        VehicleLoadingZoneDelegator eastParkingDlegator = new VehicleLoadingZoneDelegator();
        eastParkingDlegator.setWorld(world);
        world.get(Location.EASTSIDE_PARKING.getCoordinate()).enterNode(eastParkingDlegator);
        eastParkingDlegator.setTargetZones(eastSideParkingNodes);
        eastParkingDlegator.setOffloadZone(null);

        VehicleLoadingZoneDelegator eastLoadingZoneDelegator = new VehicleLoadingZoneDelegator();
        eastLoadingZoneDelegator.setWorld(world);
        world.get(Location.EASTSIDE_LOADING_ZONE.getCoordinate()).enterNode(eastLoadingZoneDelegator);
        eastLoadingZoneDelegator.setTargetZones(eastSideLoadingNodes);
        eastLoadingZoneDelegator.setOffloadZone(Location.LOADING_DOCK.getCoordinate());


        VehicleLoadingZoneDelegator westLoadingZoneDelegator = new VehicleLoadingZoneDelegator();
        westLoadingZoneDelegator.setWorld(world);
        world.get(Location.WESTSIDE_LOADING_ZONE.getCoordinate()).enterNode(westLoadingZoneDelegator);
        westLoadingZoneDelegator.setTargetZones(westSideLoadingNodes);
        westLoadingZoneDelegator.setOffloadZone(Location.VIEWING_AREA.getCoordinate());




        for(int i=0;i<totalKiosks;i++){
            Kiosk kiosk = new Kiosk();
            kiosk.setTarget(guestDelegatorLocation);
            kiosk.setWorld(world);
            simulation.addKiosk(kiosk);
            world.get(Location.TOKEN_KIOSK.getCoordinate()).enterNode(kiosk);
        }

        for(int i=0;i<totalCars;i++){
            Vehicle vehicle = new Vehicle("Vehicle " + i);
            vehicle.setWorld(world);
            simulation.addVehicle(vehicle);
            world.get(Location.EASTSIDE_PARKING.getCoordinate()).enterNode(vehicle);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        ArrayList<Integer> rec = RandomTokenGenerator.getInstance().getReceiptIds();
        ArrayList<Integer> hiddenID = RandomTokenGenerator.getInstance().getTokenIds();
        for(int i=0;i<totalGuests;i++){
            Guest guest = new Guest(rec.get(i),RandomTokenGenerator.getInstance().getTokenIDMap().get(rec.get(i)));
            guest.setWorld(world);
            simulation.addTokenBracelet(guest,RandomTokenGenerator.getInstance().getTokenIDMap().get(rec.get(i)));
            guestList.add(guest);
        }

    }

    public void breakFence(){
        ((FenceNode)fenceNodeProxies.getFirst()).setCurrentVoltage(Integer.MIN_VALUE);
    }

    public void resetFence(){
        ((FenceNode)fenceNodeProxies.getFirst()).setCurrentVoltage(Integer.MAX_VALUE);
    }

    public void start(){
        sendWave();
    }

    private void sendWave() {
        for(int i = 0; i<guestWaveSize;i++ ){
            Guest guest = guestList.pop();
            world.get(Location.LOADING_DOCK.getCoordinate()).enterNode(guest);
            guest.walk(Location.TOKEN_KIOSK.getCoordinate());
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }
    }

    public void sendNewWave(){
        sendWave();
    }

    public void addVehicle(VehicleProxy vehicle){
        vehicles.add(vehicle);
    }

    public VehicleProxy getVehicle(){
        return vehicles.pop();
    }

    private void addFenceNodeProxy(FenceNodeProxy fenceNodeProxy){
        fenceNodeProxies.add(fenceNodeProxy);
    }

    public LinkedList<FenceNodeProxy> getFenceNodeProxies() {
        return fenceNodeProxies;
    }

    public RFIDScannerProxy getRfidScannerProxy() {
        return rfidScannerProxy;
    }

    private void setRfidScannerProxy(RFIDScannerProxy rfidScannerProxy) {
        this.rfidScannerProxy = rfidScannerProxy;
    }

    private void addKiosk(TokenKioskProxy kiosk){
        kiosks.add(kiosk);
    }

    public TokenKioskProxy getKiosk(){
        return kiosks.pop();
    }

    private void addTokenBracelet(TokenBraceletProxy tokenBraceletProxy,int tokenID){
        tokenBraceletProxyMap.put(tokenID,tokenBraceletProxy);
    }

    public TokenBraceletProxy getTokeBraceletProxy(int tokenID) {
        return tokenBraceletProxyMap.get(tokenID);
    }

    public BlockingQueue<Message> getCommandQueue() {
        return commandQueue;
    }

    public LinkedList<Coordinate> getPathToWest() {
        return pathToWest;
    }

    public LinkedList<Coordinate> getPathToEast() {
        return pathToEast;
    }

    public int getInitialCars() {
        return initialCars;
    }

    public Coordinate getGuestDelegatorLocation() {
        return guestDelegatorLocation;
    }

    public int getDefaultViewingTime() {
        return defaultViewingTime;
    }

    public int getVehicleCapacity() {
        return vehicleCapacity;
    }

    public int getSafetyLoadingPeriod() {
        return safetyLoadingPeriod;
    }

    public int getLoadingPeriod() {
        return loadingPeriod;
    }
}
