/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jan
 */
public class NeuralNetwork  implements Serializable{
    
    private double[][] firstConnectionLayer;
    private double[][] secondConnectionLayer;
    private double[] hiddenNeurons;
    private double[] outputs;
    private double[] inputs;
    private final double slope_parameter = 1;
    private final double MUTATION_RATE = 0.005;
    private final double MAX_MUTATION = 0.2;
    
    public NeuralNetwork(int numberOfInputs, int numberOfHidden, int numberOfOutputs){
        firstConnectionLayer = new double[numberOfInputs + 1][numberOfHidden]; // add 1 for bias
        secondConnectionLayer = new double[numberOfHidden][numberOfOutputs];
        hiddenNeurons = new double[numberOfHidden];
        outputs = new double[numberOfOutputs];
        //targetOutputs = new double[numberOfOutputs];
        inputs = new double[numberOfInputs];
        initializeLayer(firstConnectionLayer);
        initializeLayer(secondConnectionLayer);
    }
    
    public NeuralNetwork(int numberOfInputs, int numberOfHidden, int numberOfOutputs, boolean empty){
        firstConnectionLayer = new double[numberOfInputs][numberOfHidden]; // add 1 for bias
        secondConnectionLayer = new double[numberOfHidden][numberOfOutputs];
        hiddenNeurons = new double[numberOfHidden];
        outputs = new double[numberOfOutputs];
        //targetOutputs = new double[numberOfOutputs];
        inputs = new double[numberOfInputs];
        if(!empty)
        {
            initializeLayer(firstConnectionLayer);
            initializeLayer(secondConnectionLayer);
        }
    }
    
    
    
    public NeuralNetwork(double[][] firstConnectionLayer, double[][] secondConnectionLayer, int numberOfHidden,
               int numberOfOutputs) {
        this.firstConnectionLayer = firstConnectionLayer;
        this.secondConnectionLayer = secondConnectionLayer;
        inputs = new double[firstConnectionLayer.length];
        hiddenNeurons = new double[numberOfHidden];
        outputs = new double[numberOfOutputs];
    }
    
    
    private void initializeLayer(double[][] connectionLayer){
        for (int i = 0; i < connectionLayer.length; i++) {
            for (int j = 0; j < connectionLayer[i].length; j++) {
                connectionLayer[i][j] = Math.random() * 2 - 1;     // random value between -1 and 1
            }
        }
    }
    
    public double[] propagate(double[] inputIn)
    {
        inputs = new double[inputIn.length + 1];
        inputs[0] = 1;
        System.arraycopy(inputIn, 0, inputs, 1, inputIn.length);
//        if (inputs != inputIn) {
//            System.arraycopy(inputIn, 0, this.inputs, 0, inputIn.length);
//        }
//        if (inputIn.length < inputs.length)
//            System.out.println("NOTE: only " + inputIn.length + " inputs out of " + inputs.length + " are used in the network");
//        
        propagateOneStep(inputs, hiddenNeurons, firstConnectionLayer);
        sigmoid(hiddenNeurons);
        propagateOneStep(hiddenNeurons,  outputs, secondConnectionLayer);
        sigmoid(outputs);
        
        return outputs;
    }
    
    private void sigmoid(double[] values){
        for (int i = 0; i < values.length; i++) {
            values[i] =(double) 1 / (1 + Math.pow(Math.E, - values[i] * slope_parameter));
        }
    }
    
    private void propagateOneStep(double[] fromLayer, double[] toLayer, double[][] connections)
    {
        clear(toLayer);
        for (int from = 0; from < fromLayer.length; from++) {
            for (int to = 0; to < toLayer.length; to++) {
                toLayer[to] += fromLayer[from] * connections[from][to];
                //System.out.println("From : " + from + " to: " + to + " :: " +toLayer[to] + "+=" +  fromLayer[from] + "*"+  connections[from][to]);
            }
        }
    }
    
    private void clear(double[] array) {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }
    
    
    public NeuralNetwork copy() {
        NeuralNetwork copy = new NeuralNetwork(copy(firstConnectionLayer), copy(secondConnectionLayer),
                hiddenNeurons.length, outputs.length);
        return copy;
    }

    private double[][] copy(double[][] original) {
        double[][] copy = new double[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }
    
    
    /**
     * Recombines two neural networks to form a third child network based on 
     * a random selection of building blocks (paths)
     * @param other the other network
     * @return the child
     */
    public NeuralNetwork randomRecombine(NeuralNetwork other)
    {
        // get the paths from the other network and add them to the copy of the current neural network
        NeuralNetwork child = this.copy();
        ArrayList<Path> paths = other.getPaths(Math.random());
        for(Path path : paths){
            child.updatePath(path);
        }
        return child;
    }
   
    /**
     * Recombines two networks using two point recombine, where a middle section
     * of building blocks is selected
     * @param other
     * @return 
     */
    public NeuralNetwork twoPointRecombine(NeuralNetwork other)
    {
        NeuralNetwork child = this.copy();
        ArrayList<Path> paths = other.getPaths(Math.random(), Math.random());
        for(Path path : paths){
            child.updatePath(path);
        }
        return child;
    }
    
    private double getMutationRate(double diff)
    {
        return (1- (diff * 100)) * 0.019 + 0.001; 
    }
    
    public double mutateWeight(double weight, double parent1, double parent2)
    {
        double diff =Math.abs(parent1 - parent2);
        double mutationRate = diff > 0.01 ? 0.001 : getMutationRate(diff);
        double r = Math.random();
        if(r < mutationRate) // mutate
            return (Math.random() * 200) - 100;
        else return weight;
    }
    
    public NeuralNetwork nPointCrossover(NeuralNetwork other, int n)
    {
        NeuralNetwork child = new NeuralNetwork(inputs.length, hiddenNeurons.length, outputs.length, true);
        ArrayList<Double> randomNrList = new ArrayList<>();
        boolean parent = true; // if(parent) select from this NN otherwise select from other
        for (int i = 0; i < n; i++) {
            double r = Math.random();
            boolean hit = false;
            for (int j = 0; j < randomNrList.size(); j++) {
                if(r < randomNrList.get(j)) 
                {
                    randomNrList.add(j,r);
                    hit = true;
                    break;
                }
            }
            if(!hit)
                randomNrList.add(r);
        }
        int randomCount = 0;
        double random = randomNrList.get(0);
        double totalNrOfWeights = inputs.length * hiddenNeurons.length  + hiddenNeurons.length * outputs.length;
        double pathCount = 0;
        for (int j = 0; j < inputs.length; j++) {
            for (int k = 0; k < hiddenNeurons.length; k++) {
                if(!(pathCount / totalNrOfWeights < random || randomCount == randomNrList.size()))
                {
                    parent = !parent;
                    randomCount++;
                    if(randomCount != randomNrList.size())
                    {
                        random = randomNrList.get(randomCount);
                    }
                }
                double weight = parent ? this.getWeight(0, j, k) : other.getWeight(0, j, k);
                weight = mutateWeight(weight, this.getWeight(0, j, k), other.getWeight(0, j, k) );
                child.setWeight(0, j, k, weight);
            }
        }
        for (int j = 0; j < hiddenNeurons.length; j++) {
            for (int k = 0; k < outputs.length; k++) {
                if(!(pathCount / totalNrOfWeights < random || randomCount == randomNrList.size()))
                {
                    parent = !parent;
                    randomCount++;
                    if(randomCount != randomNrList.size())
                    {
                        random = randomNrList.get(randomCount);
                    }
                }
                double weight = parent ? this.getWeight(1, j, k) : other.getWeight(1, j, k);
                weight = mutateWeight(weight, this.getWeight(1, j, k), other.getWeight(1, j, k) );
                child.setWeight(1, j, k, weight);
            }
        }
        
    
        return child;
    }
    
//    public NeuralNetwork uniformCrossover(NeuralNetwork other)
//    {
//        
//    }
    
    
    protected ArrayList<Path> getPaths(double fPerc, double sPerc)
    {
        double first = Math.min(fPerc, sPerc);
        double second = Math.max(fPerc, sPerc);
        ArrayList<Path> paths = new ArrayList<>();
        int totalNrOfPaths = inputs.length * hiddenNeurons.length * outputs.length;
        int firstPath = (int) (fPerc * totalNrOfPaths);
        int secondPath = (int) (sPerc * totalNrOfPaths);
        int pathCount = 0;
        boolean done = false;
        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < hiddenNeurons.length; j++) {
                for (int k = 0; k < outputs.length; k++) {
                    // if the pathCount is between the paths to be selected, select
                    if(pathCount >= firstPath && pathCount < secondPath) // TODO : maybe pathCount <= secondPath
                    {
                        Path path = new Path(i, j, k, firstConnectionLayer[i][j], secondConnectionLayer[j][k]);
                        paths.add(path);
                    }
                    pathCount++;
                }
            }
        }
        
        return paths;
    }
    
    
    // get a certain percentage of paths from input to output
    protected ArrayList<Path> getPaths(double percentage){
        ArrayList<Path> paths = new ArrayList<>();
        int totalNrOfPaths = inputs.length * hiddenNeurons.length * outputs.length;
        int endCount = (int) (totalNrOfPaths * percentage);
        int pathCount = 0;
        int pathsLeft = endCount;
        boolean done = false;
        
        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < hiddenNeurons.length; j++) {
                for (int k = 0; k < outputs.length; k++) {
                    
                    if(pathCount < endCount){ // select a path
                        if(pathsLeft == endCount - pathCount){ // select all remainings paths --> select this path 
                            Path path = new Path(i, j, k, firstConnectionLayer[i][j], secondConnectionLayer[j][k]);
                            paths.add(path);
                            pathsLeft--;
                            pathCount++;
                        }
                        else // go on chance
                        {
                            if(Math.random() < percentage){ // select the path
                                Path path = new Path(i, j, k, firstConnectionLayer[i][j], secondConnectionLayer[j][k]);
                                paths.add(path);
                                pathsLeft--;
                                pathCount++;
                            }
                        }
                    }
                    else {
                        done = true;
                        break;
                    }
                }
                if(done) break;
            }
            if(done) break;
        }
        return paths;
    }
    
    
    /**
     * Mutates the network according to a mutation rate and a max mutation value
     */
    public void mutate(){
        for (int i = 0; i < inputs.length; i++) {
            for (int j = 0; j < hiddenNeurons.length; j++) {
                if(Math.random() < MUTATION_RATE){
                    if(Math.random() < 0.5) // +-
                        firstConnectionLayer[i][j] += Math.random() * MAX_MUTATION;
                    else
                        firstConnectionLayer[i][j] -= Math.random() * MAX_MUTATION;
                }
            }
        }
    }
    
    /**
     * Updates a path from input to output to the selected values
     * @param x input node
     * @param y hidden node
     * @param z output node
     * @param weight1 weight for first layer
     * @param weight2 weight second layer
     */
    protected void updatePath(Path path){
        firstConnectionLayer[path.getX()][path.getY()] = path.getFirstWeight();
        secondConnectionLayer[path.getY()][path.getZ()] = path.getSecondWeight();
        
    }
    
    
    /**
     * Returns the weight of the first or second layer and the vertex
     * @param layer 1 ||2 
     * @param x first coord
     * @param y second coord
     * @return 
     */
    protected double getWeight(int layer, int x, int y){
        return layer == 0 ? firstConnectionLayer[x][y] : secondConnectionLayer[x][y];
    }
    
    
    protected void setWeight(int layer, int x, int y, double weight)
    {
        double[][] connLayer = layer == 0 ? firstConnectionLayer : secondConnectionLayer;
        connLayer[x][y] = weight;
    }
    
    public int getNrOfInputs(){
        return inputs.length;
    }
    public int getNrOfOutputs(){
        return outputs.length;
    }
    public int getNrOfHidden(){
        return hiddenNeurons.length;
    }
    
    
    
}
