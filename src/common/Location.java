package common;

import simulation.Simulation;

import java.util.LinkedList;

public enum Location {
    EASTSIDE_PARKING,
    TOKEN_KIOSK,
    LOADING_DOCK,
    VIEWING_AREA,
    WESTSIDE_PARKING,
    WESTSIDE_LOADING_ZONE,
    EASTSIDE_LOADING_ZONE;

    private static final Coordinate EASTSIDE_PARKING_COORD = new Coordinate(38,15);
    private static final Coordinate TOKEN_KIOSK_COORD = new Coordinate(73,37);
    private static final Coordinate LOADING_DOCK_COORD = new Coordinate(84,38);
    private static final Coordinate VIEWING_AREA_COORD = new Coordinate(20,50);
    private static final Coordinate WESTSIDE_PARKING_COORD = new Coordinate(24,35);
    private static final Coordinate WESTSIDE_LOADING_ZONE_COORD = new Coordinate(19,62);
    private static final Coordinate EASTSIDE_LOADING_ZONE_COORD = new Coordinate(60,40);

    private static LinkedList<Coordinate> PATH_TO_WEST = Simulation.getInstance().getPathToWest();
    private static LinkedList<Coordinate> PATH_TO_EAST = Simulation.getInstance().getPathToEast();

    public Coordinate getCoordinate(){
        switch (this){
            case WESTSIDE_PARKING: return WESTSIDE_PARKING_COORD;
            case EASTSIDE_PARKING: return EASTSIDE_PARKING_COORD;
            case EASTSIDE_LOADING_ZONE: return EASTSIDE_LOADING_ZONE_COORD;
            case WESTSIDE_LOADING_ZONE: return WESTSIDE_LOADING_ZONE_COORD;
            case TOKEN_KIOSK: return TOKEN_KIOSK_COORD;
            case VIEWING_AREA: return VIEWING_AREA_COORD;
            case LOADING_DOCK: return LOADING_DOCK_COORD;
            default: return null;
        }
    }

    public static LinkedList<Coordinate> getPathWayPoints(Location destination){
        switch (destination){
            case EASTSIDE_LOADING_ZONE: return PATH_TO_EAST;
            case WESTSIDE_LOADING_ZONE: return PATH_TO_WEST;
            default: return null;
        }
    }

    public static Location getLocation(Coordinate coordinate){
        if(coordinate.equals(EASTSIDE_LOADING_ZONE_COORD)) return EASTSIDE_LOADING_ZONE;
        if(coordinate.equals(WESTSIDE_LOADING_ZONE_COORD)) return WESTSIDE_LOADING_ZONE;
        if(coordinate.equals(EASTSIDE_PARKING_COORD)) return EASTSIDE_PARKING;
        if(coordinate.equals(WESTSIDE_PARKING_COORD)) return WESTSIDE_PARKING;
        if(coordinate.equals(VIEWING_AREA_COORD)) return VIEWING_AREA;
        return null;
    }



}
