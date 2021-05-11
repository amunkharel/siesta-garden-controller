package common.device.proxy;

import common.Coordinate;
import common.device.state.VehicleCondition;

import java.util.concurrent.ConcurrentHashMap;

public interface VehicleProxy {
    void drive(Coordinate coordinate,Coordinate previousLocation,boolean isEmergency);
    Coordinate getCurrentLocation();
    VehicleCondition getCurrentVehicleCondition();
    void lockDoors();
    void unlockDoors();
    ConcurrentHashMap<Integer,Integer> readRFIDScanner();
}
