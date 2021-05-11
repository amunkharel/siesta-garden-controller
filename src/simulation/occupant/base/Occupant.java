package simulation.occupant.base;

import common.Coordinate;
import simulation.occupant.entiy.movement.PathFinder;
import simulation.world.Node;
import simulation.world.World;

import java.util.List;

public abstract class Occupant {

    protected World world;
    protected Coordinate coordinate;

    private OccupantType occupantType;

    protected Node currentNode;
    protected Node previousNode;
    public Occupant(Node node,OccupantType occupantType) {
        this.occupantType = occupantType;
        this.currentNode = node;
    }

    public synchronized OccupantType getOccupantType() {
        return occupantType;
    }

    public void setOccupantType(OccupantType occupantType) {
        this.occupantType = occupantType;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public abstract void enterNode(Occupant occupant);

    public  void exitNode(){
        if(previousNode != null)previousNode.resetNode();
    }

    public void setCurrentNode(Node node) {
        this.previousNode = currentNode;
        this.currentNode = node;
    }
    public void setCurrentNodeTemp(Node node) {
        this.currentNode = node;
    }


    protected List<Node> findPath(Coordinate start,Coordinate end){
        return new PathFinder(world).findRoute(world.get(start),world.get(end));
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}
