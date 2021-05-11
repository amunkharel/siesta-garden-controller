package simulation.occupant.base;

import simulation.occupant.entiy.Guest;
import simulation.world.Node;
import simulation.occupant.entiy.Vehicle;

import java.util.Random;

public class None extends Occupant {

    public None(Node node) {
        super(node, OccupantType.NONE);
    }

    @Override
    public void enterNode(Occupant occupant) {
        switch (occupant.getOccupantType()){
            case VEHICLE:
            case KIOSK:
            case OBSTRUCTION:
            case VEHICLE_LOADING_ZONE_DELEGATOR:
            case EASTSIDE_GUEST_LOADING_DELEGATOR:
                currentNode.setOccupant(occupant);
                break;
            default:
                //do nothing
        }
    }


}
