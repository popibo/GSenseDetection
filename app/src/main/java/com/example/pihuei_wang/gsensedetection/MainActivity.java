package com.example.pihuei_wang.gsensedetection;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "sensor";
    private  SensorManager sm;
    private float [] old_G_vakyes = {0,0,0};
    private float [] new_G_vakyes = {0,0,0};
    public double old_time = System.currentTimeMillis();;
    public double current_time = 0;
    float speed = 0;
    private static final int SHAKE_THRESHOLD = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // use G sensor
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        int sensorType = Sensor.TYPE_ACCELEROMETER; 

        // register
        sm.registerListener(myAccelerometerListener,sm.getDefaultSensor(sensorType),SensorManager.SENSOR_DELAY_NORMAL);
    }

    // set G sensor listener
    final SensorEventListener myAccelerometerListener = new SensorEventListener(){

        long lastUpdate = 0;

        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                Log.i(TAG,"onSensorChanged");

                long curTime = System.currentTimeMillis();
                // update every 100ms.
                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    new_G_vakyes[0] = sensorEvent.values[0];
                    new_G_vakyes[1] = sensorEvent.values[1];
                    new_G_vakyes[2] = sensorEvent.values[2];
                    Log.i(TAG, "\n heading " + new_G_vakyes[0]);
                    Log.i(TAG, "\n pitch " + new_G_vakyes[1]);
                    Log.i(TAG, "\n roll " + new_G_vakyes[2]);

                    String x = String.valueOf(new_G_vakyes[0]);
                    String y = String.valueOf(new_G_vakyes[1]);
                    String z = String.valueOf(new_G_vakyes[2]);

                    // shake rule
                    speed = Math.abs(new_G_vakyes[0] + new_G_vakyes[0] + new_G_vakyes[0] - old_G_vakyes[0] - old_G_vakyes[1] - old_G_vakyes[2]) / diffTime * 10000;

                    // show in list view
                    processview(x, y, z);

                    // base on shake rule to decide close app
                    decisionClose();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.i(TAG, "onAccuracyChanged");
        }
    };

    private void processview(String x, String y, String z){
        final ListView listview = (ListView) findViewById(R.id.listview);
        String[] values ={x,y,z};

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, values);
        listview.setAdapter(adapter);
    }

    private void decisionClose(){

        // if signature shake
        if (speed > SHAKE_THRESHOLD) {

            // pause G sensor listener
            onPause();

            // show alert dialog to notify user
            AlertDialog.Builder d = new AlertDialog.Builder(this);
            String message = getString(R.string.close_app);
            d.setTitle(R.string.close)
                    .setMessage(message);
            d.setPositiveButton(android.R.string.yes,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            closeapp();
                        }
                    });
            d.show();
        }
        current_time = System.currentTimeMillis();
        if ((current_time - old_time) > 1000){
            old_G_vakyes[0] = new_G_vakyes[0];
            old_G_vakyes[1] = new_G_vakyes[1];
            old_G_vakyes[2] = new_G_vakyes[2];
        }
    }

    private void closeapp(){
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    public void onPause(){
        sm.unregisterListener(myAccelerometerListener);
        super.onPause();
    }
}
