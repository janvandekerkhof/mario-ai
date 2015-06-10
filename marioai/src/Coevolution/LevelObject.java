/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Coevolution;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.engine.level.SpriteTemplate;
import java.util.ArrayList;

/**
 *
 * @author Jan
 */
public class LevelObject extends Coevolvable {
    
    private Level level;
    private int difficulty;
    
    public LevelObject(Level level){
        super();
        this.level = level;
    }
    
    public LevelObject(Level level, int difficulty)
    {
        super();
        this.level = level;
        this.difficulty = difficulty;
    }
    
    public int getDifficulty()
    {
        return difficulty;
    }
    
    public Level getLevel(){
        return copy(level);
    }
    
    private Level copy(Level level){
        Level newLevel = new Level(level.width, level.height);
        for (int i = 0; i < level.width; i++) {
            for (int j = 0; j < level.height; j++) {
                newLevel.setBlock(i, j, level.getBlock(i, j));
            }
        }
        for (int i = 0; i < level.spriteTemplates.length; i++) {
            for (int j = 0; j < level.spriteTemplates[i].length; j++) {
                SpriteTemplate template = level.getSpriteTemplate(i, j);
                if(template != null)
                newLevel.setSpriteTemplate(i, j, new SpriteTemplate(template.getType(), template.isWinged()));
            }
        }
        
        newLevel.xExit = level.xExit;
        newLevel.yExit = level.yExit;
        return newLevel;
    }
    
}
