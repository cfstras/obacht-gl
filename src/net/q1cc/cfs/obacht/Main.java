package net.q1cc.cfs.obacht;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.lwjgl.LWJGLException;


class Main {

    public final static int NUM_PLAYERS = 1;
    public final static int GAME_FIELD_SIZE = 800;
    
    static boolean collisionDisabled=false;
    
    static Random r;
    static GLGUI gui;
    static Game game;
    
    public static void main(String[] args) {
        try {
            String system;
            String prop = System.getProperty("os.name");
            if("Linux".contains(prop)){
                system = "linux";
            } else if("Windows".contains(prop)){
                system = "windows";
            } else if("Mac".contains(prop)){
                system = "macosx";
            } else if("Solaris".contains(prop)){
                system = "solaris";
            } else {
                System.out.println("OHMYGODWTFWTFHELP");
                return;
            }
            
            System.setProperty("org.lwjgl.librarypath",new File(".").getCanonicalPath()+"/lib/native/"+system);
            System.setProperty("org.lwjgl.util.Debug","true");
            System.setProperty("org.lwjgl.util.NoChecks","false");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        try {
            r = new Random();
            game = new Game();
            gui = new GLGUI(game);
            gui.run();
        } catch (LWJGLException ex) {
            ex.printStackTrace();
        }
        
    }    
    
}
