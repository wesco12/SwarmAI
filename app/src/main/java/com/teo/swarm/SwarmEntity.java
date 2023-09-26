package com.teo.swarm;
/*
   Theodoros Sokratous 2023
   City University of London
   MS Dissertation project 2023

 */
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/*
 Swarm entity class. It handles the behaviour and movement of a single swarm entity
 and how it is drawn on screen.
 */
public class SwarmEntity extends GameObject implements  Comparable<SwarmEntity>{
    private  float MAX_VELOCITY=0.5f;
    private float velocity = 0.0f;
    private float alpha = 0.5f;
    private float beta = 0.1f;
    private float prDistX = 0.0f;
    private float prDistY = 0.0f;
    private float glBestX = 1.0f;
    private float glBestY = 1.0f;
    private float glBest = 0.0f;
    private int glPosX=0;
    private int glPosY=0;
    private float lcBestX = 1.0f;
    private float lcBestY = 1.0f;
    private float lcBest = 0;

    private float movingVectorX = 10.0f;
    private float movingVectorY = 5.0f;
    private int targetX = 0;
    private int targetY = 0;
    private int despairStepX = 0;
    private int despairStepY = 0;
    private long lastDrawNanoTime =-1;
    protected float xdiff = 0.0f;
    protected float ydiff = 0.0f;
    private String myColor="";
    private int mode = 0;
    protected double vx;
    protected double vy;
    protected double biasval=0.001;
    private GameSurface gameSurface;
    public SwarmEntity(GameSurface gameSurface, int x, int y, int r,float sp,String c) {
        super(x, y,r);
        this.myColor = c;
        this.gameSurface= gameSurface;
        this.MAX_VELOCITY = sp;
    }
    @Override
    public int compareTo(SwarmEntity other) {
        if(mode==0)
            if(lcBest > other.getBest())
                return 1;
            else return -1;
        else if(mode==1)
            if(lcBest < other.getBest())
                return 1;
            else return -1;
        return 0;
    }
    public void update()  {

        // Current time in nanoseconds
        long now = System.nanoTime();

        // Never once did draw.
        if(lastDrawNanoTime==-1) {
            lastDrawNanoTime= now;
        }
        // Change nanoseconds to milliseconds (1 nanosecond = 1000000 milliseconds).
        int deltaTime = (int) ((now - lastDrawNanoTime)/ 1000000 );

        //update velocity based on PSO algorithm
        double epsilon1 = Math.random();
        double epsilon2 = Math.random();


        //velocity =(float) ( velocity - alpha * epsilon1 * (glBest - prDist) -
        //                      beta *  epsilon2 * (lcBest-prDist) );
        //calculate new position and velocity
/*        int dir = 0;
        if(mode==1) {
          dir = 1;
        } else if(mode==0){
          dir = -1;
        } else{
            dir =0;
        }*/
        int vectorX = (int) (movingVectorX / Math.abs(movingVectorX));
        int vectorY = (int) (movingVectorY / Math.abs(movingVectorY));
        float atX = (float) (this.gameSurface.getSocialInfluence() *vectorX* epsilon1 * Math.abs(glBestX - prDistX));
        float btX = (float) (this.gameSurface.getSelfCognition() *vectorX* epsilon2 * Math.abs(lcBestX - prDistX));
        float atY = (float) (this.gameSurface.getSocialInfluence() *vectorY* epsilon1 * Math.abs(glBestY - prDistY));
        float btY = (float) (this.gameSurface.getSelfCognition()*vectorY * epsilon2 * Math.abs(lcBestY - prDistY));
        //velocity = VELOCITY + at + bt;
        //System.out.println("GLX:"+glBestX+"PR:"+prDistX);
        //System.out.println("GLY:"+glBestY+"PR:"+prDistY);
        //if(mode==1) {
        //    System.out.println("MovingVectorX:" + movingVectorX + "-" + atX + ":" + btX);
        //    System.out.println("MovingVectorY:" + movingVectorY + "-" + atY + ":" + btY);
        //}
        //if(despairStepX >0) {
        //    despairStepX--;
        //}
        //else {
            movingVectorX = movingVectorX + atX + btX;
        //}
        //if(despairStepY >0) {
        //    despairStepY--;
        //}
        //else{
            movingVectorY = movingVectorY + atY + btY;
        //}
        //System.out.print("\nVelocityΔ="+ (at+bt));
        //System.out.print("\nVelocity="+ velocity);
        // Distance moves
        float myBest = this.getBest();
        float div = 1.0f;
        if(mode==1)
            div =  -myBest;
        else if(mode==0){
            div = myBest;
        }
        if(div!=0) {
            velocity = MAX_VELOCITY - MAX_VELOCITY * ( 100 * (float) Math.random() / div);
        }
        else{
            velocity = 0.1f;
        }
        if(velocity<0) velocity = 0.1f;
        //System.out.println("VL:"+velocity+" GB="+this.glBest+" LB="+myBest);

        float distance = velocity * deltaTime;
        prDistX = lcBestX;
        prDistY = lcBestY;
        //glBest = distance;
        //lcBest = distance;
        double movingVectorLength = Math.sqrt(movingVectorX* movingVectorX + movingVectorY*movingVectorY);
        // When the swarm entity touches the edge of the screen, then change direction
        //appropriately to simulated real world situation

        if(this.x < 0 )  {
            this.x = this.gameSurface.getWidth()-2*rad;//0;
            this.y = this.gameSurface.getHeight()- 2*rad - this.y;
            //this.despairStepX = 50;
            //this.movingVectorX = - this.movingVectorX;

        } else if(this.x > this.gameSurface.getWidth() - 2*rad)  {
            this.x = 0;
            this.y = this.gameSurface.getHeight()- 2*rad - this.y;
            //this.despairStepX = 50;
            //this.x= this.gameSurface.getWidth()-2*rad;
            //this.movingVectorX = - this.movingVectorX;
        }

        if(this.y < 0 )  {
            this.y= this.gameSurface.getHeight()- 2*rad;
            this.x = this.gameSurface.getWidth()-2*rad - this.x;
            //this.y = 0;
            //this.despairStepY = 50;
            //this.movingVectorY = - this.movingVectorY;
        } else if(this.y > this.gameSurface.getHeight()- 2*rad)  {
            this.y=0;
            this.x = this.gameSurface.getWidth()-2*rad - this.x;
            //this.despairStepY = 50;
            //this.y= this.gameSurface.getHeight()- 2*rad;
            //this.movingVectorY = - this.movingVectorY ;
        }

        // Calculate the new position of the swarm entity.
        this.xdiff = (float) (distance* movingVectorX / movingVectorLength);
        this.ydiff = (float) (distance* movingVectorY / movingVectorLength);
        this.vx = this.xdiff;
        this.vy = this.ydiff;
        this.x = x +  (int)(this.xdiff);
        this.y = y +  (int)(this.ydiff);

    }

    public void draw(Canvas canvas)  {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor(myColor)); //"#FF0000"));
        canvas.drawCircle(x,y,rad,paint); //   drawBitmap(bitmap,x, y, null);
        //draw also the direction vector as a line to indicate direction
        paint.setColor(Color.parseColor("#FFFFFF"));
        paint.setStrokeWidth(3);
        canvas.drawLine(x,y,x+xdiff,y+ydiff,paint);
        //System.out.println("VL:"+x+","+y+":"+xdiff+","+ydiff);
        //int vectorX = (int) (movingVectorX / Math.abs(movingVectorX));
        //int vectorY = (int) (movingVectorY / Math.abs(movingVectorY));
        //if(this.mode==1) {
            //paint.setColor(Color.parseColor("#2222FF"));
            //canvas.drawLine(x, y, targetX, targetY, paint);
            //paint.setColor(Color.parseColor("#FF2222"));
            //canvas.drawLine(x, y, glPosX, glPosY, paint);
        //} else if(this.mode==0){
        //    paint.setColor(Color.parseColor("#2222FF"));
        //    canvas.drawLine(x+lcBestX, y+lcBestY, x, y, paint);
        //    paint.setColor(Color.parseColor("#FF2222"));
        //    canvas.drawLine(x+ glBestX, y+ glBestY, x, y, paint);
        //}
        // Last draw time.
        this.lastDrawNanoTime= System.nanoTime();
    }

    public void setMovingVector(float movingVectorX, float movingVectorY)  {
        //if(despairStepX==0)
          this.movingVectorX= movingVectorX;
        //if(despairStepY==0)
          this.movingVectorY = movingVectorY;
    }

    public void setTarget(int xt,int yt) {
        this.targetX = xt;
        this.targetY = yt;

        //check the mirrored ends and find the minimum or maximum of the
        //distance according to the scenario, attack or run
        //1. Case. Direct distance.
        //float x1 = Math.abs(this.x -xt);
        //float y1 = Math.abs(this.y -yt);
        //double d1 = Math.sqrt(x1*x1+y1*y1);

        //2. Case. Mirrored from x side
       //float x21 = Math.min(this.x,xt);
        //float x22 = this.gameSurface.getWidth() - Math.max(this.x,xt);
        //find the slope
        //float sl = (yt-this.y)/(xt-this.x);

        //float y21 =0;
        //float y22 = 0;
        //double d2 = Math.sqrt(x21*x21+y21*y21) + Math.sqrt(x22*x22+y22*y22);

        //3. Case. Mirrored form y side
        //float y31 = Math.min(this.y,yt);
      /*  float y32 = this.gameSurface.getHeight() - Math.max(this.y,yt);
        float x31=0;
        float x32=0;
        double d3 = Math.sqrt(x31*x31+y31*y31) + Math.sqrt(x32*x32+y32*y32);

        if(this.mode==1){
          if(d1>=d2&&d1>=d3){
              this.lcBestX = x1;
              this.lcBestY = y1;
              this.lcBest = (float) d1;
          }else if(d2>=d1&&d2>=d3){

              this.lcBest = (float) d2;
          }else if(d3>=d1&&d3>=d2){

              this.lcBest = (float) d3;
          }

        }else if(this.mode==0){

        }*/

        this.lcBestX = Math.abs(this.x -xt);

        this.lcBestY = Math.abs(this.y -yt);

    }

    public void setGlBest(float gb,float gx, float gy,int px,int py){
        this.glBest = gb;
        this.glBestX = gx;
        this.glBestY = gy;
        this.glPosX  = px;
        this.glPosY  = py;
    }

    public float getBest(){
        return (float) Math.sqrt(this.lcBestX*this.lcBestX+this.lcBestY*this.lcBestY);
    }

    public float getBestX(){return this.lcBestX;}
    public float getBestY(){return this.lcBestY;}

    public void setMode(int mode) {
        this.mode = mode;
    }
}
