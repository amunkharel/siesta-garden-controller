package simulation.global;

import common.Coordinate;
import common.device.proxy.RFIDScannerProxy;


import java.util.concurrent.ConcurrentHashMap;

public class RFIDScanner implements RFIDScannerProxy {
    private ConcurrentHashMap<Integer,Coordinate> guests = new ConcurrentHashMap<>();

    @Override
    public ConcurrentHashMap<Integer, Coordinate> scanRFIDTokens() {
        return guests;
    }
    public void putGuestData(int tokenID,Coordinate coordinate){
        guests.put(tokenID,coordinate);
    }



}
