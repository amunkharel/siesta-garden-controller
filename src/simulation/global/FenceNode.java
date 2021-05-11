package simulation.global;

import common.device.proxy.FenceNodeProxy;

public class FenceNode implements FenceNodeProxy {
    private int currentVoltage = Integer.MAX_VALUE;
    @Override
    public int checkVoltage() {
        return currentVoltage;
    }



    public void setCurrentVoltage(int currentVoltage) {
        this.currentVoltage = currentVoltage;
    }
}
