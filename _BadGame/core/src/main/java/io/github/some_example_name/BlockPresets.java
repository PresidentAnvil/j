package io.github.some_example_name;
import java.awt.Color;

public class BlockPresets{
    private static Color newColor(){
        Color[] colors = {
            new Color(255,0,0),
            new Color(0,255,0),
            new Color(255,0,255),
            new Color(0,0,255),
        };
        int r = (int)(Math.random() * colors.length);
        return colors[r];
    }

    public static void Line(int size){
        Color rc = newColor();
        Main.newBlock(Main.WIDTH/2, 400, size, rc);
        Main.newBlock(Main.WIDTH/2+(size*2), 400, size, rc);
        Main.newBlock(Main.WIDTH/2+(size*4), 400, size, rc);
        Main.newBlock(Main.WIDTH/2+(size*2), 400+(size*2), size, rc);
    }
}