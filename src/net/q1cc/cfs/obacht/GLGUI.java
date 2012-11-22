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
import java.nio.FloatBuffer;
import net.q1cc.cfs.font.FontLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;


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
    double nameGrowTime = 0.7f;
    double targetFPS = 60;
    double timeSpeed = 1.0;
    double startTime;
    int windowLeftBorder    = 10;
    int windowTopBorder     = 10;
    int windowRightBorder   = 80;
    int windowBotBorder     = 10;
    int hudLeftBorder       = 10;
    int hudTopBorder        = 10;
    int hudBotBorder        = 10;
    int hudRightBorder      = 10;
    int windowX;
    int windowY;
    DisplayMode dm;
    private boolean DEBUG=false;
    
    FontLoader font;
    
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
        windowRightBorder = windowX - fieldSize-windowLeftBorder;
        windowBotBorder = windowY - fieldSize-windowTopBorder;
        Display.setDisplayMode(dm);
        System.out.println("displaymode: "+dm);
        Display.setSwapInterval(1);
        Display.setVSyncEnabled(true);
        Display.create();
        Display.makeCurrent();
        glViewport(0,0,windowX,windowY);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_ALWAYS);
        
        loadStuff();
        
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
            if(frameCounter>40) {
                fps = (frameCounter/((float)(time-lastFPSTime))) *0.8+fps*0.2;
                frameCounter=0; lastFPSTime=time;
                //Display.setTitle("Obacht!");// fps: "+((int)(fps*100)/100.0f)+" time: "+(int)((time-startTime)*1000)/1000.0 + " delta: "+deltaTime);
            }
            double waitTime = (double)(1.0/targetFPS)-(time-lastFrameTime);
            if(waitTime>0) { //only sleep if we would sleep more than 10 msecs
                try {
                    Thread.sleep((int)(waitTime*1000));//30 fps
                } catch (InterruptedException ex) {}
                timeToWait=timeToWait*0.9f+(waitTime)*0.1f;
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
        glClearColor(0.5f,0.5f,0.5f,1.0f);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        
        //draw field
        
        glViewport(windowLeftBorder, windowBotBorder, fieldSize, fieldSize);
        drawField();
        glViewport(0,0,windowX,windowY);
        drawBorder();
        //gui!
        drawHUD();
        Display.update();
        checkError();
    }
    private void drawBorder() {
        //draw border
        glPushMatrix();
        glLoadIdentity();
        //glTranslatef(-1,-0.5f,0);
        //glScalef(2f/windowX, 2f/windowY,1);
        FloatBuffer pts = BufferUtils.createFloatBuffer(15);
        glViewport(windowLeftBorder-1, windowBotBorder-1, fieldSize+1, fieldSize+1);
        //pts.put(windowLeftBorder-1).put(windowTopBorder-1).put(1f)
        //.put(windowLeftBorder+fieldSize).put(windowTopBorder-1).put(1f)
        //.put(windowLeftBorder+fieldSize).put(windowTopBorder+fieldSize).put(1f)
        //.put(windowLeftBorder-1).put(windowTopBorder+fieldSize).put(1f)
        //.put(windowLeftBorder-1).put(windowTopBorder-1).put(1f);
        float edge = 1-1.0f/(fieldSize+1);
        pts .put(-1).put(edge).put(1f)
            .put(edge).put(edge).put(1f)
            .put(edge).put(-1).put(1f)
            .put(-1).put(-1).put(1f)
            .put(-1).put(edge).put(1f);
        pts.flip();
        glColor4f(1,1,1,1);
        glVertexPointer(3,0,pts);
        glDrawArrays(GL_LINE_STRIP, 0, 5);
        glPopMatrix();
    }
    
    private void drawField() {
        
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,game.fieldColorTexture);
        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(1,1,1,1);
        glTexCoord2f(0,1); glVertex2f(-1,1);
        glTexCoord2f(0,0); glVertex2f(-1,-1);
        glTexCoord2f(1,1); glVertex2f(1,1);
        glTexCoord2f(1,0); glVertex2f(1,-1);
        glEnd();
        glDisable(GL_TEXTURE_2D);
        for(Player p: game.players){
            glColor4ub((byte)p.color.getRed(),(byte)p.color.getGreen(),(byte)p.color.getBlue(),(byte)255); //TODO make the head blink?
            glPushMatrix();
            glTranslatef(p.pos.x,p.pos.y,0);
            glBegin(GL_TRIANGLE_FAN);
            glVertex2f(0,0);
            for(int i=0;i<17;i++) {
                float x = (float)Math.cos(2*Math.PI*i/16.0f)*p.width*1.5f;
                float y = (float)Math.sin(2*Math.PI*i/16.0f)*p.width*1.5f;
                glVertex2f(x,y);
            }
            glEnd();
            glPopMatrix();
        }
        
    }
    private void drawHUD() {
        float fontSize = 35.0f;
        int hudXSize = windowRightBorder-hudLeftBorder-hudRightBorder;
        int hudYSize = windowY-windowTopBorder-windowBotBorder-hudTopBorder-hudBotBorder;
        glViewport(windowLeftBorder+fieldSize+hudLeftBorder, windowBotBorder+hudBotBorder, hudXSize, hudYSize);
        Matrix4f mat = new Matrix4f();
        mat = mat.translate(new Vector3f(-1.0f,1.0f,0.0f));
        //mat = mat.scale(new Vector3f(1.0f/windowX, 1.0f/windowY, 1.0f));
        mat = mat.scale(new Vector3f(1.0f/hudXSize, 1.0f/hudYSize, 1.0f));
        font.drawString("Player Scores:", mat,fontSize);
        mat = mat.translate(new Vector3f(0,-fontSize*2,0));
        
        for(Player p:game.players) {
            glColor3ub((byte)p.color.getRed(), (byte)p.color.getGreen(), (byte)p.color.getBlue());
            float fontTmp = fontSize;
            if (p.lastScoreTime + nameGrowTime > time) {
                fontTmp += (Math.sin(Math.PI*(time - p.lastScoreTime)/(nameGrowTime/2) - 0.5*Math.PI)+1)*fontSize*0.7f;
            }
            font.drawString(p.name+": "+p.score, mat,fontTmp);
            mat = mat.translate(new Vector3f(0,-fontSize*1.3f,0));
        }
        glColor3f(1, 1, 1);
        mat = mat.translate(new Vector3f(0,-fontSize,0));
        
        String status;
        if(!game.pause) status="pause";
        else if(game.waitingForNewRound) status="new game";
        else status="unpause";
        font.drawString("SPACE = "+status, mat, fontSize);
        mat = mat.translate(new Vector3f(0,-fontSize*2,0));
        
        glColor3f(0.15f, 0.15f, 0.15f);
        fontSize=20.0f;
        font.drawString("fps: "+Math.floor(fps*100)/100, mat, fontSize);
        mat = mat.translate(new Vector3f(0,-fontSize*1.3f,0));
        font.drawString("frameWait: "+Math.floor(timeToWait*1000000)/100, mat, fontSize);
        mat = mat.translate(new Vector3f(0,-fontSize*1.3f,0));
        font.drawString("time: "+Math.floor((time-startTime)*100)/100, mat, fontSize);
        mat = mat.translate(new Vector3f(0,-fontSize*1.3f,0));
        font.drawString("delta: "+Math.floor((deltaTime)*100000)/100, mat, fontSize);
        
        glViewport(0,0,windowX,windowY);
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

    private void loadStuff() {
        font = new FontLoader();
        font.loadFont();
    }
    
    private void drawTexture(int id) {
        glPushMatrix();
        glLoadIdentity();
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,id);
        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(1,1,1,1);
        glTexCoord2f(0,0); glVertex2f(-1,1);
        glTexCoord2f(0,1); glVertex2f(-1,-1);
        glTexCoord2f(1,0); glVertex2f(1,1);
        glTexCoord2f(1,1); glVertex2f(1,-1);
        glEnd();
        glDisable(GL_TEXTURE_2D);
        glPopMatrix();
    }
}
