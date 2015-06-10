///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package Coevolution;
//
//import ch.idsia.ai.agents.Agent;
//import ch.idsia.mario.engine.level.Level;
//import ch.idsia.mario.engine.level.LevelGenerator;
//import ch.idsia.tools.CmdLineOptions;
//import ch.idsia.tools.EvaluationOptions;
//import java.util.ArrayList;
//import java.util.concurrent.Semaphore;
//
///**
// *
// * @author Jan
// */
//public class Coevolve {
//    
//    private static SimpleNNAgent initial = new SimpleNNAgent();
//    private static final int AGENT_POP_SIZE = 150;
//    private static final int TEST_POP_SIZE = 300;
//    private static Population agents;
//    private static Population tests;
//    private static final int INIT_AMOUNT = 20;
//    private static final int GENERATIONS = 20000;
//    private static final int ENCOUNTERS = 20;
//    private static final int SEGMENT_LENGTH = 60;
//    private static final int DIFFICULTY = 2;
//    private static final int MORE_CHANCE = 4;
//    private static final int TIME_LIMIT = 30;
//    private static  int testCount = 0;
//    
//    private static int maxTestCount;
//    
//    
//    
//    public static void main(String[] args) {
//        
//        EvaluationOptions options = new CmdLineOptions(args);
//        options.setNumberOfTrials(1);
//        options.setPauseWorld(false);
//        options.setAgent(initial);
//        options.setMaxFPS(true);
//        options.setLevelDifficulty(DIFFICULTY);
//        options.setVisualization(false);
//        options.setTimeLimit(30);
//        
//        maxTestCount = INIT_AMOUNT * (AGENT_POP_SIZE + TEST_POP_SIZE) + GENERATIONS * (ENCOUNTERS + INIT_AMOUNT);
//        
//        ArrayList<Coevolvable> agentPop = new ArrayList<>();
//        for (int i = 0; i < AGENT_POP_SIZE; i++) {
//            agentPop.add(new AgentObject((SimpleNNAgent)initial.getNewInstance()));
//        }
//        ArrayList<Coevolvable> levelPop = new ArrayList<>();
//        ArrayList<Level> levels = LevelGenerator.createLevelSegments(TEST_POP_SIZE,24, DIFFICULTY, 0, SEGMENT_LENGTH);
//        for (int i = 0; i < levels.size(); i++) {
//            levelPop.add(new LevelObject(levels.get(i)));
//        }
//        
//        agents = new Population(agentPop, MORE_CHANCE);
//        tests = new Population(levelPop, MORE_CHANCE);
//        
//        initPops(options);
//        
//        CoevolutionTask task = new CoevolutionTask(options);
//        for (int i = 0; i < GENERATIONS; i++) {
//            if(i % 20 ==0){
//                System.out.println("Generation " + i + " of " + GENERATIONS);
//                System.out.println("Best fitness : " + agents.getMembers().get(0).getAvgFitness() );
//            }
//                
//            for (int j = 0; j < ENCOUNTERS; j++) {
//
//                options = new CmdLineOptions(args);
//                options.setNumberOfTrials(1);
//                options.setPauseWorld(false);
//                options.setAgent(initial);
//                options.setMaxFPS(true);
//                options.setLevelDifficulty(2);
//                options.setVisualization(false);
//                options.setTimeLimit(20);
//                task = new CoevolutionTask(options);
//                AgentObject agent = (AgentObject) agents.select();
//                LevelObject level = (LevelObject) tests.select();
//                
//                double[] results = task.evaluate(agent.getAgent(),level.getLevel(), false);
//                agent.push(results[0]);
//                level.push(results[1]);
//                agents.update(agent);
//                tests.update(level);
//                testCount++;
//                if(testCount % 100 == 0){
//                    System.out.println((double)testCount / maxTestCount);
//                }
//            }
//            AgentObject firstAgent = (AgentObject)agents.select();
//            AgentObject secondAgent = (AgentObject)agents.select();
//            SimpleNNAgent f1 = (SimpleNNAgent) firstAgent.getAgent();
//            SimpleNNAgent f2 = (SimpleNNAgent) secondAgent.getAgent();
//            NeuralNetwork newNet = f1.getNetwork().twoPointRecombine(f2.getNetwork());
//            if(f1 != null && f2 != null){
//                SimpleNNAgent child = new SimpleNNAgent(newNet);
//                AgentObject childObj = new AgentObject(child);
//                testFitnessAndInsert(childObj, options);
//            }
//        }
//        
//        options.setVisualization(true);
//        options.setMaxFPS(false);
//        AgentObject agent = (AgentObject)agents.getMembers().get(0);
//        
//        int seed = 23;
//        for (int i = 0; i < 10; i++) {
//            options = new CmdLineOptions(args);
//            options.setNumberOfTrials(1);
//            options.setPauseWorld(false);
//            options.setAgent(initial);
//            options.setMaxFPS(false);
//            options.setLevelDifficulty(DIFFICULTY);
//            options.setVisualization(false);
//            options.setTimeLimit(20);
//            LevelObject level = (LevelObject)tests.getMembers().get(i);
//            task.evaluate(agent.getAgent(), level.getLevel(), false);
//        }
//        options.setMaxFPS(false);
//        options.setTimeLimit(100);
//        
//        while(true){
//            Level level = LevelGenerator.createLevel(300, 15, seed, DIFFICULTY, 0);
//            double[] outputs = task.evaluate(agent.getAgent(), level, false);
//            seed++;
//        }
//        
////        Level level = LevelGenerator.createLevel(300, 15, 24, 0, 0);
////        Level level2 = LevelGenerator.createLevel(300, 15, 24, 1, 0);
////        Level level3 = LevelGenerator.createLevel(300, 15, 24, 2, 0);
////        Level level4 = LevelGenerator.createLevel(300, 15, 24, 3, 0);
////        
////        task.evaluate(agent.getAgent(), level);
////        task.evaluate(agent.getAgent(), level2);
////        task.evaluate(agent.getAgent(), level3);
////        task.evaluate(agent.getAgent(), level4);
//        
////        for (int i = 0; i < tests.getMembers().size(); i++) {
////            LevelObject level = (LevelObject)tests.getMembers().get(i);
////            task.evaluate(agent.getAgent(), level.getLevel());
////        }
////        
//        
//    }
//    
//    private static void testFitnessAndInsert(AgentObject aObj, EvaluationOptions options){
//        CoevolutionTask task = new CoevolutionTask(options);
//        for (Coevolvable level : tests.selectMany(INIT_AMOUNT)) {
//                LevelObject lObj = (LevelObject) level;
//                double[] results = task.evaluate(aObj.getAgent(), lObj.getLevel(), false);
//                aObj.push(results[0]);
////                testCount++;
////                 if(testCount % 100 == 0){
////                    System.out.println((double)testCount / maxTestCount);
////                }
//        }
//        if(aObj.getAvgFitness() > agents.getLowestFitness()){
//            agents.push(aObj);
//        }
//    }
//    
//    private static void initPops(EvaluationOptions options){
//        CoevolutionTask task = new CoevolutionTask(options);
//        for (Coevolvable agent : agents.getMembers()) {
//            AgentObject aObj = (AgentObject) agent;
//            for (Coevolvable level : tests.getRandomSet(INIT_AMOUNT)) {
//                LevelObject lObj = (LevelObject) level;
//                double[] results = task.evaluate(aObj.getAgent(), lObj.getLevel(), false);
//                aObj.push(results[0]);
////                testCount++;
////                if(testCount % 100 == 0){
////                    System.out.println((double)testCount / maxTestCount);
////                }
//            }
//        }
//        for (Coevolvable level : tests.getMembers()) {
//            LevelObject lObj = (LevelObject) level;
//            for (Coevolvable agent : agents.getRandomSet(INIT_AMOUNT)) {
//                AgentObject aObj = (AgentObject) agent;
//                double[] results = task.evaluate(aObj.getAgent(), lObj.getLevel(), false);
//                lObj.push(results[1]);
////                testCount++;
////                if(testCount % 100 == 0){
////                    System.out.println((double)testCount / maxTestCount);
////                }
//            }
//        }
//        
//        agents.sort();
//        tests.sort();
//    }
//    
//}
//
//class SimThread implements Runnable{
//
//    private LevelObject lObj;
//    private AgentObject aObj;
//    private CoevolutionTask task;
//    private SimCount count;
//    
//    public SimThread(LevelObject lObj, AgentObject aObj, CoevolutionTask task, SimCount count){
//        this.lObj = lObj;
//        this.aObj = aObj;
//        this.task = task;
//        this.count = count;
//    }
//    
//    @Override
//    public void run() {
//        double[] results = task.evaluate(aObj.getAgent(), lObj.getLevel(), false);
//        aObj.push(results[0]);
//        lObj.push(results[1]);
//        count.increment();
//    }
//    
//}
//
//class SimCount {
//    
//    private Semaphore waitForSim;
//    private int count = 0;
//    private int waitThreshold;
//    
//    public SimCount(Semaphore waitForSim, int waitThreshold){
//        this.waitForSim = waitForSim;
//        this.waitThreshold = waitThreshold;
//    }
//    
//    public void increment(){
//        count++;
//        if(count >= waitThreshold){
//            waitForSim.release();
//        }
//    };
//    
//}
//
