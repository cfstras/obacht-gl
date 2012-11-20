/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.font;

/**
 *
 * @author claus
 */
public class FontLoader {
    
    
   public FontPackage font;
   
   public FontLoader() {
       font = new FontPackage("Fleftex Mono","/res/Fleftex_M.ttf",30);
       font.buildFont("abcdefghijklmnopqrstuvwxyzäöüABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ \"\\/()[]{}=?!.,:;-_+&%$<>|ß");
       
   }
   
}
