/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.obacht;

import org.lwjgl.util.glu.GLU;
import java.nio.ByteBuffer;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.awt.image.BufferedImage;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.DisplayMode;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;


/**
 *
 * @author claus
 */
class GLGUI {
    Game game;
    boolean running = true;
    double deltaTime = 1.0f/30;
    double time;
    double lastFrameTime;
    int frameCounter;
    double lastFPSTime;
    int fieldSize;
    double fps;
    double timeToWait;
    double targetFPS = 60;
    double timeSpeed = 1.0;
    double startTime;
    int windowLeftBorder    = 10;
    int windowTopBorder     = 30;
    int windowRightBorder   = 160;
    int windowBotBorder     = 10;
    int hudLeftBorder       = 10;
    int hudTopBorder        = 10;
    int hudBotBorder        = 10;
    int hudRightBorder      = 10;
    int windowX;
    int windowY;
    DisplayMode dm;
    private boolean DEBUG=true;
    
    public GLGUI(Game game) throws LWJGLException {
        this.game=game;
        fieldSize = game.fieldSize;
        Display.setTitle("Obacht!");
        windowX = fieldSize+windowLeftBorder+windowRightBorder;
        windowY = fieldSize+windowTopBorder+windowBotBorder;
        
        DisplayMode[] dms = Display.getAvailableDisplayModes();
        for(DisplayMode d : dms){
            if (dm==null){dm = d;}
            if (dm.getWidth() < windowX && d.getWidth()>dm.getWidth())
                dm=d;
            if (dm.getHeight() < windowY && d.getHeight()>dm.getHeight())
                dm=d;
            if (dm.getWidth()==d.getWidth() && dm.getHeight()==d.getHeight() && d.getFrequency()>dm.getFrequency())
                dm=d;
        }
        windowX = dm.getWidth(); windowY = dm.getHeight();
        Display.setDisplayMode(dm);
        System.out.println("displaymode: "+dm);
        Display.setSwapInterval(1);
        Display.setVSyncEnabled(true);
        Display.create();
        Display.makeCurrent();
        glViewport(0,0,windowX,windowY);
        startTime = lastFPSTime = lastFrameTime = time = Sys.getTime()/(double)Sys.getTimerResolution();
        
        //some debug info
        System.out.println("Sys Timer value: "+Sys.getTime());
        System.out.println("Sys Timer resolution: "+Sys.getTimerResolution());
    }
    
    public void run() {
        game.init();
        while(running){
            doInput();
            game.inputLogic(deltaTime);
            game.gameLogic(deltaTime,time);
            draw();
            time = Sys.getTime()/(double)Sys.getTimerResolution();
            frameCounter++;
            if(frameCounter>10) {
                fps = (frameCounter/((float)(time-lastFPSTime))) *0.8+fps*0.2;
                frameCounter=0; lastFPSTime=time;
                Display.setTitle("Obacht! fps: "+((int)(fps*100)/100.0f)+" time: "+(int)((time-startTime)*1000)/1000.0 + " delta: "+deltaTime);
            }
            double waitTime = (double)(1.0/targetFPS)-(time-lastFrameTime);
            if(waitTime>0) { //only sleep if we would sleep more than 10 msecs
                try {
                    Thread.sleep((int)(waitTime*1000));//30 fps
                } catch (InterruptedException ex) {}
                timeToWait=timeToWait*0.8f+(waitTime)*0.2f;
            }
            time = Sys.getTime()/(double)Sys.getTimerResolution();
            deltaTime = (time-lastFrameTime)*timeSpeed;
            if(DEBUG) {
                deltaTime = 0.016f;
            }
            lastFrameTime = time;
            
            if(Display.isCloseRequested()) running=false;
        }
        Display.destroy();
    }
        
    private void draw() {
        // get Graphics
        glViewport(0,0,windowX,windowY);
        glClearColor(0.3f,0.3f,0.3f,1.0f);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        
        //gui!
        drawHUD();
        //draw field
        drawBorder();
        
        glViewport(windowLeftBorder, windowTopBorder, fieldSize, fieldSize);
        drawField();
        glViewport(0,0,windowX,windowY);
        checkError();
        Display.update();
        checkError();
    }
    private void drawBorder() {
        //draw border
        //g.setColor(Color.WHITE);
        int[] xpts = new int[4]; int[] ypts = new int[4];
        xpts[0]=windowLeftBorder-1; ypts[0]=windowTopBorder-1;
        xpts[1]=windowLeftBorder+fieldSize; ypts[1]=windowTopBorder-1;
        xpts[2]=windowLeftBorder+fieldSize; ypts[2]=windowTopBorder+fieldSize;
        xpts[3]=windowLeftBorder-1; ypts[3]=windowTopBorder+fieldSize;
        //g.drawPolygon(xpts,ypts,4);
    }
    
    private void drawField() {
        
        //TODO
        glEnable(GL_TEXTURE_2D);
        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(1,1,1,1);
        glTexCoord2f(0,1); glVertex2f(-1,1);
        glTexCoord2f(0,0); glVertex2f(-1,-1);
        glTexCoord2f(1,1); glVertex2f(1,1);
        glTexCoord2f(1,0); glVertex2f(1,-1);
        glEnd();
        //TODO players
//        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
//        for(Player p: game.players){
//            g.setColor(new Color(255-p.color.getRed(),255-p.color.getGreen(),255-p.color.getBlue()));
//            g.fillOval((int)(p.pos.x*fieldSize-p.width*1.5f*fieldSize), (int)(p.pos.y*fieldSize-p.width*1.5f*fieldSize), (int)(p.width*3*fieldSize), (int)(p.width*3*fieldSize));
//        }
    }
    private void drawHUD() {
        //TODO all this crap
//        int xoff = windowLeftBorder+fieldSize+hudLeftBorder;
//        int yoff = windowTopBorder+hudTopBorder;
//        g.setClip(xoff, yoff, (windowX-hudRightBorder)-xoff, (windowX-hudBotBorder)-yoff);
//        //g.setTransform(AffineTransform.getTranslateInstance(xoff, yoff));
//        Font font = new Font("Verdana",Font.PLAIN,20);
//        g.setFont(font);
//        g.setColor(Color.WHITE);
//        for(Player p:game.players) {
//            g.setColor(p.color);
//            float fontsize = 15.0f;
//            if(p.lastScoreTime+700>time) {
//                fontsize += (0.7f-(time-p.lastScoreTime)/1000.0f)*20.0f;
//            }
//            font=font.deriveFont(fontsize);
//            g.setFont(font);
//            yoff += font.getSize();
//            g.drawString(p.name+": "+p.score, xoff, yoff);
//        }
//        yoff += font.getSize()*2;
//        font=font.deriveFont(15.0f);
//        g.setFont(font);
//        g.setColor(Color.WHITE);
//        if(!game.pause) g.drawString("SPACE = pause", xoff, yoff);
//        else if(game.waitingForNewRound) g.drawString("SPACE = restart", xoff, yoff);
//        else g.drawString("SPACE = unpause", xoff, yoff);
//        yoff += font.getSize()*2;
//        
//        g.setColor(Color.LIGHT_GRAY);
//        font=font.deriveFont(9.0f);
//        g.setFont(font);
//        g.drawString("fps: "+Math.floor(fps*100)/100, xoff, yoff);
//        yoff += font.getSize()*2;
//        g.drawString("delta: "+Math.floor(deltaTime*1000)/1000, xoff, yoff);
//        yoff += font.getSize()*2;
//        g.drawString("frameWait: "+Math.floor(timeToWait*100)/100, xoff, yoff);
//        yoff += font.getSize()*2;
//        g.drawString("waitFactor: "+Math.floor(waitFactor*100)/100, xoff, yoff);
//        g.setClip(0,0,windowX,windowY);
    }

    private void doInput() {
        while(Keyboard.next()) {
            int key = Keyboard.getEventKey();
            boolean keyDown = Keyboard.getEventKeyState();
            
            if(keyDown)
                switch (key) {
                case Keyboard.KEY_SPACE:
                    game.pause=!game.pause;
                    break;
                case Keyboard.KEY_ESCAPE:
                    //TODO display menu
                    game.pause=true;
                    break;
            }
            
            if(keyDown)
                game.keyPressed(key);
            else
                game.keyReleased(key);
        }
        
    }
    
    protected static void checkError() {
        int errorValue = glGetError();

        if (errorValue != GL_NO_ERROR) {
            String errorString = GLU.gluErrorString(errorValue);
            System.err.println("gl error: - " + ": " + errorString);

            //if (Display.isCreated()) {
            //    Display.destroy();
            //}
            //System.exit(-1);
        }
    }
}
