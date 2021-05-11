package simulation.occupant.entiy;

import common.Coordinate;
import common.Location;
import common.device.proxy.TokenBraceletProxy;
import simulation.occupant.base.Occupant;
import simulation.occupant.base.OccupantType;
import simulation.world.Node;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class Guest extends Occupant implements TokenBraceletProxy {


    private int tokenId = -1;
    private int receiptID;
    private int hiddenID;
    private Runnable walker;
    private boolean abortWalk;

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    private Vehicle vehicle;




    public Guest(int receiptID, int hiddenID) {
        super(null, OccupantType.GUEST);
        this.receiptID = receiptID;
        this.hiddenID = hiddenID;
    }

    public int getHiddenID() {
        return hiddenID;
    }

    public int getReceiptID() {
        return receiptID;
    }

    public boolean hasRFIDToken() {
        return tokenId > -1;
    }

    public void setTokenId(int tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public void enterNode(Occupant occupant) {

    }

    @Override
    public String toString() {
        return "Guest{" +
                "tokenId=" + tokenId +
                ", receiptID=" + receiptID +
                ", coordinate "+ coordinate +
                '}';
    }

    public int getTokenID() {
        return tokenId;
    }

    public void walk(Coordinate target) {

        abortWalk = true;
        walker = ()->{
            abortWalk = false;
            List<Node> path = findPath(coordinate,target);
            for(Node n:path) {
                if(!n.getCoordinate().equals(coordinate)){
                    world.get(n.getCoordinate()).enterNode(this);
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(abortWalk) break;
            }
        };
        Thread driverThread = new Thread(walker);
        driverThread.setDaemon(true);
        driverThread.start();
    }

    @Override
    public void notifyEmergency() {
        if(vehicle != null)this.walk(vehicle.getCurrentLocation());
    }

    @Override
    public void notifyViewingPeriodStart() {
            this.walk(Location.VIEWING_AREA.getCoordinate());
    }

    @Override
    public void notifyViewingPeriodEnd() {
        this.walk(vehicle.getCurrentLocation());
    }

}
