/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.obacht;

import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.lwjgl.BufferUtils;
import static java.lang.Math.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

/**
 *
 * @author claus
 */
class Game {
    final static int collisionAntiAA = 2; 
    
    boolean pause = false;
    int fieldFrameBuffer;
    int fieldColorTexture;
    int fieldRenderBuffer;
    
    ArrayList<Player> players;
    int fieldSize;
    Color gameBackground = Color.BLACK;
    boolean waitingForNewRound=false;
    int[] colorTempArray;
    
    public Game() {
        fieldSize = Main.GAME_FIELD_SIZE;
        colorTempArray=new int[512];
    }

    void init() {
        players = new ArrayList<Player>(Main.NUM_PLAYERS);
        for (int i = 0; i < Main.NUM_PLAYERS; i++) {
            Player p = new Player(i);
            players.add(p);
        }
        newRound();
    }
    
    double deltaTime;

    void gameLogic(double deltaTime, double time) {
        this.deltaTime = deltaTime;
        if(pause) {
            return;
        }
        if(waitingForNewRound) {
            newRound();
        }
        
        glBindTexture(GL_TEXTURE_2D,0);
        glBindFramebuffer(GL_FRAMEBUFFER,fieldFrameBuffer);
        glViewport(0,0,fieldSize,fieldSize);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS); //set z-buffer to discard anything wich has already been drawn
        //glClearColor(0.0f,0.0f,0.0f,1.0f);
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        int playersAlive=0;
        Player alivePlayer=null;
        for (Player p : players) {
            if(!p.alive) continue;
            playersAlive++;
            alivePlayer = p;
            glColor3ub((byte)p.color.getRed(), (byte)p.color.getGreen(), (byte)p.color.getBlue());
            //glColor3f(1.0f,1.0f,1.0f);
            Vec2 pos = p.pos;
            Vec2 newpos = new Vec2();
            Vec2 direction = new Vec2();
            direction.x = pos.x + (float) (Math.sin(p.angle) * p.speed * deltaTime);
            direction.y = pos.y - (float) (Math.cos(p.angle) * p.speed * deltaTime);
            newpos.add(direction);
            Vec2 cpos = new Vec2(newpos.x, newpos.y);
            cpos.wrap();
            
            DoubleBuffer verts = BufferUtils.createDoubleBuffer(4*3);
            float anglet = p.angle + 0.5f * (float) Math.PI;
            float langlet = p.lastAngle + 0.5f * (float) Math.PI;
            float width = p.width;
            verts.put((newpos.x + sin(anglet) * width));//*2-1);
            verts.put((newpos.y - cos(anglet) * width));//*2-1);
            verts.put(0.5);
            
            verts.put((newpos.x - sin(anglet) * width));//*2-1);
            verts.put((newpos.y + cos(anglet) * width));//*2-1);
            verts.put(0.5);
            //put pos back a little
            pos.minus(Vec2.minus(newpos,pos).mult(0.5f));
            verts.put((pos.x - sin(langlet) * width));//*2-1);
            verts.put((pos.y + cos(langlet) * width));//*2-1);
            verts.put(0.5);

            verts.put((pos.x + sin(langlet) * width));//*2-1);
            verts.put((pos.y - cos(langlet) * width));//*2-1);
            verts.put(0.5);
            verts.flip();
            
            newpos.wrap();//field wrap
            p.pos = newpos;
            
            //collision detection
            int occQ = glGenQueries();
            glBeginQuery(GL_SAMPLES_PASSED, occQ);
            
            glDepthFunc(GL_GREATER);
            glEnable(GL_RASTERIZER_DISCARD);
            glEnableClientState(GL_VERTEX_ARRAY);
            glVertexPointer(3,0,verts);
            GLGUI.checkError();
            glDrawArrays(GL_QUADS, 0, 4);
            GLGUI.checkError();
            
            glEndQuery(GL_SAMPLES_PASSED);
            //now draw again
            glDepthFunc(GL_ALWAYS);
            glDisable(GL_RASTERIZER_DISCARD);
            verts.put(2,1).put(5,1).put(8,1).put(11,1);
            glVertexPointer(3,0,verts);
            
            glDrawArrays(GL_QUADS, 0, 4);
            
            int passed = glGetQueryObjectui(occQ,GL_QUERY_RESULT);
            if(passed>0){
                System.out.println("passed: "+passed+" "+p.name);
            }
            glDeleteQueries(occQ);
        }
        
        glDisable(GL_DEPTH_TEST);
        glBindFramebuffer(GL_FRAMEBUFFER,0);
        glBindTexture(GL_TEXTURE_2D,fieldColorTexture);
        
        if(playersAlive<=1 && false) {
            //start new round
            if(alivePlayer!=null) {
                alivePlayer.score++;
                alivePlayer.lastScoreTime=time;
            }
            pause=true;
            waitingForNewRound=true;
        }
    }

    void inputLogic(double deltaTime) {

        for (Player p : players) {
            p.lastAngle = p.angle;
            if (p.leftDown) {
                p.angle += deltaTime * p.turnSpeed * (float) Math.PI;
            }
            if (p.rightDown) {
                p.angle -= deltaTime * p.turnSpeed * (float) Math.PI;
            }
        }
    }

    public void keyPressed(int key) {
        for (Player p : players) {
            if (key == p.keyLeft) {
                p.leftDown = true;
            }
            if (key == p.keyRight) {
                p.rightDown = true;
            }
        }
    }

    public void keyReleased(int key) {        
        for (Player p : players) {
            if (key == p.keyLeft) {
                p.leftDown = false;
            }
            if (key == p.keyRight) {
                p.rightDown = false;
            }
        }
    }

    private void newRound() {
        fieldSize = Main.GAME_FIELD_SIZE;
        
        //TODO delete old fb
        
        // http://www.flashbang.se/archives/48
        fieldFrameBuffer = glGenFramebuffers();
        fieldColorTexture = glGenTextures();
        fieldRenderBuffer = glGenRenderbuffers();
        glEnable(GL_TEXTURE_2D);
        glBindFramebuffer(GL_FRAMEBUFFER,fieldFrameBuffer);
        glBindTexture(GL_TEXTURE_2D,fieldColorTexture);
        glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, fieldSize, fieldSize, 0,GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,fieldColorTexture,0);
        
        // create a render buffer as our depth buffer and attach it
        glBindRenderbuffer(GL_RENDERBUFFER, fieldRenderBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER,GL_DEPTH_COMPONENT, fieldSize, fieldSize);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_RENDERBUFFER, fieldRenderBuffer);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if(status != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Error creating framebuffer. "+status);
        }
        
        glViewport(0,0,fieldSize,fieldSize);
        glClearColor(0.0f,0.0f,0.0f,0.0f);
        glClearDepth(1.0);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        
        drawEdgeDebug();
        
        // Go back to regular frame buffer rendering
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        //debug
        //glDrawBuffer(GL_FRONT_AND_BACK);
        
        for(Player p : players) {
            p.spawn();
        }
        waitingForNewRound=false;
    }

    private void drawEdgeDebug() {
        glBegin(GL_POINTS);
        glColor4f(1.0f,0.0f,0.0f,1);
        glVertex2f(-0.9f,0.9f); //top left=red
        
        glColor4f(0.0f,0.0f,1.0f,1);
        glVertex2f(0.9f,0.9f); //top right=blue
        
        glColor4f(0.0f,1.0f,0.0f,1);
        glVertex2f(-0.9f,-0.9f); //bot left=green
        
        glColor4f(1.0f,1.0f,0.0f,1);
        glVertex2f(0.9f,-0.9f); //bot right=yellow
        glEnd();
    }
}
