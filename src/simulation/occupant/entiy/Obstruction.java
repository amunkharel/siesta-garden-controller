package simulation.occupant.entiy;

import simulation.occupant.base.Occupant;
import simulation.occupant.base.OccupantType;
import simulation.world.Node;

public class Obstruction extends Occupant {

    public Obstruction() {
        super(null, OccupantType.OBSTRUCTION);
    }

    @Override
    public void enterNode(Occupant occupant) {

    }
}
