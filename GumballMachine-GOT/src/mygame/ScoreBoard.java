
package mygame;

import java.util.HashMap;


public class ScoreBoard implements Observer{
    protected String observerState;
    protected userInfo subject;
    
    public ScoreBoard( userInfo theSubject){
        this.subject = theSubject;
        observerState = "Gumballs Caught: 0                                  Score : 0                            Money in Gumball Machine: 0";
    }
    
    public void update() {
        HashMap<String, Integer> map = subject.getState();
        Integer score = map.get("score");
        Integer coinAmt = map.get("coinAmt");
        Integer numOfGumballs = map.get("numOfGumballs");
        observerState = "Gumballs Caught: " + numOfGumballs + "                                   " + "Score: "         
                + score + "                                 " + "Money in Gumball Machine: "+ coinAmt;
        //System.out.println("update:" + observerState);
    }
   
}
