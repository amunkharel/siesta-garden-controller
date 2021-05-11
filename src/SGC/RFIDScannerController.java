package SGC;

import common.device.proxy.RFIDScannerProxy;
import communication.message.GuestLocation;
import communication.base.Message;
import simulation.Simulation;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RFIDScannerController{
    private int scanInterval = 1;
    private BlockingQueue<Message> centralControlMessageQueue;
    private LinkedList<RFIDScannerProxy> rfidScannerProxies = new LinkedList<>();
    private ScheduledExecutorService scanExecutor = Executors.newSingleThreadScheduledExecutor();

    public RFIDScannerController(BlockingQueue<Message> messageQueue) {
        this.centralControlMessageQueue = messageQueue;
        scanExecutor.scheduleAtFixedRate(this::scanRFID,0,scanInterval, TimeUnit.SECONDS);
        rfidScannerProxies.add(Simulation.getInstance().getRfidScannerProxy());
        scanExecutor.scheduleAtFixedRate(this::scanRFID,0,100,TimeUnit.MILLISECONDS);
    }

    private void scanRFID(){
        rfidScannerProxies.forEach((scanner)->{
            scanner.scanRFIDTokens().forEach((key,value)->{
                try {
                    centralControlMessageQueue.put(new GuestLocation(key,value));
                } catch (InterruptedException e) {}
            });
        });
    }
}
