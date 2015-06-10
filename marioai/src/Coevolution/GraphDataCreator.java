/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import javax.swing.JOptionPane;

/**
 *
 * @author Jan
 */
public class GraphDataCreator {
    
    public static void main(String[] args)
    {
        String input = JOptionPane.showInputDialog(null, "Insert test data below");
        String output = "";
        String[] benchmarks = input.split("AGENT BENCHMARKS : ");
        String[][] bms = new String[benchmarks.length - 1][];
        for (int i = 0; i < bms.length; i++) {
            bms[i] = benchmarks[i + 1].substring(1).split("}")[0].split(",");
        }
        for (int i = 0; i < bms[0].length; i++) {
            if(i % 4 == 0)
            {
                output += 100 * i + " ";
                for (int j = 0; j < bms.length; j++) {
                    output += bms[j][i] + " ";
                }
                output += "\n";
            }
        }
        System.out.println(output);
    }
}