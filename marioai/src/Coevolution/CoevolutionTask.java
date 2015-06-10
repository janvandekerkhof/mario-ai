/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.tasks.Task;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.tools.EvaluationOptions;
import ch.idsia.tools.Evaluator;
import java.util.List;

/**
 *
 * @author Jan
 */
public class CoevolutionTask {
    private EvaluationOptions options;
    
    private static final double DISTANCE_COEFFICIENT = 1333;
    
    private int testHeur = 0;
    private int agentHeur = 0;

    public CoevolutionTask(EvaluationOptions evaluationOptions) {
        setOptions(evaluationOptions);
    }
    
    
    // MARIOMODE = 0 => small, 1 => large, 2 => fire
    // MARIOSTATUS = 1 => WIN 0 => DEAD
    // DT = distance travelled heuristic
    public double[] evaluate(Agent controller, Level level,  int segmentLength) {
        double distanceTravelled = 0;
        double totalDistance = segmentLength * 16; // square factor
        double marioStatus = -1;
        int marioMode = -1;
        double marioY = 0;
        double timeLeft = 0;
//        controller.reset();
        options.setAgent(controller);
        Evaluator evaluator = new Evaluator(options);
        List<EvaluationInfo> results = evaluator.evaluate(level);
        for (EvaluationInfo result : results) {
            //if (result.marioStatus == Mario.STATUS_WIN )
            //    Easy.save(options.getAgent(), options.getAgent().getName() + ".xml");
            timeLeft = result.timeLeft;
            distanceTravelled += result.computeDistancePassed();
            marioStatus = result.marioStatus;
            marioMode = result.marioMode;
            marioY  = result.marioEndingYPosition;
        }
        
        double distance = distanceTravelled / DISTANCE_COEFFICIENT;
        boolean suicidePenal = marioY > 224 ; // 14 * 16
        double heuristic = suicidePenal ? 0 : distance + marioStatus * marioMode;
        
        double mapHeur = calcTestHeurstic(heuristic, totalDistance, marioStatus, marioMode);
        return agentHeur == 0? new double[]{distanceTravelled, mapHeur} : new double[]{heuristic, mapHeur};     
    }
    
    // 0 = distancetravelled
    // 1 = other heurstic
    public void setAgentHeuristic(int heuristic)
    {
        agentHeur = heuristic;
    }
    // 0 = inverse heurstic
    // 1 = mario status
    public void setTestHeuristic(int heuristic)
    {
        testHeur = heuristic;
    }
    
    private double calcTestHeurstic(double heuristic, double totalDistance, double marioStatus, int marioMode)
    {
        switch(testHeur)
        {
            case 0 :
                return totalDistance / DISTANCE_COEFFICIENT + 2 - heuristic;
            case 1 : 
                return 3 - (marioStatus * (marioMode + 1));
        }
        return 0;
    }
    
    public void setOptions(EvaluationOptions options) {
        this.options = options;
    }

    public EvaluationOptions getOptions() {
        return options;
    }

    
}
