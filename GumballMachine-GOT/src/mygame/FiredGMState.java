
package mygame;

import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.AbstractControl;

public class FiredGMState extends AbstractControl implements GumballState {
    Gumball Gumball;
    
    public FiredGMState(Gumball Gumball) {
        this.Gumball = Gumball;
    }
    
    public String catchIt() {
        return "Fired gumball cannot be caught. Disappearing...";
    }
    
    
    
    @Override
    protected void controlUpdate(float tpf) {
        
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
       //controlRender code here
    }
}

