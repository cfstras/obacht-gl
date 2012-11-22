/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author claus
 */
public class FontPackage {
    
    public String fontName;
    public String fileName;
    public boolean loaded;
    Font awtFont;
    
    BufferedImage fontTex;
    int size;
    
    char[] charIndex;
    Rectangle2D.Float[] charPosition;
    
    public FontPackage(String fontName, String fileName, float size) {
        this.fontName = fontName;
        this.fileName = fileName;
        try {
            awtFont = Font.createFont(Font.TRUETYPE_FONT, this.getClass().getResourceAsStream(fileName)).deriveFont(size);
            loaded=true;
            
        } catch (FontFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void buildFont(String chars) {
        FontRenderContext frc = new FontRenderContext(null, false, false);
        //count the needed vertice
        Rectangle2D maxCharBounds = awtFont.getMaxCharBounds(frc);
        //calc needed size
        boolean works = false;
        size=128;
        int line1Height=0;
        do {
            int x=0; int y=0;
            int currHeight=0;
            for(int i=0;i<chars.length();i++) {
                Rectangle2D bounds = awtFont.getStringBounds(chars, i, i+1, frc).getBounds2D();
                x += (int)bounds.getWidth();
                int newHeight=currHeight;
                if(currHeight<bounds.getHeight()) {
                    newHeight+= bounds.getHeight();
                }
                if(x>=size) {
                    if(y==0) {
                        line1Height=currHeight;
                    }
                    y += currHeight;
                    x = (int)bounds.getWidth();
                } else {
                    currHeight = newHeight;
                }
            }
            if(y<size && x<size) {
                works=true;
            } else {
                size*=2;
            }
        } while (!works);
        
        //build image
        charIndex = new char[chars.length()];
        charPosition = new Rectangle2D.Float[chars.length()];
        fontTex = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = fontTex.createGraphics();
        g.setFont(awtFont);
        g.setBackground(new Color(0,0,0,0));
        g.clearRect(0, 0, size, size);
        g.setColor(Color.WHITE);
        char[] charArr = chars.toCharArray();
        int x=0,y=line1Height;
        int currHeight=0;
        for(int i=0;i<chars.length();i++) {
            Rectangle2D bounds = awtFont.getStringBounds(chars, i, i+1, frc).getBounds2D();
            int newHeight = currHeight;
            if (currHeight < bounds.getHeight()) {
                newHeight += bounds.getHeight();
            }
            if (x + (int)bounds.getWidth() >= size) {
                y += currHeight;
                x = 0;
            } else {
                currHeight = newHeight;
            }
            g.drawChars(charArr, i, 1, x, y);
            charIndex[i] = chars.charAt(i);
            charPosition[i] = new Rectangle2D.Float(x/(float)size, (y-(float)bounds.getHeight())/(float)size, (float)bounds.getWidth()/(float)size, (float)bounds.getHeight()/(float)size);
            x += (int)bounds.getWidth();
        }
        g.dispose();
        
    }

    Float getCharRect(char c) {
        for(int i=0;i<charIndex.length;i++) {
            if(charIndex[i]==c) {
                return charPosition[i];
            }
        }
        throw new NullPointerException("tried to draw char that is not in texture.");
    }
}
