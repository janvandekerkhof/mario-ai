/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

/**
 *
 * @author Jan
 */
public class Path {
    private int x, y, z;
    private double firstWeight, secondWeight;
    public Path(int x, int y, int z, double firstWeight, double secondWeight){
        this.x = x;
        this.y = y;
        this.z = z;
        this.firstWeight = firstWeight;
        this.secondWeight = secondWeight;
    }
    
    
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public int getZ(){
        return z;
    }
    public double getFirstWeight(){
        return firstWeight;
    }
    public double getSecondWeight(){
        return secondWeight;
    }
}
