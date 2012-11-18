/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.obacht;

import java.awt.Color;
import java.awt.event.KeyEvent;
import org.lwjgl.input.Keyboard;

/**
 *
 * @author claus
 */
class Player {
    
    static int lastAlivePoints = 3;
    static int collidePoints = -1;
    
    Vec2 pos;
    float speed;
    float angle;
    float lastAngle;
    float turnSpeed;
    float width;
    double lastScoreTime;
    Color color;
    int rgbColor;
    String name;
    int num;
    boolean alive;
    int score=0;
    
    int keyLeft;
    int keyRight;
    
    boolean leftDown;
    boolean rightDown;
    
    public Player(int num) {
        //spawn();
        this.num=num;
        switch(num){
            case 0:
                color = Color.RED;
                keyLeft = Keyboard.KEY_LEFT;
                keyRight = Keyboard.KEY_RIGHT;
                name = "Red";
                break;
            case 1:
                color = Color.GREEN;
                keyLeft = Keyboard.KEY_A;
                keyRight = Keyboard.KEY_D;
                name = "Green";
                break;
            case 2:
                color = Color.BLUE;
                keyLeft = Keyboard.KEY_J;
                keyRight = Keyboard.KEY_L;
                name = "Blue";
                break;
            case 3:
                color = Color.YELLOW;
                keyLeft = Keyboard.KEY_NUMPAD4;
                keyRight = Keyboard.KEY_NUMPAD6;
                name = "Yellow";
                break;
        }
        rgbColor = color.getRGB();
    }

    void spawn() {
        pos = new Vec2();
        pos.x = Main.r.nextFloat()*0.6f+0.2f;
        pos.y = Main.r.nextFloat()*0.6f+0.2f;
        speed = 0.2f;
        turnSpeed = 1.2f;
        angle = Main.r.nextFloat()*2*(float)Math.PI;
        lastAngle = angle;
        width = 0.008f;
        alive = true;
    }

    void die() {
        speed = 0;
        turnSpeed = 0;
        alive=false;
        score += collidePoints;
    }
    
    void lastAlive(double time) {
        score += lastAlivePoints;
        lastScoreTime=time;
    }
    
    @Override public String toString() {
        return name+"@"+pos;
        
    }
    
}
