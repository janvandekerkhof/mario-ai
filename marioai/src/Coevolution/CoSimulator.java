/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.GlobalOptions;
import ch.idsia.mario.engine.MarioComponent;
import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.simulation.BasicSimulator;
import ch.idsia.mario.simulation.Simulation;
import ch.idsia.mario.simulation.SimulationOptions;
import ch.idsia.tools.EvaluationInfo;

/**
 *
 * @author Jan
 */
public class CoSimulator implements Simulation
{
    SimulationOptions simulationOptions = null;
    private MarioComponent marioComponent;
    private Level level;
    public CoSimulator(SimulationOptions simulationOptions, Level level)
    {
        GlobalOptions.VisualizationOn = simulationOptions.isVisualization();
        this.marioComponent = GlobalOptions.getMarioComponent();
        this.level = level;
        this.setSimulationOptions(simulationOptions);
    }

    private MarioComponent prepareMarioComponent()
    {
        Agent agent = simulationOptions.getAgent();
        agent.reset();
        marioComponent.setAgent(agent);
        return marioComponent;
    }

    @Override
    public void setSimulationOptions(SimulationOptions simulationOptions)
    {
        this.simulationOptions = simulationOptions;
    }

    @Override
    public EvaluationInfo simulateOneLevel()
    {
        Mario.resetStatic(simulationOptions.getMarioMode());        
        prepareMarioComponent();
        marioComponent.setZLevelScene(simulationOptions.getZLevelMap());
        marioComponent.setZLevelEnemies(simulationOptions.getZLevelEnemies());
        marioComponent.startLevel(level, simulationOptions.getTimeLimit());
        marioComponent.setPaused(simulationOptions.isPauseWorld());
        marioComponent.setZLevelEnemies(simulationOptions.getZLevelEnemies());
        marioComponent.setZLevelScene(simulationOptions.getZLevelMap());
        marioComponent.setMarioInvulnerable(simulationOptions.isMarioInvulnerable());
        return marioComponent.run1(simulationOptions.currentTrial++,
                simulationOptions.getNumberOfTrials()
        );
    }
}
