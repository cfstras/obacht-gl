package net.q1cc.cfs.obacht;

import java.awt.Color;
import java.awt.event.KeyEvent;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;

/**
 *
 * @author claus
 */
class Player {
    
    Vec2 pos;
    Vec2 lastPos;
    float angle;
    float lastAngle;
    float lastLastAngle;
    float speed;
    float turnSpeed;
    float width;
    double lastScoreTime;
    Color color;
    int rgbColor;
    String name;
    int num;
    boolean alive;
    double deathTime;
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
        lastPos = pos;
        speed = 0.2f;
        turnSpeed = 1.2f;
        angle = Main.r.nextFloat()*2*(float)Math.PI;
        lastAngle = angle;
        lastLastAngle = angle;
        width = 0.008f;
        alive = true;
        deathTime = 0;
    }

    void die() {
        deathTime = Time.time;
        speed = 0;
        turnSpeed = 0;
        alive = false;
        score += Settings.collidePoints;
    }
    
    void lastAlive() {
        score += Settings.playerLastAlivePoints;
        lastScoreTime=Time.time;
    }
    
    void drawHead() {
        glPushMatrix();
        if(!alive){
            float[] hsb = new float[3];
            Color.RGBtoHSB(color.getRed(),color.getGreen(),color.getBlue(), hsb);
            hsb[1] = (float) (Settings.playerDeathFadeTime-(Time.time-deathTime));
            if(hsb[1]<0) hsb[1]=0;
            hsb[2] = 0.5f + 0.5f*hsb[1];
            Color c = new Color(Color.HSBtoRGB(hsb[0],hsb[1],hsb[2]));
            glColor4ub((byte) (c.getRed()),
                (byte) (c.getGreen()),
                (byte) (c.getBlue()),
                (byte) 255);
        } else {
            glColor4ub((byte) color.getRed(),
                (byte) color.getGreen(),
                (byte) color.getBlue(),
                (byte) 255);
        }
        glTranslatef(pos.x, pos.y, 0);
        glBegin(GL_TRIANGLE_FAN);
        glVertex2f(0, 0);
        for (int i = 0; i < 17; i++) {
            float x = (float) Math.cos(2 * Math.PI * i / 16.0f) * width * 1.5f;
            float y = (float) Math.sin(2 * Math.PI * i / 16.0f) * width * 1.5f;
            glVertex2f(x, y);
        }
        glEnd();
        glPopMatrix();
    }
    
    @Override public String toString() {
        return name+"@"+pos;
        
    }
    
}
