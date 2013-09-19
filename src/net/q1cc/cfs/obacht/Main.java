package net.q1cc.cfs.obacht;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;


class Main {
    
    static boolean collisionDisabled=false;
    
    static Random r;
    static GLGUI gui;
    static Game game;
    
    public static void main(String[] args) {
        try {
            String system;
            String prop = System.getProperty("os.name");
            if(prop==null) {
                System.out.println("Error: could not determine system type.");
                return;
            }
            if(prop.contains("Linux")){
                system = "linux";
            } else if(prop.contains("Windows")){
                system = "windows";
            } else if(prop.contains("Mac")){
                system = "macosx";
            } else if(prop.contains("Solaris")){
                system = "solaris";
            } else {
                System.out.println("can't identify system \""+prop+"\"");
                return;
            }
            
            System.setProperty("org.lwjgl.librarypath",new File(".").getCanonicalPath()+"/lib/native/"+system);
            System.setProperty("org.lwjgl.util.Debug","true");
            System.setProperty("org.lwjgl.util.NoChecks","false");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("LWJGL "+Sys.getVersion());
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
