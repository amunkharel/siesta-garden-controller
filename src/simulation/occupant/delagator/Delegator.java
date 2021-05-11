package simulation.occupant.delagator;

import common.Coordinate;
import simulation.occupant.base.Occupant;
import simulation.occupant.base.OccupantType;
import simulation.world.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public abstract class Delegator extends Occupant {
    protected Runnable delegating;

    protected LinkedList<Coordinate> targetZones;

    public Delegator(OccupantType occupantType) {
        super(null, occupantType);
    }

    protected  abstract void delegate(Occupant occupant);


    protected void sleepThread(int duration){
        try {
            TimeUnit.SECONDS.sleep(duration);
        } catch (InterruptedException e) {e.printStackTrace();}
    }

    protected void startDelegation(){
        Thread delegationThread = new Thread(delegating);
        delegationThread.setDaemon(true);
        delegationThread.start();
    }

    public void setTargetZones(LinkedList<Coordinate> targetZones) {
        this.targetZones = targetZones;
    }
}
