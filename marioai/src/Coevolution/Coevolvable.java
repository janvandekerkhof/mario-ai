/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import java.util.ArrayList;

/**
 *
 * @author Jan
 */
public abstract class Coevolvable {
    
    private final static int FITNESS_AMOUNT = 20;
    
    private ArrayList<Double> fitness = new ArrayList<>();
    private double avgFitness = 0;
    
    public Coevolvable(){
    }
    
    public void push(double fitness){
        this.fitness.add(fitness);
        if(this.fitness.size() > FITNESS_AMOUNT){
            this.fitness.remove((int)0);
        }
        double fit =0;
        for(double d : this.fitness){
            fit += d;
        }
        avgFitness = fit / this.fitness.size();
    }
    
    public double getAvgFitness(){
        return avgFitness;
    }
    
}
