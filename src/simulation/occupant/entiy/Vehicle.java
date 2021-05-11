package simulation.occupant.entiy;

import common.Coordinate;
import common.Location;
import common.device.proxy.VehicleProxy;
import common.device.state.VehicleCondition;
import simulation.Simulation;
import simulation.occupant.base.Occupant;
import simulation.occupant.base.OccupantType;
import simulation.world.Node;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Vehicle extends Occupant implements VehicleProxy {
    private int capacity = Simulation.getInstance().getVehicleCapacity();
    private VehicleCondition condition = VehicleCondition.OKAY;
    private ConcurrentHashMap<Integer,Guest> passengers = new ConcurrentHashMap<>();
    private LinkedList<Guest> temp;
    private AtomicInteger reserved = new AtomicInteger();
    private int id = 1;

    private String name;

    private FutureTask<Void> currentOperationTask = null;
    private boolean abortDrive;
    private boolean isLoading;

    public Vehicle(String name) {
        super(null, OccupantType.VEHICLE);
        reserved.set(0);
        this.name = name;
    }


    @Override
    public synchronized void enterNode(Occupant occupant) {
        switch (occupant.getOccupantType()){
            case GUEST:
                Guest guest = (Guest)occupant;
                passengers.put(guest.getTokenID(),guest);
                ((Guest)occupant).setVehicle(this);
                break;
            default:
                //do nothing
        }
    }

    int i = 0;
    public synchronized boolean isFull(){
        return  reserved.incrementAndGet() > capacity;
    }





    public synchronized LinkedList<Integer> getPassengers() {
        LinkedList<Integer> tokenIDS = new LinkedList<>();
        passengers.values().forEach((p) -> tokenIDS.add(p.getTokenID()));
        return tokenIDS;
    }
    public Guest getPassengerRaw(int id) {
        return passengers.get(id);
    }

    public void removeGuests() {
        passengers.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        passengers.values().forEach((passenger)-> sb.append(passenger.getTokenID()).append("\n"));
        return "Vehicle" +
                "\n,coordinate=" +coordinate+
                "\npassengers:\n" + sb.toString();
    }



    public VehicleCondition getCondition() {
        return condition;
    }

    public String getName() {
        return name;
    }



    @Override
    public void drive(Coordinate target,Coordinate previousLocation, boolean isEmergency) {
        initializeDrive();
        startDrive(target,previousLocation,isEmergency);
    }
    public void drive(Coordinate target) {
        initializeDrive();
        startDrive(target, null,false);
    }
    private void startDrive(Coordinate target,Coordinate previous,boolean isEmergency) {
        Occupant occupant = this;
        currentOperationTask = new FutureTask<>(() -> {
            LinkedList<Coordinate> route = determineRoute(target,previous,isEmergency);
            abortDrive = false;
            route.forEach((coordinate)->{
                List<Node> path = findPath(this.coordinate, coordinate);
                for(Node n:path) {

                    if(!this.coordinate.equals(n.getCoordinate())){
                        world.get(n.getCoordinate()).enterNode(occupant);
                    }
                    occupant.exitNode();
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (abortDrive) break;
                }
            });
            setLoading(true);
            return null;
        });
        Thread t = new Thread(currentOperationTask);
        t.setDaemon(true);
        t.start();
    }

    private LinkedList<Coordinate> determineRoute(Coordinate target,Coordinate previous,boolean isEmergency) {
        if(this.coordinate.equals(previous)) return new LinkedList<>();
        if(previous == null){
            LinkedList<Coordinate> route = new LinkedList<>();
            route.add(target);
            return route;
        }

        if(Location.getLocation(previous) == Location.EASTSIDE_LOADING_ZONE
                && isEmergency){
            LinkedList<Coordinate> temp = new LinkedList<>(Simulation.getInstance().getPathToWest());
            LinkedList<Coordinate> route = new LinkedList<>();
            Collections.reverse(temp);
            boolean add = false;
            for(Coordinate c:temp){
                if(c.equals(Simulation.getInstance().getWestPoint())|| add){
                    add = true;
                    route.addLast(c);
                };
            }
            route.addLast(Location.EASTSIDE_LOADING_ZONE.getCoordinate());
            return route;
        }

        switch (Objects.requireNonNull(Location.getLocation(target))){
            case EASTSIDE_LOADING_ZONE: return Simulation.getInstance().getPathToEast();
            case WESTSIDE_LOADING_ZONE: return Simulation.getInstance().getPathToWest();
            default:
                LinkedList<Coordinate> route = new LinkedList<>();
                route.add(target);
                return route;
        }
    }

    private void initializeDrive() {
        abortDrive = true;

        if(currentOperationTask != null){
            try {
                currentOperationTask.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    @Override
    public Coordinate getCurrentLocation() {
        return this.coordinate;
    }

    @Override
    public VehicleCondition getCurrentVehicleCondition() {
        return VehicleCondition.OKAY;
    }

    @Override
    public void lockDoors() {

    }

    @Override
    public void unlockDoors() {

    }

    @Override
    public ConcurrentHashMap<Integer, Integer> readRFIDScanner() {
        ConcurrentHashMap<Integer,Integer> temp = new ConcurrentHashMap<>();

        passengers.forEach((k,v)->temp.put(k,k));
        return temp;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public ConcurrentHashMap<Integer,Guest> getPassengerMap(){
        return passengers;
    }

    public void resetReserved() {
        reserved.set(0);
    }
}
