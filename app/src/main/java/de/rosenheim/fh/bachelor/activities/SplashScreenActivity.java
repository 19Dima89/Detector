package de.rosenheim.fh.bachelor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import de.rosenheim.fh.bachelor.detector.R;

/**
 * Created by infinity on 10/30/15.
 */
public class SplashScreenActivity extends Activity {

    //Duration of the Splash-Screen
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    //Executed when the Activity is created
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.splash_screen_layout);

        //Handler to start DetectionActivity after the Splash-Screen
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                //This will launch the DetectionActivity
                Intent mainIntent = new Intent(SplashScreenActivity.this, DetectionActivity.class);
                SplashScreenActivity.this.startActivity(mainIntent);
                SplashScreenActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

}
