package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class HasGMPriceState extends AbstractControl implements State {
    GumballMachine gMach;
    
    public HasGMPriceState(GumballMachine gMach) {
        this.gMach = gMach;
    }
    
    public void acceptCoin(int value) {
        int curr_amt = gMach.getPayment();
        curr_amt += value;
        gMach.setPayment(curr_amt);
    }
    
    public String turnCrank() {
        System.out.println("Crank turned!");
        dispense();
        return "Crank turned!";
    }
    
    private void dispense() {
        int gBalls = gMach.getCount();
        --gBalls;
        gMach.setCount(gBalls);
        System.out.println("Gumball dispensed!");
        System.out.println(gMach.getCount() + " gumballs left in machine.");
        gMach.setDispense();
        if (gMach.getCount() > 0) {
            gMach.setState(gMach.getNoCoinState());
        }
        else {
            gMach.setState(gMach.getSoldOutState());
        }
        
    }
    
    @Override
    protected void controlUpdate(float tpf) {
        if (spatial != null) {
            //System.out.println("Do something here");
           
        }
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
       //controlRender code here
    }
}

