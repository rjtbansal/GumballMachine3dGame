package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class GMHasCoinState extends AbstractControl implements State {
    GumballMachine gMach;
    
    public GMHasCoinState(GumballMachine gMach) {
        this.gMach = gMach;
    }
    
    public void acceptCoin(int value) {
        int curr_amt = gMach.getPayment();
        curr_amt += value;
        gMach.setPayment(curr_amt);
        if (curr_amt >= gMach.getGBallPrice()){
            gMach.setState(gMach.getHasGPriceState());
        }
    }
    
    public String turnCrank() {
        int remain = gMach.getGBallPrice() - gMach.getPayment();
        System.out.println("You need to put in " + remain + " cents more");
        return "You need to put in " + remain + " cents more";
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

