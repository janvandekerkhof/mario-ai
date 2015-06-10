/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 *
 * @author Jan
 */
public class StringFormatter {
    
    public static void main(String[] args){
        String input = JOptionPane.showInputDialog(null, "Insert test data below");
        String[] difficulties = input.split("Difficulty");
        for (int i = 1; i < difficulties.length; i++) {
            String[] values = difficulties[i].split("Values : ")[1].split(",");
            ArrayList<Double> vals = new ArrayList<>();
            for (int j  = 1; j < values.length - 1 ; j++) {
                double v = Double.parseDouble(values[j]);

                vals.add(v);
            }
            double avg = 0;
            for (Double d : vals) {
                avg += d;
            }
            avg /= vals.size();
            double dev = 0;
            for(Double d : vals) {
                dev += Math.pow( d - avg, 2);
            }
            dev = Math.sqrt(dev / vals.size());
            System.out.println("Average : " + avg);
            System.out.println("Deviation : " + dev);
        }
        System.out.println("Highest difficulty : " + input.split("Highest difficulty : ")[1]);

    }
    
//    public static void main(String[] args)
//    {
//        String input = JOptionPane.showInputDialog(null, "Insert test data below");
//        String[] replacements = input.split("Level of difficulty  ");
//        HashMap<Integer, Integer> difficulties = new HashMap<>();
//        difficulties.put(0, 400);
//        HashMap<Integer, Double> averages = new HashMap<>();
//        int[] levels = new int[replacements.length -1];
//        int[] generations = new int[replacements.length - 1];
//        for (int i = 1; i < replacements.length; i++) {
//            levels[i - 1] = Integer.parseInt(replacements[i].split(" replaced")[0]);
//            generations[i - 1] = Integer.parseInt(replacements[i].split("generation ")[1].split(" ")[0]);
//        }
//        
//        for (int i = 0; i < levels.length; i++) {
//            int level = levels[i];
//            if(!difficulties.containsKey(level + 1)) 
//            {
//                difficulties.put(level + 1, 1);
//            }
//            else 
//            {
//                int amount = difficulties.get(level + 1);
//                difficulties.remove(level + 1);
//                difficulties.put(level+1, amount+1);
//            }
//            int amount = difficulties.get(level);
//            difficulties.remove(level);
//            difficulties.put(level, amount-1);
//        }
//        System.out.println("The average difficulty at the end is " + calculateAverage(difficulties));
//    }
//    
    private static double calculateAverage(HashMap<Integer, Integer> diff)
    {
        double avg = 0;
        for (int i : diff.keySet()) {
            avg += i * diff.get(i);
        }
        return avg / 400;
    }  
}
    
    

