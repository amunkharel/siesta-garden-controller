package simulation.occupant.entiy.movement;

import simulation.world.Node;

public class DistanceScorer implements Scorer {
    @Override
    public double computeCost(Node from, Node to) {
        double x1 = from.getCoordinate().xCoord();
        double x2 = to.getCoordinate().xCoord();
        double y1 = from.getCoordinate().yCoord();
        double y2 = to.getCoordinate().yCoord();
        double dx = x1 - x2;
        double dy = y1 - y2;
        return (dx + dy) - Math.min(dx, dy);
        //return Math.sqrt((dx * dx) + (dy * dx));
    }
}
