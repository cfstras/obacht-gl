/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.q1cc.cfs.obacht;

/**
 *
 * @author claus
 */
class Vec2 {

    static Vec2 minus(Vec2 a, Vec2 b) {
        return new Vec2(a.x-b.x,a.y-b.y);
    }
    float x;
    float y;
    public Vec2(float x, float y){
        this.x = x; this.y=y;
    }
    public Vec2(){
        this(0,0);
    }
    public Vec2(Vec2 cloneMe) {
        x=cloneMe.x; y=cloneMe.y;
    }

    void wrap() {
        //x = x - 1.0f*(float)Math.floor(x/1.0f);
        //y = y - 1.0f*(float)Math.floor(y/1.0f);
        if(x> 1.0f) x -= 2.0f;
        if(x<-1.0f) x += 2.0f;
        
        if(y> 1.0f) y -= 2.0f;
        if(y<-1.0f) y += 2.0f;
    }

    Vec2 minus(Vec2 b) {
        x -= b.x; y -=b.y;
        return this;
    }
    Vec2 add(Vec2 b) {
        x += b.x; y +=b.y;
        return this;
    }

    Vec2 mult(float f) {
        x*=f; y*=f;
        return this;
    }
    
    @Override public String toString(){
        return "("+(int)(x*1000)/1000.0f+","+(int)(y*1000)/1000.0f+")";
    } 
}
