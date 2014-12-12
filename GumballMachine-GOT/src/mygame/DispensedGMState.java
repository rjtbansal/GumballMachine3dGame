
package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class DispensedGMState extends AbstractControl implements GumballState {
    Gumball Gumball;
    
    public DispensedGMState(Gumball Gumball) {
        this.Gumball = Gumball;
    }
    
    public String catchIt() {
        Gumball.setState(Gumball.getInPocketState());
        return "Gumball Caught";
    
    }
    
    
    
    @Override
    protected void controlUpdate(float tpf) {
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
       //controlRender code here
    }
}

