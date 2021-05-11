package simulation.occupant.entiy.movement;

import simulation.world.Node;

public class RouteNode implements Comparable<RouteNode> {
    private final Node current;
    private Node previous;
    private double routeScore;
    private double estimatedScore;

    public RouteNode(Node current) {
        this.current = current;
        this.previous = null;
        this.routeScore = Double.POSITIVE_INFINITY;
        this.estimatedScore = Double.POSITIVE_INFINITY;
    }

    public RouteNode(Node current, Node previous, double routeScore, double estimatedScore) {
        this.current = current;
        this.previous = previous;
        this.routeScore = routeScore;
        this.estimatedScore = estimatedScore;
    }

    @Override
    public int compareTo(RouteNode other) {
        if (this.estimatedScore > other.estimatedScore) {
            return 1;
        } else if (this.estimatedScore < other.estimatedScore) {
            return -1;
        } else {
            return 0;
        }
    }

    public Node getCurrent() {
        return current;
    }

    public Node getPrevious() {
        return previous;
    }

    public double getRouteScore() {
        return routeScore;
    }

    public void setPrevious(Node current) {
        previous = current;
    }

    public void setRouteScore(double newScore) {
        routeScore = newScore;
    }

    public void setEstimatedScore(double estimatedScore) {
        this.estimatedScore = estimatedScore;
    }
}
