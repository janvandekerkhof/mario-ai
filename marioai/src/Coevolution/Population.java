/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import ch.idsia.ai.Evolvable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Random;

/**
 *
 * @author Jan
 */
public class Population {
    
    private ArrayList<Coevolvable> members = new ArrayList<>();
    private ArrayList<Double> chances = new ArrayList<>();
    private double cumChance;
    
    public Population(ArrayList<Coevolvable> members, int moreChance){
        this.members = members;
        initChances(moreChance);
    }
    
    private void initChances(int moreChance){
        double extra = moreChance - 1;
        for(int i = 0; i < members.size(); i++){
            double chance = 1 + i * (extra / (members.size() - 1));
            cumChance += chance;
            chances.add(0, cumChance);
        }
    }
    
    // returns all the members in the population that fall below the fitness value
    public ArrayList<Coevolvable> getBelowFitness(double fitness)
    {
        ArrayList<Coevolvable> returnMembers = new ArrayList<>();
        for(Coevolvable member : members)
        {
            if(member.getAvgFitness() <= fitness)
            {
                returnMembers.add(member);
            }
        }
        return returnMembers;
    }
    
    public double getAvgFitness(){
        double fitness = 0;
        for (Coevolvable member : members) {
            fitness += member.getAvgFitness();
        }
        return fitness / members.size();
    }
    
    public double getBestFitness(){
        return members.size() > 0 ? members.get(0).getAvgFitness() : 0;
    }
    
    public void sort(){
        ArrayList<Coevolvable> sortedList = new ArrayList<>();
        while(!members.isEmpty()) {
            double maxVal = Integer.MIN_VALUE;
            
            
            int pos = -1;
            for (int j = 0; j < members.size(); j++) {
                double fitness = members.get(j).getAvgFitness();
                if(fitness > maxVal){
                    maxVal =fitness;
                    pos = j;
                }
            }
            sortedList.add(members.remove(pos));
        }
        members = sortedList;
    }
    
    public Coevolvable select(){
        double chance = Math.random() * cumChance;
        int pos = chances.size() - 1; // default lowest
        for (int i = 0; i < chances.size() - 1; i++) {
            if( (chance <= chances.get(i) && chance > chances.get(i + 1))) //select the current chance
               pos = i; 
        }
        return members.get(pos);
    }
    
    public ArrayList<Coevolvable> selectMany(int amount){
        ArrayList<Coevolvable> list = new ArrayList<>();
        while(list.size() < amount){
            Coevolvable selection = select();
            if(!list.contains(selection))
            {
                list.add(selection);
            }
        }
        return list;
    }
    
    public void update(Coevolvable member){
        members.remove(member);
        for (int i = 0; i < members.size(); i++) {
            if(member.getAvgFitness() > members.get(i).getAvgFitness())
            {
                members.add(i, member);
                break;
            }
            if(i == members.size() - 1){ // add at the last
                members.add(member);
                break;
            }
        }
    }
    
    public void push(Coevolvable member){
        for (int i = 0; i < members.size(); i++) {
            if(member.getAvgFitness() > members.get(i).getAvgFitness())
            {
                members.add(i, member);
                break;
            }
        }
        members.remove(members.size() -1 );
    }
    
    public double getLowestFitness(){
        return members.get(members.size() -1 ).getAvgFitness();
    }
    
    public ArrayList<Coevolvable> getMembers(){
        return members;
    }
    
    public ArrayList<Coevolvable> getRandomSet(int amount){ 
        ArrayList<Coevolvable> list = new ArrayList<>();
        while(list.size() < amount){
            int rand =(int)( Math.random() * members.size());
            list.add(members.get(rand));
        }
        return list;
    }
    
    public ArrayList<Coevolvable> getRandomSetNoDouble(int amount){ 
        ArrayList<Coevolvable> list = new ArrayList<>();
        while(list.size() < amount){
            int rand =(int)( Math.random() * members.size());
            Coevolvable member = members.get(rand); 
            if(!list.contains(member))
                list.add(member);
        }
        return list;
    }
    
    
    
    
    
    
}
