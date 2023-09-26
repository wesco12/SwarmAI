package com.teo.swarm;
/*
   Theodoros Sokratous 2023
   City University of London
   MS Dissertation project 2023

 */
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.slider.Slider;
/*
 Main activity view to handle user input. User sets here all parameters that influence the
 behaviour and size of the swarm
 */
public class MainActivityUI extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);

        Button btn = (Button)findViewById(R.id.button);
        //Swarm Parameters
        Slider sln = (Slider)findViewById(R.id.SwarmNum);
        Slider sls = (Slider)findViewById(R.id.SwarmSize);
        Slider slsp = (Slider)findViewById(R.id.SwarmSpeed);
        Slider attR = (Slider)findViewById(R.id.AttackerRange);
        //Boids Parameters
        Slider turnF = (Slider)findViewById(R.id.TurnF);
        Slider visualR = (Slider)findViewById(R.id.VisualR);
        Slider protectedR = (Slider)findViewById(R.id.ProtectedR);
        Slider maxSpeed = (Slider)findViewById(R.id.maxSpeed);
        Slider minSpeed = (Slider)findViewById(R.id.minSpeed);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sn = Math.round(sln.getValue());
                int ss = Math.round(sls.getValue());
                float sp = slsp.getValue();
                int ar = Math.round(attR.getValue());

                int tf = Math.round(turnF.getValue());
                int vr = Math.round(visualR.getValue());
                int pr = Math.round(protectedR.getValue());
                int maxs = Math.round(maxSpeed.getValue());
                int mins = Math.round(minSpeed.getValue());

                Intent intent = new Intent(MainActivityUI.this, MainActivity.class);
                intent.putExtra("SwarmNum", sn);
                intent.putExtra("SwarmSize", ss);
                intent.putExtra("SwarmSpeed", sp);
                intent.putExtra("AttackerRange", ar);

                intent.putExtra("TurnFactor", tf);
                intent.putExtra("VisualRange", vr);
                intent.putExtra("ProtectedRange", pr);
                intent.putExtra("MaxSpeed", maxs);
                intent.putExtra("MinSpeed", mins);

                startActivity(intent);
            }
        });
    }
}