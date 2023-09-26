package com.teo.swarm;
/*
   Theodoros Sokratous 2023
   City University of London
   MS Dissertation project 2023

 */

/*
 Abstract class to hold common game object data
 */
public abstract class GameObject {
    protected int rad;
    protected int x;
    protected int y;


    public GameObject(int x,int y,int rad){
        this.x = x;
        this.y = y;
        this.rad = rad;
    }

    public int getX()  {
        return this.x;
    }

    public int getY()  {
        return this.y;
    }

    public int getRad()  {
        return this.rad;
    }
}
