/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.obacht;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.*;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import net.q1cc.cfs.font.FontLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;
import static org.lwjgl.opengl.GL30.*;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;


/**
 *
 * @author claus
 */
class GLGUI implements ComponentListener, WindowFocusListener, WindowListener {
    Game game;
    boolean running = true;

    int frameCounter;
    double lastFPSTime;
    
    int fieldSize;
	int multiSampling;

    int windowLeftBorder    = 10;
    int windowTopBorder     = 10;
    int windowRightBorder   = 200;
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
    Frame frame;
    Canvas canvas;
    private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<Dimension>();
    ArrayList<Pair<Player,Matrix4f>> drawLaterList;

    public GLGUI(Game game) throws LWJGLException {
        this.game = game;
        fieldSize = game.fieldSize;
        
        frame = new Frame("Obacht!");
        frame.setLayout(new BorderLayout());
        canvas = new Canvas();
        canvas.addComponentListener(this);
        frame.addWindowFocusListener(this);
        frame.addWindowListener(this);
        frame.add(canvas, BorderLayout.CENTER);
        
        Display.setTitle("Obacht!");
        windowX = fieldSize+windowLeftBorder+windowRightBorder;
        windowY = fieldSize+windowTopBorder+windowBotBorder;

        Display.setSwapInterval(1);
        Display.setParent(canvas);
        Display.setVSyncEnabled(true);
        canvas.setPreferredSize(new Dimension(windowX, windowY));
        frame.setMinimumSize(new Dimension(windowX, windowY));
        frame.pack();
        frame.setVisible(true);
        
        Display.create();
        Display.makeCurrent();
        glViewport(0,0,windowX,windowY);
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        
        loadStuff();
        drawLaterList = new ArrayList<Pair<Player,Matrix4f>>(Settings.numPlayers*2);

        Time.startTime = Time.lastFPSTime = Time.lastFrameTime = Time.time
                = Sys.getTime()/(double)Sys.getTimerResolution();
        
        //some debug info
        System.out.println("Sys Timer value: "+Sys.getTime());
        System.out.println("Sys Timer resolution: "+Sys.getTimerResolution());
    }
    
    public void run() {
        game.init();
        Dimension newDim;
        while(running){
            
            newDim =newCanvasSize.getAndSet(null);
            if(newDim!=null) {
                windowX = newDim.width;
                windowY = newDim.height;
                windowRightBorder = windowX - fieldSize - windowLeftBorder;
                windowBotBorder = windowY - fieldSize - windowTopBorder;
                glViewport(0,0,windowX, windowY);
            }
            
            doInput();
            game.inputLogic(Time.deltaTime);
            game.gameLogic();
            draw();
            Time.time = Sys.getTime()/(double)Sys.getTimerResolution();
            frameCounter++;
            if(frameCounter>40) {
                Time.fps = (frameCounter/((float)(Time.time-lastFPSTime))) *0.8+Time.fps*0.2;
                frameCounter=0; lastFPSTime=Time.time;
                //Display.setTitle("Obacht!");// fps: "+((int)(fps*100)/100.0f)+" time: "+(int)((time-startTime)*1000)/1000.0 + " delta: "+deltaTime);
            }
            double waitTime = (double)(1.0/Time.targetFPS)-(Time.time-Time.lastFrameTime);
            if(waitTime>0) { //only sleep if we would sleep more than 0 msecs
                try {
                    Thread.sleep((int)(waitTime*1000));
                } catch (InterruptedException ex) {}
                Time.timeToWait=Time.timeToWait*0.9f+(waitTime)*0.1f;
            }
            Time.time = Sys.getTime()/(double)Sys.getTimerResolution();
            Time.deltaTime = (Time.time-Time.lastFrameTime)*Time.speed;
            if(DEBUG) {
                Time.deltaTime = 0.016f;
            }
            Time.lastFrameTime = Time.time;
            
            if(Display.isCloseRequested()) running=false;
        }
        Display.destroy();
        frame.dispose();
    }
        
    private void draw() {
        // get Graphics
        glViewport(0,0,windowX,windowY);
        glClearColor(0.5f,0.5f,0.5f,1.0f);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        
        //draw field
        glViewport(windowLeftBorder, windowBotBorder, fieldSize, fieldSize);
        drawField(0.9f);
        glViewport(0,0,windowX,windowY);
        drawBorder(0.5f);
        //gui!
        drawHUD(0.2f);
        Display.update();
        checkError();
    }
    private void drawBorder(float z) {
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
        pts .put(-1).put(edge).put(z)
            .put(edge).put(edge).put(z)
            .put(edge).put(-1).put(z)
            .put(-1).put(-1).put(z)
            .put(-1).put(edge).put(z);
        pts.flip();
        glColor4f(0.2f,0.2f,0.2f,1);
        glVertexPointer(3,0,pts);
        glDrawArrays(GL_LINE_STRIP, 0, 5);
        glPopMatrix();
    }
    
    private void drawField(float z) {
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,game.fieldColorTexture);
		glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
		
        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(1,1,1,1);
        glTexCoord2f(0,1); glVertex3f(-1, 1, z);
        glTexCoord2f(0,0); glVertex3f(-1,-1, z);
        glTexCoord2f(1,1); glVertex3f( 1, 1, z);
        glTexCoord2f(1,0); glVertex3f( 1,-1, z);
        glEnd();
        glDisable(GL_TEXTURE_2D);
        for(Player p: game.players){
            p.drawHead();
        }
    }
    private void drawHUD(float z) {
        float fontSize = Settings.hudFontSize;
        int hudXSize = windowRightBorder-hudLeftBorder-hudRightBorder;
        int hudYSize = windowY-windowTopBorder-windowBotBorder-hudTopBorder-hudBotBorder;
        glViewport(windowLeftBorder+fieldSize+hudLeftBorder, windowBotBorder+hudBotBorder, hudXSize, hudYSize);
        Matrix4f mat = new Matrix4f();
        mat.translate(new Vector3f(-1.0f,1.0f,0.0f));
        mat.scale(new Vector3f(1.0f/hudXSize, 1.0f/hudYSize, 1.0f));
        font.drawString("Player Scores:", mat,fontSize, z);
        mat.translate(new Vector3f(0,-fontSize*2,0));

		// draw player list
		drawLaterList.clear();
        for(Player p : game.players) {
            drawPlayerEntry(p, mat, z, true);
        }

        glColor3f(1, 1, 1);
        mat.translate(new Vector3f(0,-fontSize,0));
        
        String status;
        if(!game.pause) status="pause";
        else if(game.waitingForNewRound) status="new game";
        else status="unpause";
        font.drawString("SPACE = "+status, mat, fontSize, z);
        mat.translate(new Vector3f(0,-fontSize*2,0));
        
        glColor3f(0.15f, 0.15f, 0.15f);
        fontSize=20.0f;
        font.drawString("fps: "+Math.floor(Time.fps*100)/100, mat, fontSize, z);
        mat.translate(new Vector3f(0,-fontSize*1.3f,0));
        font.drawString("frameWait: "+Math.floor(Time.timeToWait*1000000)/100,
                mat, fontSize, z);
        mat.translate(new Vector3f(0,-fontSize*1.3f,0));
        font.drawString("time: "+Math.floor((Time.time-Time.startTime)*100)/100,
                mat, fontSize, z);
        mat.translate(new Vector3f(0,-fontSize*1.3f,0));
        font.drawString("delta: "+Math.floor((Time.deltaTime)*100000)/100, mat,
                fontSize, z);

		// draw elements that need to be in foreground
        for(Pair<Player,Matrix4f> p : drawLaterList) {
            drawPlayerEntry(p.a, p.b, z, false);
        }

        glViewport(0,0,windowX,windowY);
    }

	Matrix4f drawPlayerEntry(Player p, Matrix4f mat, float z, boolean addLater) {
		float fontSize = Settings.hudFontSize;
		float fontTmp = fontSize;
		
		if (p.lastScoreTime + Settings.scoreNameWinGrowTime > Time.time) {
			if(addLater) {
				drawLaterList.add(new Pair<Player,Matrix4f>(p, new Matrix4f(mat)));
				return mat.translate(new Vector3f(0,-fontSize*1.3f,0));
			}
			fontTmp += (Math.sin(Math.PI*(Time.time - p.lastScoreTime)
				/ (Settings.scoreNameWinGrowTime/2)
				- 0.5*Math.PI)+1)*fontSize*0.7f;
			z -= 0.1f;
		}

		glColor3ub((byte)p.color.getRed(), (byte)p.color.getGreen(), (byte)p.color.getBlue());
		
		font.drawString(p.name+": "+p.score, mat, fontTmp, z);
		return mat.translate(new Vector3f(0,-fontSize*1.3f,0));
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

	// debug
    private void drawTexture(int id, float z) {
        glPushMatrix();
        glLoadIdentity();
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D,id);
        glBegin(GL_TRIANGLE_STRIP);
        glColor4f(1,1,1,1);
        glTexCoord2f(0,0); glVertex3f(-1, 1, z);
        glTexCoord2f(0,1); glVertex3f(-1,-1, z);
        glTexCoord2f(1,0); glVertex3f( 1, 1, z);
        glTexCoord2f(1,1); glVertex3f( 1,-1, z);
        glEnd();
        glDisable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        newCanvasSize.set(canvas.getSize());
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {
        canvas.requestFocusInWindow();
    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        windowDeactivated(e);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        windowActivated(e);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        running = false;
        //TODO release resources?
    }

    @Override
    public void windowClosed(WindowEvent e) {
        running = false;
    }

    @Override
    public void windowIconified(WindowEvent e) {
        Time.targetFPS = 1;
        game.pause=true;
        
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        Time.targetFPS=60;
        
    }
        
    @Override
    public void windowActivated(WindowEvent e) {
        Time.targetFPS = 60;
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        Time.targetFPS=5;
        game.pause=true;
    }
}
