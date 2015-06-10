/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import java.util.ArrayList;

/**
 *
 * @author Jan
 */
public class MultipleNNAgent  implements Agent , Evolvable {

    private NeuralNetwork platformNet;
    private NeuralNetwork enemyNet;
    private NeuralNetwork gapNet;
//    private NeuralNetwork obstacleNet;
    private NeuralNetwork coupledNet;
    private NeuralNetwork modeNet;
    private NeuralNetwork[] networks = new NeuralNetwork[]{platformNet, enemyNet, gapNet,modeNet, coupledNet};
    private String name = "MultipleNNAgentDefault";
    private double[] thresholds = new double[]{0,0,0,0,0}; // every threshold at 0.6
    private final int nrOfEnemies = 5;
    private final int nrOfIntermediateOutputs = 3;
    private final int nrOfPlatforms = 8;
//    private final int nrOfObstacles = 6;
    private final int nrOfGaps = 1;
    private final int nrOfOutputs = 5;
    private final int nrOfModes = 4;
    private boolean hasJumped = false;
    
    
    public MultipleNNAgent(){
        //go for 2 enemies + 2 platforms = 2 * 3 + 2 * 4
        // 1 for mario mode
        this.platformNet = new NeuralNetwork(nrOfPlatforms * 4, (int) (nrOfPlatforms * 4 * 1.5), nrOfIntermediateOutputs);
        this.enemyNet = new NeuralNetwork(nrOfEnemies * 3, (int) (nrOfEnemies * 3 * 1.5), nrOfIntermediateOutputs);
        this.gapNet = new NeuralNetwork(nrOfGaps * 2, (int) (nrOfGaps * 2 * 1.5), nrOfIntermediateOutputs);
//        this.obstacleNet = new NeuralNetwork(nrOfObstacles * 2, (int) (nrOfObstacles * 2 * 1.5), nrOfIntermediateOutputs);
        // TODO : add mode to coupled net????
        this.modeNet = new NeuralNetwork(nrOfModes , (int) (nrOfModes * 1.5), nrOfIntermediateOutputs);
        this.coupledNet = new NeuralNetwork(nrOfIntermediateOutputs * (networks.length - 1),(int) ( nrOfIntermediateOutputs * (networks.length - 1) * 1.5), nrOfOutputs);
        networks = new NeuralNetwork[]{platformNet, enemyNet, gapNet,modeNet, coupledNet};
    }
    
    public MultipleNNAgent(NeuralNetwork[] networks){
        this.networks = networks;
        platformNet = networks[0];
        enemyNet = networks[1];
        gapNet = networks[2];
//        obstacleNet = networks[3];
        modeNet = networks[3];
        coupledNet = networks[4];
    }
    
    @Override
    public void reset() {
    }

    @Override
    public boolean[] getAction(Environment observation) {
        //TODO : Map observation to neural network;
        byte[][] fullObservation = observation.getCompleteObservation();
        int marioMode = observation.getMarioMode();
//        for (int i = 0; i < fullObservation.length; i++) {
//            for (int j = 0; j < fullObservation[i].length; j++) {
//                if(fullObservation[i][j] == (byte)25)
//                {
//                    double d = 0;
//                }
//            }
//        }
        
        ArrayList<int[]> enemies = getEnemyCoordsAndType(observation, 1);
        ArrayList<int[]> platforms = getPlatformObservation(observation);
        ArrayList<int[]> gaps = getGapsObservations(observation);
        double[] modes = getModes(observation);
        
        double[] enemyInputs = getInputs(enemies, nrOfEnemies, 3);
        double[] platformInputs = getInputs(platforms, nrOfPlatforms, 4);
        double[] gapInputs = getInputs(gaps, nrOfGaps, 2);
        
        double[] enOutputs = enemyNet.propagate(enemyInputs);
        double[] platOutputs = platformNet.propagate(platformInputs);
        double[] gapOutputs = gapNet.propagate(gapInputs);
        double[] modeOutputs = modeNet.propagate(modes);
        
        double[][] allInputs = new double[][]{enOutputs, platOutputs, gapOutputs, modeOutputs};
        
        double[] inputs = new double[enOutputs.length + platOutputs.length + gapOutputs.length + modeOutputs.length]; 
        
        int count = 0;
        for (int i = 0; i < allInputs.length; i++) {
            for (int j = 0; j < allInputs[i].length; j++) {
                inputs[count] = allInputs[i][j];
                count++;
            }
        }
        
        boolean[] actions = new boolean[5];
        double[] outputs = coupledNet.propagate(inputs);
        // speed and jump 
        for (int i = 0; i < outputs.length; i++) {
            if(i == Mario.KEY_SPEED  ) 
            actions[i] = outputs[i] > thresholds[i];
            else if(i == Mario.KEY_JUMP && observation.isMarioOnGround())// can only jump if on ground
            {
                if(!hasJumped)
                {
                    boolean jump = outputs[i] > thresholds[i];
                    if(jump){
                        actions[i] = true;
                        hasJumped = true;
                    }
                }
                else
                    hasJumped = false;
            }
            else if(i == Mario.KEY_JUMP){
                actions[Mario.KEY_JUMP] = outputs[i] > thresholds[i];
            }
        }
        
        //only one movement
        double maxDirOutput = 0;
        int action = -1;
        for (int i = 0; i < outputs.length; i++) {
            if(i == Mario.KEY_DOWN || i == Mario.KEY_LEFT || i == Mario.KEY_RIGHT){
                if(maxDirOutput < outputs[i] && outputs[i] > thresholds[i])
                {
                    maxDirOutput = outputs[i];
                    action = i;
                }
            }
        }
        if(action > 0 )
        {
            actions[action] = true;
        }
        
        return actions;
    }
    
    public double[] getModes(Environment e){
        int marioMode = e.getMarioMode();
        int jump = e.mayMarioJump() ? 1 : 0;
        int carrying = e.isMarioCarrying() ? 1 :0 ;
        int onGround = e.isMarioOnGround() ? 1 :0 ;
        
        return new double[]{marioMode, jump, carrying, onGround};
    }
    
    public MultipleNNAgent recombine(MultipleNNAgent other){
        NeuralNetwork[] newNets = new NeuralNetwork[networks.length];
        NeuralNetwork[] otherNets = other.getNetworks();
        for (int i = 0; i < otherNets.length; i++) {
            newNets[i] = networks[i].twoPointRecombine(otherNets[i]);
            newNets[i].mutate();
        }
        return new MultipleNNAgent(newNets);
    }
    
    private double[] getInputs(ArrayList<int[]> vals, int nrOfVals, int elementSize){
        ArrayList<Double> inputs = new ArrayList<>();
        double[] inputArray = new double[nrOfVals * elementSize];
        for (int i = 0; i < nrOfVals; i++) {
            if(i < vals.size()){
                int[] el = vals.get(i);
                for (int j = 0; j < el.length; j++) {
                    inputs.add((double)el[j]);
                }
            }
            else
                for(int j = 0; j < elementSize; j ++)
                    inputs.add((double)0);
        }
        for (int i = 0; i < inputs.size(); i++) {
            inputArray[i] = inputs.get(i);
        }
        return inputArray;
    }
    
    private double[] getInputs(ArrayList<int[]> enemies, ArrayList<int[]> platforms){
        ArrayList<Double> inputs = new ArrayList<>();
        double[] inputArray = new double[1 + nrOfEnemies * 3 + nrOfPlatforms * 4];
        for (int i = 0; i < nrOfEnemies; i++) {
            if(i < enemies.size()){
                int[] enemy = enemies.get(i);
                for (int j = 0; j < enemy.length; j++) {
                    inputs.add((double)enemy[j]);
                }
            }
            else
                for(int j = 0; j < 3; j ++)
                    inputs.add((double)0);
        }
        for (int i = 0; i < nrOfPlatforms; i++) {
            if(i < platforms.size()){
                int[] platform = platforms.get(i);
                for (int j = 0; j < platform.length; j++) {
                    inputs.add((double)platform[j]);
                }
            }
            else
                for(int j = 0; j < 4; j ++)
                    inputs.add((double)0);
        }
        for (int i = 0; i < inputs.size(); i++) {
            inputArray[i] = inputs.get(i);
        }
        return inputArray;
    }

    @Override
    public Agent.AGENT_TYPE getType() {
        return Agent.AGENT_TYPE.AI;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Evolvable getNewInstance() {
        return new MultipleNNAgent();
    }

    @Override
    public Evolvable copy() {
        NeuralNetwork[] newNets = new NeuralNetwork[networks.length];
        for (int i = 0; i < networks.length; i++) {
            newNets[i] = networks[i].copy();
        }
        return new MultipleNNAgent(newNets);
    }

    @Override
    public void mutate() {
        for(NeuralNetwork net : networks){
            net.mutate();
        }
    }
    
    public NeuralNetwork[] getNetworks(){
        return networks;
    }
    
    /**
     * Returns a list of all enemies with an int array {x, y, type}
     * @param e
     * @param zLevel
     * @return 
     */
    private ArrayList<int[]> getEnemyCoordsAndType(Environment e, int zLevel){
        byte[][] enemies = e.getEnemiesObservationZ(zLevel);
        ArrayList<int[]> eList = new ArrayList<>();
        for (int i = 0; i < enemies.length; i++) {
            for (int j = 0; j < enemies[i].length; j++) {
                if(enemies[i][j] != 0){
//                    System.out.println("ENEMYYYY!!!");
                    eList.add(new int[]{i, j, enemies[i][j]});
                }
            }
        }
        return eList;
    }
    
    
    private ArrayList<int[]> getGapsObservations(Environment e){
        byte[][] platforms = e.getLevelSceneObservationZ(1);
        ArrayList<int[]> gapList = new ArrayList<>();
        int gapStart =0;
        boolean gap = false;
        for (int i = 0; i < platforms[0].length; i++) {
            int count = 0;
            for (int j = 0; j < platforms.length; j++) {
                if(platforms[j][i] != 0){
                    count++;
                    break;
                }
            }
            if(count > 0 ) // no gap
            {
                if(gap) // add gap
                {
                    gapList.add(new int[]{gapStart, i - 1});
                }
                gap = false;
            }
            else{ // gap
                if(!gap) // gapstart
                {
                    gapStart = i;
                    gap = true;
                }
                if(i == platforms[0].length - 1){ // add gap to end
                    gapList.add(new int[]{gapStart, i});
                }
            }
        }
        return gapList;
    }
    
    
    /**
     * Method to detect platforms (and gaps??) 
     * @param e
     * @return int[] for platform {xStart, yStart, length, type}
     */
    private ArrayList<int[]> getPlatformObservation(Environment e){
        //TODO: implement gaps || GAPS IMPLIED????
        byte[][] platforms = e.getLevelSceneObservationZ(1);
        ArrayList<int[]> pList = new ArrayList<>();
        ArrayList<Integer> platKinds = getPlatKinds();
        boolean platHit = false;
        int prevPlatKind = 0;
        int platLength = 0;
        for (int i = 0; i < platforms.length; i++) {
            for (int j = 0; j < platforms[i].length; j++) {
                if(platKinds.contains((int)platforms[i][j])){
                    if(platHit){ // check if it matches previous type
                        if(platforms[i][j] == prevPlatKind){ // platform continues
                            platLength++;
                        }
                        else // new platform diff kind
                        {
                            addPlatForm(pList, j, i, platLength, prevPlatKind);
                            platLength = 1;
                            prevPlatKind = platforms[i][j];
                        }
                    }
                    else
                    {
                        platHit = true;
                        prevPlatKind = platforms[i][j];
                        platLength = 1;
                    }
                }
                else if(platHit) // add a new platform to the list
                {
                    addPlatForm(pList, j, i, platLength, prevPlatKind);
                    platHit = false;
                }
            }
            if(platHit) // line ended, add platform
            {
                addPlatForm(pList, platforms[i].length, i, platLength, prevPlatKind);
                platHit = false;
            }
        }
        return pList;
    }
    
    /**
     * Adds a platform to the list
     * @param platList
     * @param x curr x of platform
     * @param y curr y of platform
     * @param platLength 
     */
    private void addPlatForm(ArrayList platList, int x, int y, int platLength, int platType){
        platList.add(new int[]{x - platLength, y, platLength, platType});
    }
    
    private ArrayList<Integer> getPlatKinds(){
        ArrayList<Integer> pKinds = new ArrayList<>();
        pKinds.add(-10);
        pKinds.add(-12);
        pKinds.add(-11);
        pKinds.add(-20);
        pKinds.add(16);
        pKinds.add(21);
        return pKinds;
    }
    
    
}