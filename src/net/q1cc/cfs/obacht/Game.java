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

	int numPlayers;
    boolean pause = false;
    int fieldFrameBuffer;
    int fieldColorTexture;
    int fieldRenderBuffer;
    
    ArrayList<Player> players;
    int fieldSize;
	int renderBufferSize;
    Color gameBackground = Color.BLACK;
    boolean waitingForNewRound = false;
	boolean wasNewRound = false;
    
    public Game() {
        this.fieldSize = Settings.gameFieldSize;
		this.renderBufferSize = fieldSize * Settings.multiSampling;
		this.numPlayers = Settings.numPlayers;
    }

    void init() {
        players = new ArrayList<Player>(numPlayers);
        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player(i);
            players.add(p);
        }
        newRound();
    }
    
    void gameLogic() {
        if(pause) {
            return;
        }
        if(waitingForNewRound) {
            newRound();
			return;
        }
		if(wasNewRound) {
			wasNewRound = false;
			return;
		}
        
        glBindTexture(GL_TEXTURE_2D,0);
        glBindFramebuffer(GL_FRAMEBUFFER,fieldFrameBuffer);
        glViewport(0,0,renderBufferSize,renderBufferSize);
        //glClearColor(0.0f,0.0f,0.0f,1.0f);
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        int playersAlive=0;
        Player alivePlayer=null;
        for (Player p : players) {
            if(!p.alive) {
                continue;
            }
            Vec2 pos = p.pos;
            Vec2 newpos = new Vec2();
            newpos.x = pos.x + (float) (Math.sin(p.angle) * p.speed * Time.deltaTime);
            newpos.y = pos.y - (float) (Math.cos(p.angle) * p.speed * Time.deltaTime);
            Vec2 lastPos = p.lastPos;
            
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
            pos.minus(Vec2.minus(newpos,pos).mult(0.3f));
            verts.put((pos.x - sin(langlet) * width));//*2-1);
            verts.put((pos.y + cos(langlet) * width));//*2-1);
            verts.put(0.5);

            verts.put((pos.x + sin(langlet) * width));//*2-1);
            verts.put((pos.y - cos(langlet) * width));//*2-1);
            verts.put(0.5);
            verts.flip();
            
            //collision detection
            int occQ = glGenQueries();
            glBeginQuery(GL_SAMPLES_PASSED, occQ);
            
            glDepthFunc(GL_LESS); //set z-buffer to discard anything wich has already been drawn
            //glEnable(GL_RASTERIZER_DISCARD);
            glColor4f(0,1,0,1.0f);
            glEnableClientState(GL_VERTEX_ARRAY);
            glVertexPointer(3,0,verts);
            GLGUI.checkError();
            glDrawArrays(GL_QUADS, 0, 4);
            GLGUI.checkError();
            
            glEndQuery(GL_SAMPLES_PASSED);
            
            //now draw again
            
            DoubleBuffer dverts = BufferUtils.createDoubleBuffer(4*3);
            anglet = p.lastAngle + 0.5f * (float) Math.PI;
            langlet = p.lastLastAngle + 0.5f * (float) Math.PI;
            width = p.width;
            dverts.put((pos.x + sin(anglet) * width));//*2-1);
            dverts.put((pos.y - cos(anglet) * width));//*2-1);
            dverts.put(0.5);
            
            dverts.put((pos.x - sin(anglet) * width));//*2-1);
            dverts.put((pos.y + cos(anglet) * width));//*2-1);
            dverts.put(0.5);
            //put pos back a little
            lastPos.minus(Vec2.minus(pos,lastPos).mult(0.3f));
            dverts.put((lastPos.x - sin(langlet) * width));//*2-1);
            dverts.put((lastPos.y + cos(langlet) * width));//*2-1);
            dverts.put(0.5);

            dverts.put((lastPos.x + sin(langlet) * width));//*2-1);
            dverts.put((lastPos.y - cos(langlet) * width));//*2-1);
            dverts.put(0.5);
            dverts.flip();  
            
            if(newpos.wrap()) {//field wrap
                pos = newpos;
            }
            p.lastPos = pos;
            p.pos = newpos;
            
            glDepthFunc(GL_ALWAYS);
            //glDisable(GL_RASTERIZER_DISCARD);
            dverts.put(2,1).put(5,1).put(8,1).put(11,1);
            glVertexPointer(3,0,dverts);
            glColor4ub((byte)p.color.getRed(), (byte)p.color.getGreen(), (byte)p.color.getBlue(),(byte)255);
            //glColor3f(1.0f,1.0f,1.0f);
            glDrawArrays(GL_QUADS, 0, 4);
            
            int passed = glGetQueryObjectui(occQ,GL_QUERY_RESULT);
            glDeleteQueries(occQ);
            if(passed>2){
                //collide!
                System.out.println(p.toString()+": "+passed);
                p.die();
            } else {
                alivePlayer = p;
                playersAlive++;
            }
            
        }
        
        glDepthFunc(GL_LEQUAL);
        glBindFramebuffer(GL_FRAMEBUFFER,0);
        
        if(playersAlive<=1) {
            //start new round
            if(alivePlayer!=null) {
                alivePlayer.lastAlive();
            }
            pause=true;
            waitingForNewRound=true;
        }
    }

    void inputLogic(double deltaTime) {
        if(pause) {
            return;
        }
        for (Player p : players) {
            p.lastLastAngle = p.lastAngle;
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
        System.out.println("new game");

		//delete old fb //TODO do this on exit, too
        if(fieldFrameBuffer != 0) {
			glDeleteFramebuffers(fieldFrameBuffer);
		}
		if(fieldColorTexture != 0) {
			glDeleteTextures(fieldColorTexture);
		}
		if(fieldRenderBuffer != 0) {
			glDeleteRenderbuffers(0);
		}

        fieldFrameBuffer = glGenFramebuffers();
        fieldColorTexture = glGenTextures();
        fieldRenderBuffer = glGenRenderbuffers();
        glEnable(GL_TEXTURE_2D);
        glBindFramebuffer(GL_FRAMEBUFFER,fieldFrameBuffer);
        glBindTexture(GL_TEXTURE_2D,fieldColorTexture);
        glTexParameterf(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, renderBufferSize, renderBufferSize, 0,GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        //TODO for multisampling, check here, too
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D,fieldColorTexture,0);
        
        // create a render buffer as our depth buffer and attach it
        glBindRenderbuffer(GL_RENDERBUFFER, fieldRenderBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER,GL_DEPTH_COMPONENT, renderBufferSize, renderBufferSize);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_RENDERBUFFER, fieldRenderBuffer);
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if(status != GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("Error creating framebuffer. "+status);
        }
        
        glViewport(0,0,renderBufferSize,renderBufferSize);
        glClearColor(0.0f,0.0f,0.0f,1.0f);
        glClearDepth(0.0);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
        glClearDepth(1.0);
        //drawEdgeDebug();
        
        // Go back to regular frame buffer rendering
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        //debug
        //glDrawBuffer(GL_FRONT_AND_BACK);
        
        for(Player p : players) {
            p.spawn();
        }
        waitingForNewRound=false;
		wasNewRound = true;
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
