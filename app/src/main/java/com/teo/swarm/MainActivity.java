package com.teo.swarm;
/*
   Theodoros Sokratous 2023
   City University of London
   MS Dissertation project 2023

 */
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

/*
 The main activity view where the PSO Simulation takes place
 It sets several input variables from previous UI activity
 and creates the GameSurface that starts the animation
 */
public class MainActivity extends AppCompatActivity {
    private GameSurface myGameSurface;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent intent = getIntent();
        int sn = intent.getIntExtra("SwarmNum",20);
        int ss = intent.getIntExtra("SwarmSize",10);
        float sp = intent.getFloatExtra("SwarmSpeed",0.5f);
        int ar = intent.getIntExtra("AttackerRange",30);
        int tf = intent.getIntExtra("TurnFactor",20);
        int vr = intent.getIntExtra("VisualRange",40);
        int pr = intent.getIntExtra("ProtectedRange",20);
        int maxs = intent.getIntExtra("MaxSpeed",32);
        int mins = intent.getIntExtra("MinSpeed",15);

        myGameSurface = new GameSurface(this,sn,ss,sp,ar,tf,vr,pr,maxs,mins);
        this.setContentView(myGameSurface);
    }

    @Override
    protected void onPause() {
        super.onPause();
        myGameSurface.pause();
    }

    //running the game when activity is resumed
    @Override
    protected void onResume() {
        super.onResume();
        this.setContentView(myGameSurface);
        myGameSurface.resume();
    }
}