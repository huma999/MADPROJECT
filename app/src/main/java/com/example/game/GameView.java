package com.example.game;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    public Thread Tobj;
    boolean playing;
    boolean Gameover = false;


    private BackGround background1, background2;
    int screenX;
    int screenY;
    public int score = 0;
    public Paint Paint;
    List<Bullet> bullets;
    public static float screenRatioX;
    public static float ScreenRatioY;
    private Flight flight;
    private enemyship[] enemies;
    private Random random;

    private SharedPreferences shared;
    private SoundPool sound;
    private  int ShootSound;
private ActivityBack activity;

    public GameView(ActivityBack activity, int screenX, int screenY) {
        super(activity);


        this.activity= activity;
        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 1920f / screenX;
        ScreenRatioY = 1080f / screenY;

        background1 = new BackGround(screenX, screenY, getResources());
        background2 = new BackGround(screenX, screenY, getResources());

        flight = new Flight(this, screenY, getResources());

        bullets = new ArrayList<>();

        background2.x = screenX;

        Paint = new Paint();
        Paint.setTextSize(128);
        Paint.setColor(Color.WHITE);

        shared= activity.getSharedPreferences("Game", Context.MODE_PRIVATE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            AudioAttributes audioAttributes=new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_GAME).build();
            sound= new SoundPool.Builder().setAudioAttributes(audioAttributes).build();

        }
        else

            sound= new SoundPool(1,AudioManager.STREAM_MUSIC,0);


        ShootSound= sound.load(activity,R.raw.shoot,1);


        enemies = new enemyship[4];
        for (int i = 0; i < 4; i++) {
            enemyship enemy = new enemyship(getResources());
            enemies[i] = enemy;
        }

        random = new Random();
    }


    @Override
    public void run() {
        while (playing) {
            update();
            draw();
            sleep();
        }
    }


    public void update() {
        background1.x -= 10 * screenRatioX;
        background2.x -= 10 * screenRatioX;

        if (background1.x + background1.Background.getWidth() < 0) {
            background1.x = screenX;
        }
        if (background2.x + background2.Background.getWidth() < 0) {
            background2.x = screenX;
        }
        if (flight.isGoingUp) {
            flight.y -= 30 * ScreenRatioY;
        } else {
            flight.y += 30 * ScreenRatioY;
        }
        if (flight.y < 0) {
            flight.y = 0;
        }
        if (flight.y >= screenY - flight.height) {
            flight.y = screenY - flight.height;
        }

        List<Bullet> trash = new ArrayList<>();
        for (Bullet bullet : bullets) {
            if (bullet.x > screenRatioX) {
                trash.add(bullet);
            } else {
                bullet.x += 50 * screenRatioX;
            }

            for (enemyship enemy : enemies) {
                if (Rect.intersects(enemy.collision(), bullet.collision())) {
                    enemy.x = -500;
                    bullet.x = screenX + 500;
                    enemy.wasShot = true;
                    score++;
                }
            }
        }


        for (Bullet bullet : trash) {
            bullets.remove(bullet);
        }
        for (enemyship enemy : enemies) {
            enemy.x -= enemy.speed;
//put int and check
            if ((enemy.x + enemy.width) < 0) {

                if (!enemy.wasShot) {
                    Gameover = true;
                }

                int bound = (int) (30 * screenRatioX);
                enemy.speed = random.nextInt(bound);


                if (enemy.speed < 10 * screenRatioX) {
                    enemy.speed = (int) (10 * screenRatioX);
                }

                enemy.x = screenX;
                enemy.y = random.nextInt(screenY - enemy.height);

                enemy.wasShot = false;


            }
            if (Rect.intersects(enemy.collision(), flight.collision())) {
                Gameover = true;


                return;
            }
        }


    }

    public void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.Background, background1.x, background1.y, Paint);
            canvas.drawBitmap(background2.Background, background2.x, background2.y, Paint);

            canvas.drawText(score+"",screenX/2f,160,Paint);

            for (enemyship enemy : enemies) {
                canvas.drawBitmap(enemy.getenemy(), enemy.x, enemy.y, Paint);

            if (Gameover) {
                playing = false;
                canvas.drawBitmap(flight.Dead(), flight.x, flight.y, Paint);
                getHolder().unlockCanvasAndPost(canvas);

                exit_wait();
                Save_Highscore();
                return;
            }




            }

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, Paint);


            for (Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, Paint);

            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    private void exit_wait() {
        try {
            Thread.sleep(3000);
            activity.startActivity(new Intent(activity,MainActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void Save_Highscore() {
        if(shared.getInt("HighScore",0)<score)
        {
            SharedPreferences.Editor editor=  shared.edit();
            editor.putInt("HighScore", score);
            editor.apply();
        }
    }


    public void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public void resume() {
        playing = true;
        Tobj = new Thread(this);
        Tobj.start();

    }

    public void pause() {
        try {
            playing = false;
            Tobj.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < screenX / 2) {
                    flight.isGoingUp = true;
                }
                break;
            //right side of the screen
            case MotionEvent.ACTION_UP:
                flight.isGoingUp = false;
                if (event.getX() > screenX / 2) {
                    flight.toShoot++;
                    break;
                }
        }
        return true;
    }

    public void newBullet() {
        Bullet bullet = new Bullet(getResources());
        if(!shared.getBoolean("isMute",false))
        {
            sound.play(R.raw.shoot,1,1,0,0,1);

        }
        bullet.x = flight.x;//+ flight.width
        bullet.y = flight.y;
        bullet.x += 50 * screenRatioX;//+ (flight.height / 2)
        bullets.add(bullet);

    }


}
