package simulation.occupant.entiy;

import common.Coordinate;
import common.device.proxy.TokenKioskProxy;
import common.device.state.TokenKioskState;
import simulation.Simulation;
import simulation.occupant.base.Occupant;
import simulation.occupant.base.OccupantType;
import simulation.occupant.delagator.GuestLoadingDelegator;


public class Kiosk extends Occupant implements TokenKioskProxy {

    private Coordinate target;
    public Kiosk() {
        super(null, OccupantType.KIOSK);
    }

    @Override
    public synchronized void enterNode(Occupant occupant) {
        switch (occupant.getOccupantType()){
            case GUEST:
                Guest guest = (Guest)occupant;
                if(!guest.hasRFIDToken()){
                    putReceiptID(guest.getReceiptID());
                    guest.setTokenId(getTokenID());
                    guest.walk(target);
                }
                break;
            default:
                //do nothing
        }
    }

    public void setTarget(Coordinate target) {
        this.target = target;
    }

    @Override
    public TokenKioskState getState() {
        return TokenKioskState.OKAY;
    }

    @Override
    public void shutDown() {
        ((GuestLoadingDelegator)world.get(Simulation.getInstance().getGuestDelegatorLocation()).getOccupant()).shutdown();
    }

    @Override
    public void restart() {
        ((GuestLoadingDelegator)world.get(Simulation.getInstance().getGuestDelegatorLocation()).getOccupant()).restart();
    }
}
