/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import ch.idsia.ai.Evolvable;
import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.SimpleMLPAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Jan
 */
public class SimpleNNAgent implements Agent, Evolvable, Serializable {

    private NeuralNetwork network;
    private String name = "SimpleNNAgentDefault";
    private final double left_thresh = 1/3;
    private final double down_thresh = 0.5;
    private final double jump_thresh = 0.5;
    private final double speed_thresh = 0.5;
    private final double right_thresh = 2/3;
    
    private final double THRESHOLD = 0.5;
    
    private final int sizeOfGap = 4;
    private final int sizeOfEnemy = 3;
    private final int sizeOfPlatform = 4;
    private final int sizeOfObstacle = 2;
    private final double SQUARE_FACTOR = 16;
    
    
    private float xFloat;
    private float yFloat;
    
    private final int nrOfModes = 4;
    private final int nrOfEnemiesFront = 2; // 9 + 1 behind
    private final int nrOfEnemiesBehind = 0;
    private final int nrOfPlatformsFront = 3; // 9 + 1 behind
    private final int nrOfPlatformsBehind = 0; // 9 + 1 behind
    private final int nrOfObstaclesFront = 1; // 9 + 1
    private final int nrOfObstaclesBehind = 0; // 9 + 1
    private final int nrOfGaps = 1; 
    private final int nrOfHidden = 12;
    private final int nrOfOutputs = 5;
    private final static int marioPos = 11;
    private boolean hasJumped = false;
    private static ArrayList<Integer> pKinds = new ArrayList<>();
    private int totalNrOfInputs;
    
    public SimpleNNAgent(){
        //go for 2 enemies + 2 platforms = 2 * 3 + 2 * 4
        // 2 for remaining platform below and above
        totalNrOfInputs = 2 +nrOfModes + (nrOfEnemiesFront + nrOfEnemiesBehind) * sizeOfEnemy + (nrOfPlatformsFront + nrOfPlatformsBehind) * sizeOfPlatform + (nrOfObstaclesFront + nrOfObstaclesBehind) * sizeOfObstacle + nrOfGaps * sizeOfGap;
        this.network = new NeuralNetwork(totalNrOfInputs, nrOfHidden, nrOfOutputs);
        if(pKinds.isEmpty())
        {
            pKinds.add(-10);
            pKinds.add(-12);
            pKinds.add(-11);
            pKinds.add(-20);
            pKinds.add(16);            
            pKinds.add(20);
            pKinds.add(21);
        }
    }
    
    public SimpleNNAgent(NeuralNetwork network){
        // the 1 input is for the length of the platform mario currently has left
        totalNrOfInputs = 2 +  nrOfModes + (nrOfEnemiesFront + nrOfEnemiesBehind) * sizeOfEnemy + (nrOfPlatformsFront + nrOfPlatformsBehind) * sizeOfPlatform + (nrOfObstaclesFront + nrOfObstaclesBehind) * sizeOfObstacle + nrOfGaps * sizeOfGap;
        if(pKinds.isEmpty())
        {
            pKinds.add(-10);
            pKinds.add(-12);
            pKinds.add(-11);
            pKinds.add(-20);
            pKinds.add(16);
            pKinds.add(20);
            pKinds.add(21);
        }
        this.network = network;
    }
    
    @Override
    public void reset() {
    }

    
    private int[] getRemainingPlatform(ArrayList[] platforms){
        int belowLength =-1, upperLength =-1; 
        for (ArrayList list :platforms) {
             for (int i = 0; i < list.size(); i++) {
                int[] plat = (int[]) list.get(i);
                if(plat[1] == 1 && plat[0] <= 0 && plat[0] + plat[2] >= 0)
                {
                    belowLength= plat[0] + plat[2];
                }
                if(plat[1] <= 0 && plat[0] <= 0 && plat[0] + plat[2] >= 0 && (plat[3] == 16 || plat[3] == 21)) // platform above mario
                {
                    upperLength = plat[0] + plat[2];
                }

            }
        }
        return new int[]{belowLength, upperLength};
    }
    
    @Override
    public boolean[] getAction(Environment observation) {
        //TODO : Map observation to neural network;
//        for (int i = 0; i < fullObservation.length; i++) {
//            for (int j = 0; j < fullObservation[i].length; j++) {
//                if(fullObservation[i][j] == (byte)25)
//                {
//                    double d = 0;
//            }
//        }
        float[] pos = observation.getMarioFloatPos();
        xFloat = pos[0];
        yFloat = pos[1];
        
        ArrayList[] platforms = orderAndShiftInputs(getAdvancedPlatformObservation(observation));
        ArrayList[] enemies = orderAndShiftInputsDouble(getAdvancedEnemyObservation(observation));
        ArrayList[] obstacles = getObstacleObservation(observation, platforms);
        
        ArrayList<int[]> gaps = getGapsObservations(observation, platforms);
        double[] modes = getModes(observation);
        
        boolean[] actions = new boolean[5];
        double[] inputs = getInputs(modes, getRemainingPlatform(platforms), enemies, platforms, obstacles, gaps);
        double[] outputs = network.propagate(inputs);
        // speed and jump 
        
        for (int i = 0; i < outputs.length; i++) {
            actions[i] = outputs[i] > THRESHOLD;
//            if(i == Mario.KEY_JUMP && (!observation.mayMarioJump() && observation.isMarioOnGround())) // if mario may not jump
//            {
//                actions[i] = false;
//            }
        }
        
//        for (int i = 0; i < outputs.length; i++) {
//            if(i == Mario.KEY_SPEED - 1) 
//            actions[i + 1] = outputs[i] > speed_thresh;
//            else if (i == Mario.KEY_JUMP -1)
//                actions[Mario.KEY_JUMP] = outputs[i] > jump_thresh;
//
////            else if(i == Mario.KEY_JUMP - 1 && observation.isMarioOnGround())// can only jump if on ground
////            {
////                if(!hasJumped)
////                {
////                    boolean jump = outputs[i] > jump_thresh;
////                    if(jump){
////                        actions[i + 1] = true;
////                        hasJumped = true;
////                    }
////                }
////                else
////                    actions[Mario.KEY_JUMP] = outputs[i] > jump_thresh;
////
////            }
////            else if(!observation.isMarioOnGround() && i == Mario.KEY_SPEED - 1){
////            }
//        }
//        
//        
//        // JUMP BUG FIX
//        if(observation.isMarioOnGround())
//        {
//            if(hasJumped)
//            {
//                actions[Mario.KEY_JUMP] = false;
//                hasJumped = false;
//            }
//            else if(actions[Mario.KEY_JUMP])
//            {
//                hasJumped = true;
//            }
//        }
//        else 
//            hasJumped = false;
//        
//        for (int i = 0; i < actions.length; i++) {
//            switch(i)
//            {
//                case 0:
//                    if(outputs[0] < left_thresh) actions[0] = true;
//                    else if (outputs[0] > right_thresh) actions[1] = true;
//                    break;
//                case 2:
//                    actions[2] = outputs[1] > down_thresh;
//            }
//        }
        
        return actions;
    }
    
    private ArrayList<int[]> getGapsObservations(Environment e, ArrayList[] platforms){
        byte[][] observation = e.getLevelSceneObservationZ(1);
        ArrayList<int[]> gapList = new ArrayList<>();
        int gapStart =0;
        boolean gap = false;
        for (int i = 0; i < observation[0].length; i++) {
            int count = 0;
            for (int j = 0; j < observation.length; j++) {
                if(observation[j][i] != 0){
                    count++;
                    break;
                }
            }
            if(count > 0 ) // no gap
            {
                if(gap) // add gap
                {
                    if(gapStart - marioPos >= 0 || i - 1 - marioPos >= 0)
                    {
                        int gapHeightStart = -11, gapHeightEnd =-11;
                        for (ArrayList list : platforms) {
                            for (int j = 0; j < list.size(); j++) {
                                int[] plat = (int[]) list.get(j);
                                if(plat[0] + plat[2] + marioPos == gapStart) // start of gap
                                {
                                    if(plat[1] > gapHeightStart) gapHeightStart = plat[1];
                                }
                                if(plat[0] + marioPos  == i) // end of gap
                                {
                                    if(plat[1] > gapHeightEnd) gapHeightEnd = plat[1];
                                }
                            }
                        }
                        gapList.add(new int[]{gapStart - marioPos, i - 1 - marioPos, gapHeightStart, gapHeightEnd});
                    }
                }
                gap = false;
            }
            else{ // gap
                if(!gap) // gapstart
                {
                    gapStart = i;
                    gap = true;
                }
                if(i == observation[0].length - 1){ // add gap to end
                    int gapHeightStart = -11;
                    for (ArrayList list : platforms) {
                            for (int j = 0; j < list.size(); j++) {
                                int[] plat = (int[]) list.get(j);
                                if(plat[0] + plat[2] + marioPos == gapStart) // start of gap
                                {
                                    if(plat[1] > gapHeightStart) gapHeightStart = plat[1];
                                }
                            }
                    }
                    gapList.add(new int[]{gapStart, i, gapHeightStart, 0}); // no ending
                }
            }
        }
        return gapList;
    }
    
    
     public double[] getModes(Environment e){
        int marioMode = e.getMarioMode();
        int jump = e.mayMarioJump() ? 1 : 0;
        int carrying = e.isMarioCarrying() ? 1 :0 ;
        int onGround = e.isMarioOnGround() ? 1 :0 ;
        
        return new double[]{marioMode, jump, carrying, onGround};
    }
     
    private double[] getInputs(double[] modes, int[] remainders, ArrayList[] enemies, ArrayList[] platforms, ArrayList[] obstacles, ArrayList<int[]> gaps){
        ArrayList<Double> inputs = new ArrayList<>();
        double[] inputArray = new double[totalNrOfInputs];
        inputs.add((double)remainders[0]);
        inputs.add((double)remainders[1]);
        for (double d : modes){
            inputs.add(d);
        }
        addFrontAndBehind(inputs, enemies, nrOfEnemiesFront, nrOfEnemiesBehind, sizeOfEnemy);
        addFrontAndBehind(inputs, shiftToMarioPosition(platforms), nrOfPlatformsFront, nrOfPlatformsBehind, sizeOfPlatform);
        addFrontAndBehind(inputs, shiftToMarioPosition(obstacles), nrOfObstaclesFront, nrOfObstaclesBehind, sizeOfObstacle);
        ArrayList<double[]> newGaps = shiftGapsToMarioPosition(gaps);
        for (int i = 0; i < nrOfGaps; i++) {
            if (i < gaps.size()) {
                double[] gap = newGaps.get(i);
                for (int j = 0; j < gap.length; j++) {
                    inputs.add((double)gap[j]);
                }
            }
            else
            {
                for (int j = 0; j < sizeOfGap; j++) {
                    inputs.add((double)0);
                }
            }
        }
       for (int i = 0; i < inputs.size(); i++) {
            inputArray[i] = inputs.get(i);
        }
        return inputArray;
    } 
    
    private ArrayList<double[]> shiftGapsToMarioPosition(ArrayList<int[]> gaps)
    {
        double xRem =( xFloat % SQUARE_FACTOR) / (SQUARE_FACTOR );
        double yRem = (yFloat % SQUARE_FACTOR )/ (SQUARE_FACTOR );
        ArrayList<double[]> returnData = new ArrayList();
        for (int i = 0; i < gaps.size(); i++) {
            int[] data = gaps.get(i);
            double[] newData = new double[data.length];
            newData[0] = data[0] - xRem;
            newData[1] = data[1] - xRem;
            newData[2] = data[2] - yRem;
            newData[3] = data[3] - yRem;
            returnData.add(newData);
        }
        return returnData;
    }
    
    private ArrayList[] shiftToMarioPosition(ArrayList[] inputs)
    {
        double xRem =( xFloat % SQUARE_FACTOR) / (SQUARE_FACTOR );
        double yRem = (yFloat % SQUARE_FACTOR )/ (SQUARE_FACTOR );
        ArrayList[] returnData = new ArrayList[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            returnData[i] = new ArrayList<>();
            ArrayList<int[]> list = inputs[i];
            for (int[] data : list) {
                double[] newData = new double[data.length];
                newData[0] = data[0] - xRem;
                newData[1] = data[1] - yRem;
                for (int j = 2; j < data.length; j++) {
                    newData[j] = data[j];
                }
                returnData[i].add(newData);
            }
        }
        return returnData;
        
    }
    
    
    private void addFrontAndBehind(ArrayList<Double> inputs, ArrayList[] values, int nrOfFront, int nrOfBack, int amountOfValues)
    {
        ArrayList<double[]> enemiesFront = values[0];
        for (int i = 0; i < nrOfFront; i++) {
            if(i < enemiesFront.size()){
                double[] enemy = enemiesFront.get(i);
                for (int j = 0; j < enemy.length; j++) {
                    inputs.add((double)enemy[j]);
                }
            }
            else
                for(int j = 0; j < amountOfValues; j ++)
                    inputs.add((double)0);
        }
        ArrayList<double[]> enemiesBack = values[1];
        for (int i = 0; i < nrOfBack; i++) {
            if(i < enemiesBack.size()){
                double[] enemy = enemiesBack.get(i);
                for (int j = 0; j < enemy.length; j++) {
                    inputs.add((double)enemy[j]);
                }
            }
            else
                for(int j = 0; j < amountOfValues; j ++)
                    inputs.add((double)0);
        }
    }


    @Override
    public AGENT_TYPE getType() {
        return AGENT_TYPE.AI;
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
        return new SimpleNNAgent(new NeuralNetwork(network.getNrOfInputs(), network.getNrOfHidden(), network.getNrOfOutputs()));
    }

    @Override
    public Evolvable copy() {
        return new SimpleNNAgent(network.copy());
    }

    @Override
    public void mutate() {
        network.mutate();
    }
    
    public NeuralNetwork getNetwork(){
        return network;
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
    
    private ArrayList<double[]> getAdvancedEnemyObservation(Environment e){
        byte[][] enemies = e.getEnemiesObservation();
        float[] enemiesFloat = e.getEnemiesFloatPos();
        
        ArrayList<double[]> eList = new ArrayList<>();
        for (int i = 0; i < enemiesFloat.length; i += 3) {
            double eX = (enemiesFloat[i + 1] - xFloat) / SQUARE_FACTOR;
            double eY = (enemiesFloat[i + 2] - yFloat) / SQUARE_FACTOR;
            double type = enemiesFloat[i];
            eList.add(new double[]{eX, eY, type});
        }
//        for (int i = 0; i < enemies[0].length; i++) { //y 
//            for (int j = 0; j < enemies.length; j++) { //x
//                if(enemies[j][i] != 0)
//                {
//                    eList.add(new int[]{j, i, enemies[j][i]});
//                }
//            }
//        }
        return eList;
    }
    
    /**
     * Use the ordered platform array as input
     * @param e
     * @param platForms
     * @return {x, y1, length}
     */
    private ArrayList[] getObstacleObservation(Environment e, ArrayList[] platForms){
        byte[][] platforms = e.getLevelSceneObservationZ(1);
        
        ArrayList<int[]> frontList = new ArrayList<>();
        ArrayList<int[]> backList = new ArrayList<>();
        
        for (int i = 0; i < platforms[0].length; i++) {
            byte[] o = new byte[platforms.length];
            for (int j = 0; j < o.length; j++) {
                o[j] = platforms[j][i];
            }
            ArrayList<int[]> obstacles = getPlatforms(o, i);
            // X AND Y INVERSE HERE
            
            for (int[] obst : obstacles) {
                int corrX = i - marioPos;
                
                if(corrX >= 0)//front
                {
                    ArrayList<int[]> platList = platForms[0];
                    //X AND Y REVERSE
                    for (int[] plat : platList) {
                        if(corrX < plat[0]) break;
                        if(corrX == plat[0]) //underneath the start of a platform
                        {
                            int corrY = obst[0] - marioPos;
                            if(corrY== plat[1]){ // y correct
                                //TODO : CHECK FOR OTHER PLATFORMS
                                boolean added = false;
                                for (int[] plat2 : platList) {
                                    if(corrX < plat[0]) break;
                                    if(plat2[1] > obst[0]  && plat2[1] < obst[0] + obst[2] -1 && plat2[0] + plat2[2] + 1 == corrX) // ends in the obstacle
                                    {
                                        added = true;
                                        frontList.add(new int[]{corrX, corrY}); //, plat[1] - corrY
                                    }
                                }
                                if(!added)
                                    frontList.add(new int[]{corrX, corrY}); // , obst[2]
                            }
                        }
                    }
                }
                else // BACKLIST
                {
                    ArrayList<int[]> platList = platForms[1];
                    //X AND Y REVERSE
                    for (int[] plat : platList) {
                        if(corrX > plat[0]) break;
                        if(corrX == plat[0]) //underneath the start of a platform
                        {
                            int corrY = obst[0] - marioPos;
                            if(corrY == plat[1]){ // y correct
                                //TODO : CHECK FOR OTHER PLATFORMS
                                boolean added = false;
                                for (int[] plat2 : platList) {
                                    if(corrX > plat[0]) break;
                                    if(plat2[1] > obst[0]  && plat2[1] < obst[0] + obst[2] - 1 && plat2[0] + plat2[2] + 1== corrX) // ends in the obstacle
                                    {
                                        added = true;
                                        backList.add(new int[]{corrX,corrY}); //,plat[1] - corrY
                                    }
                                }
                                if(!added)
                                    backList.add(new int[]{corrX, corrY });//, obst[2]
                            }
                        }
                    }
                }                
            }
            
        }
        
        return new ArrayList[]{frontList, backList};
    }
    
    /**
     * Returns the input in two ordered lists, based on the front and the back of mario
     * the two lists are first ordered on the x pos of mario (short, far) and then y (low, high)
     * @param inputs
     * @return {front, back}
     */
    private ArrayList[] orderAndShiftInputs(ArrayList<int[]> inputs){
        ArrayList<int[]> frontList = new ArrayList<>();
        ArrayList<int[]> backList = new ArrayList<>();
        for (int[] input : inputs) {
            input[0] = input[0] - marioPos;
            input[1] = input[1] - marioPos;
            if(input[0] >= 0) //frontlist
            {
                boolean added = false;
                for (int i = 0; i < frontList.size(); i++) {
                    int[] other = frontList.get(i);
                    if(input[0] < other[0] || (input[0] == other[0] && input[1] <= other[1] ))
                    {
                        added = true;
                        frontList.add(i, input);
                        break;
                    }
                }
                if(!added) frontList.add(input);
            }
            else
            {
                boolean added = false;
                for (int i = 0; i < backList.size(); i++) {
                    int[] other = backList.get(i);
                    if(input[0] > other[0]|| (input[0] == other[0] && input[1] <= other[1] ))
                    {
                        added=true;
                        backList.add(i, input);
                        break;
                    }
                }
                if(!added) backList.add(input);
            }
        }
        return new ArrayList[]{frontList, backList};
    }
    
    private ArrayList[] orderAndShiftInputsDouble(ArrayList<double[]> inputs){
        ArrayList<double[]> frontList = new ArrayList<>();
        ArrayList<double[]> backList = new ArrayList<>();
        for (double[] input : inputs) {
            if(input[0] >= 0) //frontlist
            {
                boolean added = false;
                for (int i = 0; i < frontList.size(); i++) {
                    double[] other = frontList.get(i);
                    if(input[0] < other[0] || (input[0] == other[0] && input[1] <= other[1] ))
                    {
                        added = true;
                        frontList.add(i, input);
                        break;
                    }
                }
                if(!added) frontList.add(input);
            }
            else
            {
                boolean added = false;
                for (int i = 0; i < backList.size(); i++) {
                    double[] other = backList.get(i);
                    if(input[0] > other[0]|| (input[0] == other[0] && input[1] <= other[1] ))
                    {
                        added=true;
                        backList.add(i, input);
                        break;
                    }
                }
                if(!added) backList.add(input);
            }
        }
        return new ArrayList[]{frontList, backList};
    }
    
    
    
    private ArrayList<int[]> getAdvancedPlatformObservation(Environment e)
    {
        //TODO : GET THE FIRST PLATFORM X COORD
        byte[][] platforms = e.getLevelSceneObservationZ(1);
        
        ArrayList<int[]> refPlats = new ArrayList<>(); // check the current platforms against the other platforms
        ArrayList<int[]> pList = new ArrayList<>();
        
        for (int i = 0; i < platforms.length; i++) {
            if(i == 0) // init references and add them all
            {
                refPlats = getPlatforms(platforms[i], i);
                for(int[] plat : refPlats )
                    pList.add(plat);
            }
            else // check platforms with the previous ones
            {
                ArrayList<int[]> newRefs = new ArrayList<>();
                for (int[] plat : getPlatforms(platforms[i], i)) {
                    
                    newRefs.add(new int[] {plat[0], plat[1], plat[2], plat[3]});
                    
                    boolean overL = false;
                    
                    for (int[] refPlat : refPlats) {
                        if(plat[0] + plat[2] - 1 < refPlat[0]){ // x + size < start of reference plat
                            break;
                        }
                        else // check if they overlap
                        {
                            int[] overlap = getOverlap(plat, refPlat);
                            if(overlap[1] > 0) // there is overlap
                            {
                                overL = true;

                                //add the remainder of the platform to the platform list
                                if(!(overlap[0] == plat[0] && overlap[1] == plat[2])) // if they do not fully overlap
                                {
                                    if(overlap[0] > plat[0])
                                    {
                                        addPlatForm(pList, plat[0], plat[1], overlap[0] - plat[0], plat[3]);

                                        //TODO : TWO DIFFERENT OVERLAP HANDLERS!!!!!
                                        
                                        if(plat[0] + plat[2] > overlap[0] + overlap[1])// there is still some platform left
                                        {
                                            plat[0] = overlap[0] + overlap[1];
                                            plat[2] = plat[2] - overlap[1] - (overlap[0] - plat[0]);
                                            overL = false;
                                        }
                                        
                                    }
                                    else
                                    {
                                        if(plat[0] + plat[2] > overlap[0] + overlap[1])
                                        {
                                            plat[0] = overlap[0] + overlap[1];
                                            plat[2] = plat[2] - overlap[1];
                                            overL = false;

                                        }
                                        else
                                        {
                                            addPlatForm(pList, plat[0] + overlap[1], plat[1], plat[2] - overlap[1], plat[3]); 
                                        }
                                    }
                                    
                                }
                            }
                        }
                    }
                    if(!overL)
                    {
                        pList.add(plat);
                    }
                }
                refPlats = newRefs;
            }
        }
        
        return pList;
    }
    
    /**
     * Returns the overlap between a platform and a reference platform
     * @param plat
     * @param refPlat
     * @return {x, length}
     */
    private int[] getOverlap(int[] plat, int[] refPlat)
    {
        int[] firstPlat;
        if(plat[0] == refPlat[0])
        {
            firstPlat = plat[2] < refPlat[2] ? plat : refPlat;
        }
        else firstPlat = plat[0] <= refPlat[0] ? plat : refPlat;
        boolean first = Arrays.equals(firstPlat, plat); 
        int[] secondPlat = first ? refPlat: plat;
        int xDif = secondPlat[0] - firstPlat[0];
        int overlap = firstPlat[2] - xDif;
        int start = first ? secondPlat[0] : firstPlat[0];
        return new int[]{start, overlap};
    }
    
    
    private ArrayList<int[]> getPlatforms(byte[] row, int yCoord)
    {
        boolean platHit = false;
        int prevPlatKind = 0;
        int platLength = 0;
        ArrayList<int[]> pList = new ArrayList<>();
        for (int i = 0; i < row.length; i++) {
                if(pKinds.contains((int)row[i])){
                    if(platHit){ // check if it matches previous type
                        if(row[i] == prevPlatKind || (row[i] == 16 && prevPlatKind == 21) || (row[i] == 21 && prevPlatKind == 16)){ // platform continues
                            platLength++;
                        }
                        else // new platform diff kind
                        {
                            addPlatForm(pList, i - platLength, yCoord, platLength, prevPlatKind);
                            platLength = 1;
                            prevPlatKind = row[i];
                        }
                    }
                    else
                    {
                        platHit = true;
                        prevPlatKind = row[i];
                        platLength = 1;
                    }
                }
                else if(platHit) // add a new platform to the list
                {
                    addPlatForm(pList, i - platLength , yCoord, platLength, prevPlatKind);
                    platHit = false;
                }
            }
            if(platHit) // line ended, add platform
            {
                addPlatForm(pList, row.length - platLength, yCoord, platLength, prevPlatKind);
            }   
        return pList;
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
        ArrayList<Integer> platKinds = pKinds;
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
        platList.add(new int[]{x , y, platLength, platType});
    }
    //     
//    private double[] getInputs(int mode, ArrayList<int[]> enemies, ArrayList<int[]> platforms){
//        ArrayList<Double> inputs = new ArrayList<>();
//        double[] inputArray = new double[1 + nrOfEnemies * 3 + nrOfPlatforms * 4];
//        inputs.add((double)mode);
//        for (int i = 0; i < nrOfEnemies; i++) {
//            if(i < enemies.size()){
//                int[] enemy = enemies.get(i);
//                for (int j = 0; j < enemy.length; j++) {
//                    inputs.add((double)enemy[j]);
//                }
//            }
//            else
//                for(int j = 0; j < 3; j ++)
//                    inputs.add((double)0);
//        }
//        for (int i = 0; i < nrOfPlatforms; i++) {
//            if(i < platforms.size()){
//                int[] platform = platforms.get(i);
//                for (int j = 0; j < platform.length; j++) {
//                    inputs.add((double)platform[j]);
//                }
//            }
//            else
//                for(int j = 0; j < 4; j ++)
//                    inputs.add((double)0);
//        }
//        for (int i = 0; i < inputs.size(); i++) {
//            inputArray[i] = inputs.get(i);
//        }
//        return inputArray;
//    }
    
}
