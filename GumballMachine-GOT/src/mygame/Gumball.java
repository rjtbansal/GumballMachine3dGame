
package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

/* This class is the control class for the Gumball spatials generated
 * through the gumballMachine game logic.
 */

public class Gumball extends AbstractControl {
    private GumballState state; //state of Gumball
    private GumballState DispensedState;
    private GumballState InPocketState;
    private GumballState FiredState;

    public Gumball() {
        DispensedState = new DispensedGMState(this);
        InPocketState = new GumballCollectedState(this);
        FiredState = new FiredGMState(this);
        state = DispensedState;
    }
    
    public void setColor(String color) {
        System.out.print("in gumball");
        System.out.print(spatial);
        spatial.setUserData("color", color);
    }
    
    public String getColor() {
        return (String)spatial.getUserData("color");
    }
    
    public void setValue(int value) {
        spatial.setUserData("value", value);
    }
    
    public int getValue() {
        return (Integer)spatial.getUserData("value");
    }
    
    
    /*For States*/
    public GumballState getState() {
        return state;
    }
   
    public GumballState getDispensedState() {
        return DispensedState;
    }
    
    public GumballState getFiredState() {
        return FiredState;
    }
    
    public GumballState getInPocketState() {
        return InPocketState;
    }
    
    public void setState(GumballState newState) {
        state = newState;
    }
    
    public void setFiredState() {
        state = FiredState;
    }
    
    
    /*For State Functions*/
    public String catchIt() {
        return state.catchIt();
    }

    
    @Override
    protected void controlUpdate(float tpf) {
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
       //controlRender code here
    }
    
}