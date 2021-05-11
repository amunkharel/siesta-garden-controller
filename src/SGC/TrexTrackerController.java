package SGC;

import common.Coordinate;
import common.device.state.TrexCondition;
import common.device.state.TrexState;
import communication.base.Message;
import communication.message.TrexStatus;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrexTrackerController{

    private BlockingQueue<Message> centralControlMessageQueue;
    private ScheduledExecutorService heartbeatExecutor;

    public TrexTrackerController(BlockingQueue<Message> messageQueue) {
        this.centralControlMessageQueue = messageQueue;
        this.heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        this.start();
    }

    private void start(){
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeatStatus,0,1, TimeUnit.SECONDS);
    }

    private void sendHeartbeatStatus() {
        TrexStatus message = new TrexStatus(TrexCondition.HEALTHY, checkBounds(), new Coordinate(15, 50));
        centralControlMessageQueue.add(message);
    }

    private TrexState checkBounds(){
        return TrexState.IN_BOUND;
    }

}
