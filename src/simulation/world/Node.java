package simulation.world;

import common.Coordinate;
import simulation.Simulation;
import simulation.global.RFIDScanner;
import simulation.occupant.base.None;
import simulation.occupant.base.Occupant;
import simulation.occupant.base.OccupantType;
import simulation.occupant.entiy.Guest;
import simulation.occupant.entiy.Vehicle;

public class Node  {
    private RFIDScanner rfidScanner = (RFIDScanner) Simulation.getInstance().getRfidScannerProxy();
    private Coordinate coordinate;
    private Occupant occupant;



    public Node() {
    }

    public Node(Coordinate coordinate) {
        this.coordinate = coordinate;
        this.occupant = new None(this);
    }

    public void setOccupant(Occupant occupant){
        this.occupant = occupant;
        occupant.setCurrentNode(this);
    }

    public synchronized void enterNode(Occupant occupant){
        occupant.setCoordinate(coordinate);
        this.occupant.enterNode(occupant);
        updateTrackers(occupant);
    }

    public void resetNode(){
        this.occupant = new None(this);
    }

    private void updateTrackers(Occupant occupant){
        switch (occupant.getOccupantType()){
            case VEHICLE:
                Vehicle vehicle = this.occupant.getOccupantType() == OccupantType.VEHICLE ? (Vehicle)this.occupant:(Vehicle) occupant;
                vehicle.getPassengers().forEach((i)->{
                    vehicle.getPassengerRaw(i).setCoordinate(coordinate);
                    rfidScanner.putGuestData(i,coordinate);
                });
                break;
            case GUEST:
                Guest guest = (Guest) occupant;

                if(guest.hasRFIDToken()||guest.getTokenID()==-50) rfidScanner.putGuestData(guest.getTokenID(),coordinate);
                break;
        }
    }

    @Override
    public String toString() {
        return "Node{" +
                "coordinate=" + coordinate +
                '}';
    }

    public Occupant getOccupant() {
        return occupant;
    }


    public Coordinate getCoordinate() {
        return coordinate;
    }
}
