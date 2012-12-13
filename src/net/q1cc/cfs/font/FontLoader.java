/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.font;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Hashtable;
import net.q1cc.cfs.obacht.Settings;
import org.lwjgl.BufferUtils;
import org.lwjgl.MemoryUtil;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import org.lwjgl.util.vector.Matrix4f;

/**
 *
 * @author claus
 */
public class FontLoader {
    
    
   public FontPackage font;
   public int fontTexID;
   ComponentColorModel colorModel;
   boolean loaded;
   FloatBuffer matBuf;
   
    public FontLoader() {
        matBuf = BufferUtils.createFloatBuffer(16);
        colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                new int[]{8, 8, 8, 8},
                true,
                false,
                ComponentColorModel.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
    }
   
   public void loadFont() {
       font = new FontPackage("Fleftex Mono","/res/Fleftex_M.ttf",Settings.fontQuality);
       font.buildFont("abcdefghijklmnopqrstuvwxyzäöüABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ1234567890 \"\\/()[]{}=?!.,:;-_+&%$<>|ß");
       
       fontTexID = glGenTextures();
       glBindTexture(GL_TEXTURE_2D, fontTexID);
       glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
       glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
       
       ByteBuffer buf = convertImageData(font.fontTex);
       
       glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,font.size,font.size,0,GL_RGBA,GL_UNSIGNED_BYTE,buf);
       loaded=font.loaded;
   }
   
   public void drawString(String s, Matrix4f trans,float size) {
       if(!loaded) {
           System.out.println("error: font not loaded, tried to draw string "+s);
           return;
       }
       glEnable(GL_TEXTURE_2D);
       glBindTexture(GL_TEXTURE_2D, fontTexID);
       glPushMatrix();
       matBuf.clear();
       trans.store(matBuf);
       matBuf.flip();
       //glColor4f(1,1,1,1);
       glLoadMatrix(matBuf);
       glBegin(GL_TRIANGLES);
       size *= font.size/font.fontSize;
       float x=0;
       for(int i=0;i<s.length();i++) {
           char c = s.charAt(i);
           Rectangle2D.Float rect = font.getCharRect(c);
           glTexCoord2f(rect.x           , rect.y            ); glVertex3f(x, 0, 1);
           glTexCoord2f(rect.x           , rect.y+rect.height); glVertex3f(x, -rect.height*size, 1);
           glTexCoord2f(rect.x+rect.width, rect.y            ); glVertex3f(x+rect.width*size, 0, 1);
           
           glTexCoord2f(rect.x+rect.width, rect.y            ); glVertex3f(x+rect.width*size, 0, 1);
           glTexCoord2f(rect.x           , rect.y+rect.height); glVertex3f(x, -rect.height*size, 1);
           glTexCoord2f(rect.x+rect.width, rect.y+rect.height); glVertex3f(x+rect.width*size, -rect.height*size, 1);
           x+=rect.width*size;
       }
       glEnd();
       glPopMatrix();
       glDisable(GL_TEXTURE_2D);
   }
   
    /**
     * Convert the buffered image to a texture
     *
     * @param bufferedImage The image to convert to a texture
     * @param texture The texture to store the data into
     * @return A buffer containing the data
     */
    private ByteBuffer convertImageData(BufferedImage bufferedImage) {
        ByteBuffer imageBuffer;
        WritableRaster raster;
        BufferedImage texImage;

        int texWidth = 2;
        int texHeight = 2;

        // find the closest power of 2 for the width and height
        // of the produced texture
        while (texWidth < bufferedImage.getWidth()) {
            texWidth *= 2;
        }
        while (texHeight < bufferedImage.getHeight()) {
            texHeight *= 2;
        }
        

        // create a raster that can be used by OpenGL as a source
        // for a texture
        raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,4,null);
        texImage = new BufferedImage(colorModel,raster,false,new Hashtable());

        // copy the source image into the produced image
        Graphics g = texImage.getGraphics();
        g.setColor(new Color(0f,0f,0f,0f));
        g.fillRect(0,0,texWidth,texHeight);
        g.drawImage(bufferedImage,0,0,null);

        // build a byte buffer from the temporary image
        // that be used by OpenGL to produce a texture.
        byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData();

        imageBuffer = ByteBuffer.allocateDirect(data.length);
        imageBuffer.order(ByteOrder.nativeOrder());
        imageBuffer.put(data, 0, data.length);
        imageBuffer.flip();

        return imageBuffer;
    }
   
}
