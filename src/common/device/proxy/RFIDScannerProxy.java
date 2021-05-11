package common.device.proxy;

import common.Coordinate;


import java.util.concurrent.ConcurrentHashMap;

public interface RFIDScannerProxy {
   ConcurrentHashMap<Integer, Coordinate> scanRFIDTokens();
}
