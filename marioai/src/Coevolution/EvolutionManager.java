/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.LevelGenerator;
import ch.idsia.tools.CmdLineOptions;
import ch.idsia.tools.EvaluationOptions;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jan
 */
public class EvolutionManager {
    private static SimpleNNAgent initial = new SimpleNNAgent();
    private  int AGENT_POP_SIZE;
    private  int TEST_POP_SIZE;
    private  int genCount = 0;
    private static Population agents;
    private static Population tests;
    private static final int INIT_AMOUNT = 20;
    private static final int ENCOUNTERS = 20;
    private static final double MIN_TEST_FITNESS = 1;
    private static int SEGMENT_LENGTH = 200;
    private static int DIFFICULTY;
    private static final int MORE_CHANCE = 2;
    private int TIME_LIMIT = 40;
    private static int testCount = 0;
    private static int maxTestCount;
    private EvaluationOptions options;
    private double lastFitnessAgent;
    private double lastFitnessLevel;
    private static final int TEST_AMOUNT = 1000;
    private static final int TEST_LEVEL_LENGTH = 300;
    
    public  int getAgentPopsize(){
        return AGENT_POP_SIZE;
    }
    
    public EvolutionManager(){
        
    }
    
    public double getAvgAgentFitness(){
        return agents.getAvgFitness();
    }
    public double getAvgTestFitness(){
        return tests.getAvgFitness();
    }
    public double getWorstAgentFitness(){
        return agents.getLowestFitness();
    }
    public double getBestAgentFitness(){
        return agents.getBestFitness();
    }
    public double getWorstTestFitness(){        
        return tests.getLowestFitness();
    }
    public double getBestTestFitness(){
        return tests.getBestFitness();
    }
    
    public EvolutionManager(int agentSize, int testSize, int difficulty, int segmentLength, boolean incremental)
    {
        AGENT_POP_SIZE = agentSize;
        TEST_POP_SIZE = testSize;
        TIME_LIMIT = segmentLength / 3 ; 
        DIFFICULTY = difficulty;
        SEGMENT_LENGTH = segmentLength;
        init(incremental);
    }
    
    private void init(boolean incremental){
        options = new CmdLineOptions(new String[]{});
        options.setNumberOfTrials(1);
        options.setPauseWorld(false);
        options.setAgent(initial);
        options.setMaxFPS(true);
        options.setLevelDifficulty(DIFFICULTY);
        options.setVisualization(false);
        options.setTimeLimit(TIME_LIMIT);
        ArrayList<Coevolvable> agentPop = new ArrayList<>();
        for (int i = 0; i < AGENT_POP_SIZE; i++) {
            agentPop.add(new AgentObject((SimpleNNAgent)initial.getNewInstance()));
        }
        ArrayList<Coevolvable> levelPop = new ArrayList<>();
        ArrayList<Level> levels = LevelGenerator.createLevelSegments(TEST_POP_SIZE,24, DIFFICULTY, 0, SEGMENT_LENGTH);
        for (int i = 0; i < levels.size(); i++) {
            levelPop.add(new LevelObject(levels.get(i), DIFFICULTY));
        }
        
        agents = new Population(agentPop, MORE_CHANCE);
        tests = new Population(levelPop, MORE_CHANCE);
        
        initPops(options, incremental);
        
    }
    
    public void runIncremental(int generations)
    {
        for (int i = 0; i < generations; i++) {
            if(i % 20 ==0){
                System.out.println("Generation " + i + " of " + generations);
                System.out.println("Best fitness : " + agents.getBestFitness() );
                System.out.println("Avg fitness : " + agents.getAvgFitness());
            }
                
            for (int j = 0; j < ENCOUNTERS; j++) {

                options = new CmdLineOptions(new String[]{});
                options.setNumberOfTrials(1);
                options.setPauseWorld(false);
                options.setAgent(initial);
                options.setMaxFPS(true);
                options.setLevelDifficulty(DIFFICULTY);
                options.setVisualization(false);
                options.setTimeLimit(TIME_LIMIT);
                CoevolutionTask task = new CoevolutionTask(options);
                task.setAgentHeuristic(1);
                task.setTestHeuristic(1);
                AgentObject agent = (AgentObject) agents.select();
                LevelObject level = (LevelObject) tests.select();
                
                double[] results = task.evaluate(agent.getAgent(),level.getLevel(),  SEGMENT_LENGTH);
                agent.push(results[0]);
                level.push(results[1]);
                agents.update(agent);
                tests.update(level);
                testCount++;
//                if(testCount % 100 == 0){
//                    System.out.println((double)testCount / maxTestCount);
//                }
            }
            genCount++;
            // Evolutionairy part
            
            
            AgentObject firstAgent = (AgentObject)agents.select();
            AgentObject secondAgent = (AgentObject)agents.select();
            SimpleNNAgent f1 = (SimpleNNAgent) firstAgent.getAgent();
            SimpleNNAgent f2 = (SimpleNNAgent) secondAgent.getAgent();
            NeuralNetwork newNet = f1.getNetwork().nPointCrossover(f2.getNetwork(), 12);
            if(f1 != null && f2 != null){
                SimpleNNAgent child = new SimpleNNAgent(newNet);
                AgentObject childObj = new AgentObject(child);
                testFitnessAndInsert(childObj, options);
            }
            replaceInvalidTests();

        }
    }
    
    public void replaceInvalidTests()
    {
        ArrayList<Coevolvable> lowTests = tests.getBelowFitness(MIN_TEST_FITNESS);
        for (int i = 0; i < lowTests.size(); i++) {
            LevelObject lObj = (LevelObject)lowTests.get(i);
            int newDiff = lObj.getDifficulty() + 1;
            Level newLevel = LevelGenerator.createRandomSegment(SEGMENT_LENGTH, newDiff);
            LevelObject newObj = new LevelObject(newLevel, newDiff);
            testFitnessAndInsert(newObj, options);
        }
    }
    
    public void runLevelAndAgent(int agent, int level){
        if(agent > agents.getMembers().size() -1 || level > tests.getMembers().size() -1 ) return;
        options = new CmdLineOptions(new String[]{});
        options.setNumberOfTrials(1);
        options.setPauseWorld(false);
        options.setAgent(initial);
        options.setMaxFPS(false);
        options.setLevelDifficulty(DIFFICULTY);
        options.setVisualization(true);
        options.setTimeLimit(TIME_LIMIT);
        AgentObject age = (AgentObject) agents.getMembers().get(agent);
        LevelObject lev = (LevelObject) tests.getMembers().get(level);
        CoevolutionTask task = new CoevolutionTask(options);
        double[] outputs = task.evaluate(age.getAgent(), lev.getLevel(),SEGMENT_LENGTH);
    }
    
    public String runTesting(int agent)
    {
        String ret = "";
        int[] difficulties = new int[]{0,3,5,10};
        int highestDifficulty = 0;
        double maxDistance = 0;

        for(int i = 0; i < difficulties.length; i++){
            double testScore = 0;
            Boolean made = false;
            ArrayList<Double> testList = new ArrayList<>();
            maxDistance = 0;
            int count = 0;
            int seed = 0;
            for (int j = 0; j < TEST_AMOUNT; j++) {
                Level level = LevelGenerator.createLevel(TEST_LEVEL_LENGTH, 15, seed, difficulties[i], 0);
                seed++;
                count++;
                if(count % 100 == 0)
                {
                    System.out.println("Done testing " + count + "levels of difficulty + " + difficulties[i]);
                }
                LevelObject obj = new LevelObject(level);
                Level lev = obj.getLevel(); // need for copy method... bad i know
                options.setLevelDifficulty(difficulties[i]);
                options.setTimeLimit(200);
                options.setMaxFPS(true);
                options.setVisualization(false);
                AgentObject age = (AgentObject) agents.getMembers().get(agent);
                CoevolutionTask task = new CoevolutionTask(options);
                double[] outputs = task.evaluate(age.getAgent(), lev, SEGMENT_LENGTH);
                if(outputs[0] > 4000 && !made){
                    made = true;
                    highestDifficulty = difficulties[i];
                }
                else
                {
                    if(i == 0 || highestDifficulty == difficulties[i -1]){
                    double result = outputs[0] / 4000;
                    if(result > maxDistance)
                        maxDistance = result;
                    }
                }
                testScore += outputs[0];
                testList.add(outputs[0]);
            }
            ret += "Difficulty " + difficulties[i] + " : " + testScore / TEST_AMOUNT ;
            ret += "Values : {";
            for(double d : testList){
                ret += d + " , " ;
            }
            ret += "}";
        }
        ret += "Highest difficulty : " + (highestDifficulty == difficulties[difficulties.length - 1] ? highestDifficulty : (highestDifficulty + maxDistance));

        return ret;
    }
    
    public double runMultiple(int agent, ArrayList<Level> levels, int difficulty)
    {
        double result = 0; 
        for(Level level : levels)
        {
            LevelObject obj = new LevelObject(level);
            Level lev = obj.getLevel(); // need for copy method... bad i know
            options.setLevelDifficulty(difficulty);
            options.setTimeLimit(200);
            options.setMaxFPS(true);
            options.setVisualization(false);
            AgentObject age = (AgentObject) agents.getMembers().get(agent);
            CoevolutionTask task = new CoevolutionTask(options);
            double[] outputs = task.evaluate(age.getAgent(), lev,  SEGMENT_LENGTH);
            result += outputs[0];
        }
        result /= (double) levels.size();
        return result;
    }
    
    
    public double runOne(int agent, Level level, int difficulty)
    {
        LevelObject obj = new LevelObject(level);
        Level lev = obj.getLevel(); // need for copy method... bad i know
        options.setLevelDifficulty(difficulty);
        options.setTimeLimit(200);
        options.setMaxFPS(true);
        options.setVisualization(false);
        AgentObject age = (AgentObject) agents.getMembers().get(agent);
        CoevolutionTask task = new CoevolutionTask(options);
        double[] outputs = task.evaluate(age.getAgent(), lev,  SEGMENT_LENGTH);
        return outputs[0];
    }
    
    public void runOne(int length, int difficulty, int timeLimit, int agent){
        if(agent > agents.getMembers().size() -1 ) return ;

        Level level = LevelGenerator.createLevel(length, 15, (int) (2000 * Math.random()), difficulty, 0);
        options.setLevelDifficulty(difficulty);
        options.setTimeLimit(length /3);
        options.setMaxFPS(false);
        options.setVisualization(true);
        AgentObject age = (AgentObject) agents.getMembers().get(agent);
        CoevolutionTask task = new CoevolutionTask(options);
        double[] outputs = task.evaluate(age.getAgent(), level, length);
    }
    
    public void run(int generations)
    {
        for (int i = 0; i < generations; i++) {
            if(i % 20 ==0){
                System.out.println("Generation " + i + " of " + generations);
                System.out.println("Best fitness : " + agents.getBestFitness() );
                System.out.println("Avg fitness : " + agents.getAvgFitness());
            }
                
            for (int j = 0; j < ENCOUNTERS; j++) {

                options = new CmdLineOptions(new String[]{});
                options.setNumberOfTrials(1);
                options.setPauseWorld(false);
                options.setAgent(initial);
                options.setMaxFPS(true);
                options.setLevelDifficulty(DIFFICULTY);
                options.setVisualization(false);
                options.setTimeLimit(TIME_LIMIT);
                CoevolutionTask task = new CoevolutionTask(options);
                task.setAgentHeuristic(1);
                AgentObject agent = (AgentObject) agents.select();
                LevelObject level = (LevelObject) tests.select();
                
                double[] results = task.evaluate(agent.getAgent(),level.getLevel(),  SEGMENT_LENGTH);
                agent.push(results[0]);
                level.push(results[1]);
                agents.update(agent);
                tests.update(level);
                testCount++;
//                if(testCount % 100 == 0){
//                    System.out.println((double)testCount / maxTestCount);
//                }
            }
            AgentObject firstAgent = (AgentObject)agents.select();
            AgentObject secondAgent = (AgentObject)agents.select();
            SimpleNNAgent f1 = (SimpleNNAgent) firstAgent.getAgent();
            SimpleNNAgent f2 = (SimpleNNAgent) secondAgent.getAgent();
            NeuralNetwork newNet = f1.getNetwork().nPointCrossover(f2.getNetwork(), 12);
            if(f1 != null && f2 != null){
                SimpleNNAgent child = new SimpleNNAgent(newNet);
                AgentObject childObj = new AgentObject(child);
                testFitnessAndInsert(childObj, options);
            }
        }
    }
    
    private void testFitnessAndInsert(LevelObject lObj, EvaluationOptions options)
    {
        CoevolutionTask task = new CoevolutionTask(options);
        task.setTestHeuristic(1);
        for (Coevolvable agent : agents.selectMany(INIT_AMOUNT)) {
                AgentObject aObj = (AgentObject) agent;
                double[] results = task.evaluate(aObj.getAgent(), lObj.getLevel(),SEGMENT_LENGTH);
                lObj.push(results[1]);
        }
        if(lObj.getAvgFitness() > tests.getLowestFitness()){
            tests.push(lObj);
            System.out.println("Level of difficulty  " + (lObj.getDifficulty() - 1) +" replaced at generation " + genCount);
        }
    }
    
    private  void testFitnessAndInsert(AgentObject aObj, EvaluationOptions options){
        CoevolutionTask task = new CoevolutionTask(options);
        task.setAgentHeuristic(1);
        for (Coevolvable level : tests.selectMany(INIT_AMOUNT)) {
                LevelObject lObj = (LevelObject) level;
                double[] results = task.evaluate(aObj.getAgent(), lObj.getLevel(), SEGMENT_LENGTH);
                aObj.push(results[0]);
        }
        if(aObj.getAvgFitness() > agents.getLowestFitness()){
            agents.push(aObj);
        }
    }
    
    
    private  void initPops(EvaluationOptions options, boolean incremental){
        CoevolutionTask task = new CoevolutionTask(options);
        task.setTestHeuristic(incremental ? 1: 0);
        task.setAgentHeuristic(1);
        for (Coevolvable agent : agents.getMembers()) {
            AgentObject aObj = (AgentObject) agent;
            for (Coevolvable level : tests.getRandomSet(INIT_AMOUNT)) {
                LevelObject lObj = (LevelObject) level;
                double[] results = task.evaluate(aObj.getAgent(), lObj.getLevel(), SEGMENT_LENGTH);
                aObj.push(results[0]);
//                testCount++;
//                if(testCount % 100 == 0){
//                    System.out.println((double)testCount / maxTestCount);
//                }
            }
        }
        for (Coevolvable level : tests.getMembers()) {
            LevelObject lObj = (LevelObject) level;
            for (Coevolvable agent : agents.getRandomSet(INIT_AMOUNT)) {
                AgentObject aObj = (AgentObject) agent;
                double[] results = task.evaluate(aObj.getAgent(), lObj.getLevel(), SEGMENT_LENGTH);
                lObj.push(results[1]);
//                testCount++;
//                if(testCount % 100 == 0){
//                    System.out.println((double)testCount / maxTestCount);
//                }
            }
        }
        
        agents.sort();
        tests.sort();
    }
    
}
