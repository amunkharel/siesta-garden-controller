package simulation.occupant.delagator;

import common.Coordinate;
import common.Location;
import simulation.occupant.base.Occupant;
import simulation.occupant.base.OccupantType;
import simulation.occupant.entiy.Guest;
import simulation.occupant.entiy.Vehicle;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GuestLoadingDelegator extends Delegator {
    private LinkedBlockingQueue<Guest> waitingGuests = new LinkedBlockingQueue<>();
    private boolean offline;

    public GuestLoadingDelegator() {
        super( OccupantType.EASTSIDE_GUEST_LOADING_DELEGATOR);
       // Executors.newSingleThreadExecutor().submit(this::tryDelegateWaitingGuests);
    }


    @Override
    public  void enterNode(Occupant occupant) {
        switch (occupant.getOccupantType()){
            case GUEST:
                delegate(occupant);
                break;
            default:

        }
    }


    private BlockingQueue<Guest> guestQueue = new LinkedBlockingQueue<>();
    @Override
    protected synchronized void delegate(Occupant occupant) {
        Guest guest = (Guest)occupant;
        if(offline){
            guest.walk(Location.LOADING_DOCK.getCoordinate());
        }
        Runnable delegating = () -> {
            boolean assigned = false;
            while(!assigned && !offline){
                for(Coordinate coord: targetZones){
                Occupant occupant1 = world.get(coord).getOccupant();
                if(occupant1.getOccupantType() == OccupantType.VEHICLE) {
                        Vehicle vehicle = (Vehicle) occupant1;
                        if (vehicle.isLoading() && !vehicle.isFull()) {
                            System.out.println("Delegated " + guest.getTokenID());
                            guest.walk(coord);
                            guest.setVehicle(vehicle);
                            assigned = true;
                            break;
                        }
                    }
                }
                sleepThread(1);
            }
            if(offline){
                guest.walk(Location.LOADING_DOCK.getCoordinate());
            }
        };
        Thread delegationThread = new Thread(delegating);
        delegationThread.setDaemon(true);
        delegationThread.start();
    }

    public void shutdown(){
        offline = true;
    }

    public void restart() {
        offline = false;
    }
}
