package com.teo.swarm;
/*
   Theodoros Sokratous 2023
   City University of London
   MS Dissertation project 2023

 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/*
   The main class tha handles what happens on screen
   It extends the SurfaceView class and uses the update and draw methods
   to control the animation of swarm entities on screen.
   It uses also the GameThread class for the timing control of the animation
 */
public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {

    private GameThread gameThread;
    private SurfaceHolder myHolder;
    private List<SwarmEntity> swarm;
    private float glBest = 0.0f;
    private float glBestX = 0.0f;
    private float glBestY = 0.0f;
    private int curX = 0;
    private int curY = 0;
    private int glPosX = 0;
    private int glPosY = 0;
    private int mode = 3;
    private int noChangeIter = 0;
    private double evalFactor = 0;
    private double inertiaWeight = 0;
    private String convState = "S1";
    private double selfCognition;
    private double socialInfluence;
    private List<String> colMap;
    private int radius = 10;
    private int snum = 30;
    private float speed = 0.5f;
    private int attackRadius = 300;
    private int destroyRadius = 50;


    //boids parameters
    private float turnfactor = 20f;
    private int visualRange = 40;
    private int visualRangesq = 1600;
    private int protectedRange = 8;
    private int protectedRangesq = 64;
    private static final double centeringfactor = 0.0005;
    private static final float avoidfactor = 0.05f;
    private static final float matchingfactor = 0.05f;
    private int maxspeed = 64;
    private int minspeed = 32;
    private static final float maxbias = 0.01f;
    private static final double bias_increment = 0.00004;
    private boolean firstTime = true;

    /*
      Constructor method
      @params Context The Activity view that the surface view is created on
      @params sn      The swarm number of entities
      @params ss      The swarm entities size(radius)
      @params sp      The swarm entities speed
      @params ar      The attacker radius of attack
     */
    public GameSurface(Context context, int sn, int ss, float sp, int ar,int tf,int vr,int pr,int maxs,int mins) {
        super(context);
        //make a color list of possible colors tou use
        this.buildColMap();
        // Make Game Surface focusable so it can handle events. .
        this.setFocusable(true);
        //setup Swarm from UI
        this.snum = sn;
        this.radius = ss;
        this.speed  = sp;
        this.attackRadius = ar;

        this.turnfactor = tf;
        this.visualRange = vr;
        this.visualRangesq = vr*vr;
        this.protectedRange = pr;
        this.protectedRangesq = pr*pr;
        this.maxspeed = maxs;
        this.minspeed = mins;

        // Sét callback.
        this.getHolder().addCallback(this);
    }

    /*
     Simple method to create a color map for randomy assigning to swarm entities
     */
    void buildColMap() {
        colMap = new ArrayList<String>();
        colMap.add("#FF0000");
        colMap.add("#00FF00");
        colMap.add("#0000FF");
        colMap.add("#FFFF00");
        colMap.add("#FF00FF");
        colMap.add("#00FFFF");
        colMap.add("#FF2222");
        colMap.add("#22FF22");
        colMap.add("#2222FF");
        colMap.add("#FFFF22");
        colMap.add("#FF22FF");
        colMap.add("#22FFFF");
        colMap.add("#FFFFFF");
    }

    /*
     PSO update the evolutionary factor coefficient recalculation
     It finds the global min and max distances
     and compairs that to the global best to revaluate the eval Factor
     */
    void updateEvolutionaryFactor() {
        double gl_dist;
        double min_dist = Double.POSITIVE_INFINITY;
        double max_dist = Double.NEGATIVE_INFINITY;

        double temp_dist;
        for (int i = 0; i < swarm.size(); i++) {
            temp_dist = swarm.get(i).getBest();
            if (temp_dist < min_dist) min_dist = temp_dist;
            if (temp_dist > max_dist) max_dist = temp_dist;
        }
        gl_dist = glBest;
        if (gl_dist < min_dist) min_dist = gl_dist;
        if (gl_dist > max_dist) max_dist = gl_dist;
        this.evalFactor = (gl_dist - min_dist) / (max_dist - min_dist);

    }

    /*
     Revaluate the inertia weight using the eval factor
     */
    void adaptInertiaWeight() {
        this.inertiaWeight = 1.0 / (1 + 1.5 * Math.exp(-2.8 * evalFactor));
    }

    /*
      Update the convergence state.
      According to the eval factor the Swarm system can be in one of the Four states
      S1 (initial), S2, S3, S4.
     */
    void updateConvState() {
        if (evalFactor <= 0.3) {
            this.convState = "S3";
            return;
        }
        if (evalFactor <= 0.6) {
            this.convState = "S2";
            return;
        }
        if (evalFactor <= 0.8) {
            this.convState = "S3";
            return;
        }
        this.convState = "S4";
    }

    /*
      Finally recalculate the Coefficients in the PSO algorithm based on
      the convergence state.
     */
    void adaptAccelerationCoeefs() {
        double deltaSlight;
        double delta;
        delta = Math.random() * 0.05 + 0.05;
        deltaSlight = Math.random() * 0.45 + 0.05;
        switch (this.convState) {
            case "S1":
                selfCognition += delta;
                socialInfluence -= delta;
                break;
            case "S2":
                selfCognition -= deltaSlight;
                socialInfluence += deltaSlight;
                break;
            case "S3":
                selfCognition += deltaSlight;
                socialInfluence += deltaSlight;
                break;
            case "S4":
                selfCognition -= delta;
                socialInfluence += delta;
                break;
        }
        double tempSum = selfCognition + socialInfluence;
        if (selfCognition <= 0) selfCognition = 0.1;
        if (socialInfluence <= 0) socialInfluence = 0.1;
        if (tempSum > 4) {
            socialInfluence = 4.0 * (socialInfluence / tempSum);
            selfCognition = 4.0 * (selfCognition / tempSum);
        }
    }

    /*
      Boids algorithm.
      When swarm is in neutral state (not attacking nor being attack, move as a flock using
      boids algorithm
     */
    public void boidsUpdate() {
        float xpos_avg;
        float ypos_avg;
        float xvel_avg;
        float yvel_avg;
        float neighboring_boids;
        float close_dx;
        float close_dy;

        for (int i = 0; i < swarm.size(); i++) {
            xpos_avg = ypos_avg = xvel_avg = yvel_avg = neighboring_boids = close_dx = close_dy = 0;
            for (int j = 0; j < swarm.size(); j++) {
                if (i == j) continue;
                //System.out.println("JJJ-"+j);
                float dx = swarm.get(i).x - swarm.get(j).x;
                float dy = swarm.get(i).y - swarm.get(j).y;
                if (Math.abs(dx) < visualRange && Math.abs(dy) < visualRange) {
                    float squared_distance = dx * dx + dy * dy;
                    if (squared_distance < protectedRangesq) {
                        close_dx += dx;
                        close_dy += dy;
                    } else if (squared_distance < visualRangesq) {
                        xpos_avg += swarm.get(j).x;
                        ypos_avg += swarm.get(j).y;
                        xvel_avg += swarm.get(j).vx;
                        yvel_avg += swarm.get(j).vy;

                        neighboring_boids += 1;
                    }
                }


            }
            if (neighboring_boids > 0) {
                xpos_avg = xpos_avg / neighboring_boids;
                ypos_avg = ypos_avg / neighboring_boids;
                xvel_avg = xvel_avg / neighboring_boids;
                yvel_avg = yvel_avg / neighboring_boids;

                swarm.get(i).vx = (swarm.get(i).vx +
                        (xpos_avg - swarm.get(i).x) * centeringfactor +
                        (xvel_avg - swarm.get(i).vx) * matchingfactor);

                swarm.get(i).vy = (swarm.get(i).vy +
                        (ypos_avg - swarm.get(i).y) * centeringfactor +
                        (yvel_avg - swarm.get(i).vy) * matchingfactor);
            }
            swarm.get(i).vx = swarm.get(i).vx + (close_dx * avoidfactor);
            swarm.get(i).vy = swarm.get(i).vy + (close_dy * avoidfactor);
            if (swarm.get(i).y < 100)
                swarm.get(i).vy = swarm.get(i).vy + turnfactor;
            if (swarm.get(i).x > (this.getWidth() - 100))
                swarm.get(i).vx = swarm.get(i).vx - turnfactor;
            if (swarm.get(i).x < 100)
                swarm.get(i).vx = swarm.get(i).vx + turnfactor;
            if (swarm.get(i).y > (this.getHeight() - 100))
                swarm.get(i).vy = swarm.get(i).vy - turnfactor;


            if (i % 2 == 0) {
                if (swarm.get(i).vx > 0)
                    swarm.get(i).biasval = Math.min(maxbias, swarm.get(i).biasval + bias_increment);
                else
                    swarm.get(i).biasval = Math.max(bias_increment, swarm.get(i).biasval - bias_increment);
                swarm.get(i).vx = (1 - swarm.get(i).biasval) * swarm.get(i).vx + (swarm.get(i).biasval * 1);
            } else {
                if (swarm.get(i).vx < 0)
                    swarm.get(i).biasval = Math.min(maxbias, swarm.get(i).biasval + bias_increment);
                else
                    swarm.get(i).biasval = Math.max(bias_increment, swarm.get(i).biasval - bias_increment);
                swarm.get(i).vx = (1 - swarm.get(i).biasval) * swarm.get(i).vx + (swarm.get(i).biasval * (-1));
            }

            swarm.get(i).vx = (1 - swarm.get(i).biasval) * swarm.get(i).vx + (swarm.get(i).biasval * 1);
            double bspeed = Math.sqrt(swarm.get(i).vx * swarm.get(i).vx + swarm.get(i).vy * swarm.get(i).vy);
            if (bspeed < minspeed) {
                swarm.get(i).vx = (swarm.get(i).vx / bspeed) * minspeed;
                swarm.get(i).vy = (swarm.get(i).vy / bspeed) * minspeed;
            }
            if (bspeed > maxspeed) {
                swarm.get(i).vx = (swarm.get(i).vx / bspeed) * maxspeed;
                swarm.get(i).vy = (swarm.get(i).vy / bspeed) * maxspeed;
            }
            swarm.get(i).x = swarm.get(i).x + (int) Math.round(swarm.get(i).vx);
            swarm.get(i).y = swarm.get(i).y + (int) Math.round(swarm.get(i).vy);
            swarm.get(i).xdiff = (float) swarm.get(i).vx;
            swarm.get(i).ydiff = (float) swarm.get(i).vy;
            System.out.println("--" + i + ":" + swarm.get(i).vx + "," + swarm.get(i).vy);
        }
    }

    /*
     Calculate the boids position for a single swarm entity.
     This is use when in attack mode and entoty is outside the attacker influence circle
     */
    public void boidsUpdateSingle(int i) {
        float xpos_avg;
        float ypos_avg;
        float xvel_avg;
        float yvel_avg;
        float neighboring_boids;
        float close_dx;
        float close_dy;


        xpos_avg = ypos_avg = xvel_avg = yvel_avg = neighboring_boids = close_dx = close_dy = 0;
        for (int j = 0; j < swarm.size(); j++) {
            if (i == j) continue;
            //System.out.println("JJJ-"+j);
            float dx = swarm.get(i).x - swarm.get(j).x;
            float dy = swarm.get(i).y - swarm.get(j).y;
            if (Math.abs(dx) < visualRange && Math.abs(dy) < visualRange) {
                float squared_distance = dx * dx + dy * dy;
                if (squared_distance < (protectedRange * protectedRange)) {
                    close_dx += dx;
                    close_dy += dy;
                } else if (squared_distance < (visualRange * visualRange)) {
                    xpos_avg += swarm.get(j).x;
                    ypos_avg += swarm.get(j).y;
                    xvel_avg += swarm.get(j).vx;
                    yvel_avg += swarm.get(j).vy;

                    neighboring_boids += 1;
                }
            }


        }
        if (neighboring_boids > 0) {
            xpos_avg = xpos_avg / neighboring_boids;
            ypos_avg = ypos_avg / neighboring_boids;
            xvel_avg = xvel_avg / neighboring_boids;
            yvel_avg = yvel_avg / neighboring_boids;

            swarm.get(i).vx = (swarm.get(i).vx +
                    (xpos_avg - swarm.get(i).x) * centeringfactor +
                    (xvel_avg - swarm.get(i).vx) * matchingfactor);

            swarm.get(i).vy = (swarm.get(i).vy +
                    (ypos_avg - swarm.get(i).y) * centeringfactor +
                    (yvel_avg - swarm.get(i).vy) * matchingfactor);
        }
        swarm.get(i).vx = swarm.get(i).vx + (close_dx * avoidfactor);
        swarm.get(i).vy = swarm.get(i).vy + (close_dy * avoidfactor);
        if (swarm.get(i).y < 100)
            swarm.get(i).vy = swarm.get(i).vy + turnfactor;
        if (swarm.get(i).x > (this.getWidth() - 100))
            swarm.get(i).vx = swarm.get(i).vx - turnfactor;
        if (swarm.get(i).x < 100)
            swarm.get(i).vx = swarm.get(i).vx + turnfactor;
        if (swarm.get(i).y > (this.getHeight() - 100))
            swarm.get(i).vy = swarm.get(i).vy - turnfactor;


        if (swarm.get(i).vx > 0)
            swarm.get(i).biasval = Math.min(maxbias, swarm.get(i).biasval + bias_increment);
        else
            swarm.get(i).biasval = Math.max(bias_increment, swarm.get(i).biasval - bias_increment);
        swarm.get(i).vx = (1 - swarm.get(i).biasval) * swarm.get(i).vx + (swarm.get(i).biasval * 1);
        double bspeed = Math.sqrt(swarm.get(i).vx * swarm.get(i).vx + swarm.get(i).vy * swarm.get(i).vy);
        if (bspeed < minspeed) {
            swarm.get(i).vx = (swarm.get(i).vx / bspeed) * minspeed;
            swarm.get(i).vy = (swarm.get(i).vy / bspeed) * minspeed;
        }
        if (bspeed > maxspeed) {
            swarm.get(i).vx = (swarm.get(i).vx / bspeed) * maxspeed;
            swarm.get(i).vy = (swarm.get(i).vy / bspeed) * maxspeed;
        }
        swarm.get(i).x = swarm.get(i).x + (int) Math.round(swarm.get(i).vx);
        swarm.get(i).y = swarm.get(i).y + (int) Math.round(swarm.get(i).vy);
        swarm.get(i).xdiff = (float) swarm.get(i).vx;
        swarm.get(i).ydiff = (float) swarm.get(i).vy;

    }

    /*
     Main update method to update all swarm entities position on screen
     Mode=3 Boids movement - Default movement at start of simulation
     Mode=0 Food source swarm attacks a single point
     Mode=1 Attacker is present, use advance PSO to move away
     */
    public void update() {
        if (mode == 3) {
            if (firstTime) {
                for (int i = 0; i < swarm.size(); i++) {
                    double rv = Math.random();
                    swarm.get(i).vx = rv * 6;
                    rv = Math.random();
                    swarm.get(i).vy = rv * 6;
                }
                firstTime = false;
            }
            //boids movement as a flock
            boidsUpdate();
        } else {
            if (mode == 0) {
                this.glBest = Float.POSITIVE_INFINITY;
            } else if (mode == 1) {
                this.glBest = Float.NEGATIVE_INFINITY;
            }
            updateEvolutionaryFactor();
            adaptInertiaWeight();
            updateConvState();
            adaptAccelerationCoeefs();

            swarm.forEach((se) -> {
                if (mode == 1) {
                    //check distance from attacker and see if PSO or boids will be applied

                    int movingVectorX = se.getX() - curX;
                    int movingVectorY = se.getY() - curY;
                    double dd = Math.sqrt(movingVectorX * movingVectorX + movingVectorY * movingVectorY);
                    if(dd < destroyRadius){
                        swarm.remove(se);
                    }
                    else {
                        if (dd < attackRadius) {
                            se.setMovingVector(movingVectorX, movingVectorY);
                            se.setTarget(curX, curY);
                            if (this.glBest < se.getBest()) {
                                this.glBest = se.getBest();
                                this.glBestX = se.getBestX();
                                this.glBestY = se.getBestY();
                                this.glPosX = se.getX();
                                this.glPosY = se.getY();
                            }
                        }
                    }
                } else if (mode == 0) {
                    int movingVectorX = curX - se.getX();
                    int movingVectorY = curY - se.getY();
                    se.setMovingVector(movingVectorX, movingVectorY);
                    se.setTarget(curX, curY);
                    if (this.glBest > se.getBest()) {
                        this.glBest = se.getBest();
                        this.glBestX = se.getBestX();
                        this.glBestY = se.getBestY();
                        this.glPosX = se.getX();
                        this.glPosY = se.getY();
                    }
                } else {
                    //random movement

                    double rv = Math.random();
                    int movingVectorX = se.getX() + (int) (rv * 10);
                    rv = Math.random();
                    int movingVectorY = se.getY() + (int) (rv * 10);
                    se.setMovingVector(movingVectorX, movingVectorY);
                }
            });

            for (int i = 0; i < swarm.size(); i++) {
                if (mode == 1) {
                    int dX = swarm.get(i).x - curX;
                    int dY = swarm.get(i).y - curY;
                    double dd = Math.sqrt(dX * dX + dY * dY);
                    if (dd < attackRadius) {
                        swarm.get(i).setMode(this.mode);
                        swarm.get(i).setGlBest(this.glBest, this.glBestX, this.glBestY, this.glPosX, this.glPosY);
                        swarm.get(i).update();
                    } else {
                        //boids movement outside
                        boidsUpdateSingle(i);
                    }
                } else {
                    swarm.get(i).setMode(this.mode);
                    swarm.get(i).setGlBest(this.glBest, this.glBestX, this.glBestY, this.glPosX, this.glPosY);
                    swarm.get(i).update();
                }
            }
            collisionDetect();
        }
        noChangeIter++;
    }
    /*
     Detects collision between swarm entities and moves them to avoid overlapping.
     It goes from the best particle (closes to the food source or further from the attacker
     and moves other particles away from it. It hen cascades through all particles until all are
     moved to the closest free spot.
     */
    public void collisionDetect() {
        Collections.sort(swarm);
        //collision detection in 2D for circles. Cascade from leader to rest and rearrange
        for (int i = 0; i < swarm.size(); i++) {
            for (int j = i + 1; j < swarm.size(); j++) {
                //check for collision between i ang j swarm entity
                int xDist = Math.abs(swarm.get(i).x - swarm.get(j).x);
                if (xDist < (2 * radius)) {
                    int yDist = Math.abs(swarm.get(i).y - swarm.get(j).y);
                    if (yDist < 2 * radius) {
                        float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
                        if (dist < (2 * radius)) {
                            if (swarm.get(i).x < swarm.get(j).x)
                                swarm.get(j).x += 2 * radius;
                            else
                                swarm.get(j).x -= 2 * radius;
                            if (swarm.get(i).y < swarm.get(j).y)
                                swarm.get(j).y += 2 * radius;
                            else
                                swarm.get(j).y -= 2 * radius;
                        }
                    }
                }
            }
        }
    }

    /*
     Handle touch events on the mobile screen. When releasing the touch the simulation is on
     food attack mode and swarm entities use PSO to attack the source. If touch is held and moved
     around it simulates an attacker.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            this.curX = (int) event.getX();
            this.curY = (int) event.getY();
            this.mode = 1;

            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            this.curX = (int) event.getX();
            this.curY = (int) event.getY();
            this.mode = 1;

            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            this.curX = (int) event.getX();
            this.curY = (int) event.getY();
            this.mode = 0;
        }
        return false;
    }

    /*
     The main draw method.
     It draws the swarm entities on the screen on their current position according to the PSO
     algorithm. It also show the Global best on top left corner. In attacker mode it also draw the
     circle of the attacker influence radius. In food mode a small square is drawn to represent
     the food source.
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#FFFFFF")); //"#FF0000"));
        paint.setTextSize(48f);

        Rect r = new Rect(this.curX - 7, this.curY - 7, this.curX + 7, this.curY + 7);

        canvas.drawRect(r, paint);
        paint.setTextSize(64f);
        canvas.drawText("GL=" + this.glBest , 40, 40, paint);
        canvas.drawText(""+swarm.size(),800,80,paint);

        //final int[] i = {80};
        swarm.forEach((se) -> {
            se.draw(canvas);
        });
        if (mode == 1) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(this.curX, this.curY, attackRadius, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#FF0000"));
            canvas.drawCircle(this.curX, this.curY, destroyRadius, paint);
        }
    }

    /*
      Randomly position the X coordinates
     */
    public int rePosX() {
        double r = Math.random();
        return (int) (r * super.getWidth());
    }
    /*
      Randomly position the Y coordinates
     */
    public int rePosY() {
        double r = Math.random();
        return (int) (r * super.getHeight());
    }

    /*
     Create the SurfaceView
     Create the specified number of swarm  entities and randomly positions them on the screen
     It also start the thread for the time control of the animation
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        swarm = new ArrayList<SwarmEntity>();
        this.myHolder = holder;

        double r = Math.random();
        int startX = (int) (r * super.getWidth());
        r = Math.random();
        int startY = (int) (r * super.getHeight());
        noChangeIter = 0;
        for (int i = 0; i < snum; i++) {
            swarm.add(new SwarmEntity(this, startX, startY, radius, this.speed, colMap.get((int) (13 * Math.random() - 1))));
            startX = this.rePosX();
            startY = this.rePosY();
            this.curX = (int) (200.0 * Math.random());
            this.curY = (int) (200.0 * Math.random());
            this.mode = 3;
        }

        this.gameThread = new GameThread(this, holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // Implements method of SurfaceHolder.Callback
    /*
     Stop the thread of the animation
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry = false;
        }
    }

    /*
      Pause the animation(thread)
     */
    public void pause() {
        //when the game is paused
        //setting the variable to false
        this.gameThread.setRunning(false);
        try {
            //stopping the thread
            this.gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    /*
     Resume the animation(thread)
     */
    public void resume() {
        //when the game is resumed
        //starting the thread again
        if (this.gameThread == null)
            this.gameThread = new GameThread(this, myHolder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    /*
     Return the social Influence coefficient for the PSO algorithm
     */
    public double getSocialInfluence() {
        return this.socialInfluence;
    }

    /*
     Return the self Cognition coefficient for the PSO algorithm
     */
    public double getSelfCognition() {
        return this.selfCognition;
    }
}