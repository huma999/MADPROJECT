package com.example.game;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean isMute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ActivityBack.class));
            }
        });
        TextView Score= findViewById(R.id.Score);
        SharedPreferences shared= getSharedPreferences("Game", MODE_PRIVATE);
        Score.setText("HighScore"+shared.getInt("HighScore",0));


    isMute=shared.getBoolean("isMute",false);
    ImageView VolumeUp =findViewById(R.id.VolumeUp);

     if ( isMute)
     {
         VolumeUp.setImageResource(R.drawable.baseline_volume_off_24);
     }
     else {
         VolumeUp.setImageResource(R.drawable.baseline_volume_up_24);
     }
     VolumeUp.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
             isMute= !isMute;
             if (isMute)
             {
                 VolumeUp.setImageResource(R.drawable.baseline_volume_off_24);
             }
             else
                 VolumeUp.setImageResource(R.drawable.baseline_volume_up_24);

             SharedPreferences.Editor editor= shared.edit();
             editor.putBoolean("isMute", isMute);
             editor.apply();

         }
     });
    }
}