package simulation.occupant.entiy.movement;

import simulation.occupant.base.Occupant;
import simulation.world.Node;
import simulation.world.World;

import java.util.*;

public class PathFinder {
    private final World world;
    private Scorer scorer = new DistanceScorer();


    public PathFinder(World world) {
        this.world = world;
    }

    public List<Node> findRoute(Node from, Node to) {
        Queue<RouteNode> openSet = new PriorityQueue<>();
        Map<Node, RouteNode> allNodes = new HashMap<>();
        RouteNode start = new RouteNode(from, null, 0d, scorer.computeCost(from, to));
        openSet.add(start);
        allNodes.put(from, start);
        while (!openSet.isEmpty()) {
            RouteNode next = openSet.poll();
            if (next.getCurrent().equals(to)) {
                List<Node> route = new ArrayList<>();
                RouteNode current = next;
                do {
                    route.add(0, current.getCurrent());
                    current = allNodes.get(current.getPrevious());
                } while (current != null);
                return route;
            }
            world.getMoves(next.getCurrent()).forEach(connection -> {
                RouteNode nextNode = allNodes.getOrDefault(connection, new RouteNode(connection));
                allNodes.put(connection, nextNode);

                double newScore = next.getRouteScore() + scorer.computeCost(next.getCurrent(), connection);
                if (newScore < nextNode.getRouteScore()) {
                    nextNode.setPrevious(next.getCurrent());
                    nextNode.setRouteScore(newScore);
                    nextNode.setEstimatedScore(newScore + scorer.computeCost(connection, to));
                    openSet.add(nextNode);
                }
            });
        }
        throw new IllegalStateException("No route found");
    }

}
