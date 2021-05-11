package simulation.occupant.entiy.movement;

import simulation.world.Node;

public interface Scorer {
    double computeCost(Node from, Node to);
}
