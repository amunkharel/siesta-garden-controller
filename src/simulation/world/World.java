package simulation.world;

import common.Coordinate;
import simulation.occupant.base.OccupantType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class World extends ConcurrentHashMap<Coordinate, Node> {
    private int size = 100;


    public World() {
        init();
    }

    private void init(){

        for(int i=0;i<size;i++){
            for(int j=0;j<size;j++){
                Coordinate coordinate = new Coordinate(i,j);
                this.put(coordinate,new Node(coordinate));

            }
        }

    }


    public static int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max)
                .findFirst()
                .getAsInt();
    }


    public Set<Node> getMoves(Node current) {
        int x = current.getCoordinate().xCoord();
        int y = current.getCoordinate().yCoord();
        List<Node> moves = new LinkedList<>();
        Node temp = tryAdd(new Coordinate(x+1,y));
        if(temp != null){
            moves.add(temp);
        }
        temp = tryAdd(new Coordinate(x-1,y));
        if(temp != null){
            moves.add(temp);
        }
        temp = tryAdd(new Coordinate(x,y-1));
        if(temp != null){
            moves.add(temp);
        }
        temp = tryAdd(new Coordinate(x,y+1));
        if(temp != null){
            moves.add(temp);
        }
        temp = tryAdd(new Coordinate(x-1,y+1));
        if(temp != null){
            moves.add(temp);
        }
        temp = tryAdd(new Coordinate(x+1,y-1));
        if(temp != null){
            moves.add(temp);
        }
        temp = tryAdd(new Coordinate(x+1,y+1));
        if(temp != null){
            moves.add(temp);
        }
        temp = tryAdd(new Coordinate(x-1,y-1));
        if(temp != null){
            moves.add(temp);
        }
        return new HashSet<>(moves);
    }

    private Node tryAdd(Coordinate coordinate){
       Node node = this.getOrDefault(coordinate,null);
       if (node == null) return null;
       if(node.getOccupant().getOccupantType() == OccupantType.OBSTRUCTION){
           return null;
       }
       return node;
    }

}
