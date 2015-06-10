/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import ch.idsia.ai.agents.Agent;

/**
 *
 * @author Jan
 */
public class AgentObject extends Coevolvable{
    
    private Agent agent;
    
    public AgentObject(Agent agent){
        super();
        this.agent = agent;
    }
    
    public Agent getAgent(){
        return agent;
    }
}
