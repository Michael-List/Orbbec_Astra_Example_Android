package com.example.michi.myapplication;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orbbec.astra.Vector3D;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private OrbbecCamAndroid orbbecCamAndroid;
    private Button getDataButton;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        // Initialize OrbbecCam only if the app is not running in an emulator
        if (!isEmulator()) {
            orbbecCamAndroid = new OrbbecCamAndroid(getApplicationContext(), 640, 480);
        }

        getDataButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getData();
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private void getData() {
        new Thread(new Runnable() {
            public void run() {
                ArrayList<Vector3D> vector3DList = orbbecCamAndroid.get3DVectors();

                Log.e("DATA", "x of 200th vector: " + vector3DList.get(200).getX());
                Log.e("DATA", "y of 200th vector: " + vector3DList.get(200).getY());
                Log.e("DATA", "z of 200th vector: " + vector3DList.get(200).getZ());
                Log.e("DATA", "size of list: " + vector3DList.size());
            }
        }).start();
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
