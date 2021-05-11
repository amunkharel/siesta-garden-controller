package SGC;

import common.device.proxy.FenceNodeProxy;
import common.device.state.FenceState;
import communication.base.Message;
import communication.message.FenceStatus;
import simulation.Simulation;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ElectricFenceController {
    private final BlockingQueue<Message> centralControlMessageQueue;
    private final ScheduledExecutorService heartbeatExecutor;
    private final LinkedList<FenceNodeProxy> fenceNodeProxies = Simulation.getInstance().getFenceNodeProxies();
    private final int criticalVoltageThreshold = 10000;
    public ElectricFenceController(BlockingQueue<Message> messageQueue) {
        this.centralControlMessageQueue = messageQueue;
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        this.start();
    }

    private void start(){
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeatStatus,0,1, TimeUnit.SECONDS);
    }

    private void sendHeartbeatStatus() {
        if(this.checkFenceStatus()) {
            FenceStatus message = new FenceStatus(FenceState.CRITICAL_FAULT);
            centralControlMessageQueue.add(message);
        }
        else {
            FenceStatus message = new FenceStatus(FenceState.ONLINE);
            centralControlMessageQueue.add(message);
        }
    }

    private boolean checkFenceStatus() {
        for(FenceNodeProxy node:fenceNodeProxies){
            if(node.checkVoltage() < criticalVoltageThreshold)
                return true;
        }
        return false;
    }
}
