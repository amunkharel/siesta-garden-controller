package simulation.occupant.delagator;

import common.Coordinate;
import common.Location;
import simulation.occupant.base.Occupant;
import simulation.occupant.base.OccupantType;
import simulation.occupant.entiy.Vehicle;

public class VehicleLoadingZoneDelegator extends Delegator {
    private Coordinate offloadZone;

    public VehicleLoadingZoneDelegator() {
        super(OccupantType.VEHICLE_LOADING_ZONE_DELEGATOR);
    }

    @Override
    public void enterNode(Occupant occupant) {
        switch (occupant.getOccupantType()){
            case VEHICLE:
                delegate(occupant);
                break;
            default:
                //do nothing
        }
    }

    @Override
    protected void delegate(Occupant occupant) {
        Vehicle vehicle = (Vehicle)occupant;
        if(vehicle.getPassengers().size() == 0) {
            sleepThread(2);
            for (Coordinate coord : targetZones) {
                if (world.get(coord).getOccupant().getOccupantType() == OccupantType.NONE) {
                    world.get(coord).enterNode(vehicle);
                    //vehicle.setLoading(false);
                    break;
                }
            }
        }else {
            sleepThread(2);
            if (vehicle.getCurrentLocation().equals(Location.EASTSIDE_LOADING_ZONE.getCoordinate())) {
                vehicle.getPassengerMap().values().forEach((guest -> {
                    guest.setVehicle(null);
                    guest.walk(Location.LOADING_DOCK.getCoordinate());
                }));
                vehicle.resetReserved();
            } else {

            }
            vehicle.removeGuests();
            for (Coordinate coord : targetZones) {
                if (world.get(coord).getOccupant().getOccupantType() == OccupantType.NONE) {
                    world.get(coord).enterNode(vehicle);
                    //vehicle.setLoading(false);
                    break;
                }
            }
            delegating = () -> {

            };
        }
        //startDelegation();
    }


    public void setOffloadZone(Coordinate offloadZone) {
        this.offloadZone = offloadZone;
    }
}
