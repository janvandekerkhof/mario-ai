package competition.cig.sergeykarakovskiy;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;
import ch.idsia.mario.engine.sprites.Mario;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy, firstname_at_idsia_dot_ch
 * Date: Sep 1, 2009
 * Time: 3:12:07 PM
 * Package: competition.cig.sergeykarakovskiy
 */
public class SergeyKarakovskiy_JumpingAgent implements Agent
{
    private String name;
    private boolean[] action;


    public SergeyKarakovskiy_JumpingAgent()
    {
        setName("SergeyKarakovskiy_JumpingAgent");
        reset();
    }

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        action[Mario.KEY_RIGHT] = true;
        action[Mario.KEY_SPEED] = true;
    }

    public boolean[] getAction(Environment observation)
    {
//        try {Thread.sleep (39);}
//        catch (Exception e){}
        byte[][] enemies = observation.getEnemiesObservation();
        
        for (int i = 0; i < enemies.length; i++) { // VERTICAL DISTANCE FROM TOP
            for (int j = 0; j < enemies[i].length; j++) { // HORIZONTAL DISCTANCE FROM START
                if(enemies[i][j] != 0 && (i < 10 || j < 10) )
                {
                    double d = 0;
                }
            }
        }
        byte[][] jaja = observation.getLevelSceneObservation();
        byte[][] comp = observation.getCompleteObservation();
        
        action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] =  observation.mayMarioJump() || !observation.isMarioOnGround();
        return action;
    }


    public Agent.AGENT_TYPE getType()
    {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() {        return name;    }

    public void setName(String Name) { this.name = Name;    }

}
