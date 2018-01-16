package com.example.pihuei_wang.gsensedetection;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
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

    private static final String msTAG = "sensor";
    private  SensorManager m_sm = null;
    private float [] mf_old_G_vakyes = {0,0,0};
    private float [] mf_new_G_vakyes = {0,0,0};
    public float mf_old_time = System.currentTimeMillis();
    public float mfCurrentTime = 0;
    private float mfspeed = 0;
    private static final int SHAKE_THRESHOLD = 20;
    private int mn_sensorType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "onCreate Start", Toast.LENGTH_LONG).show();

        // use G sensor
        m_sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mn_sensorType = Sensor.TYPE_ACCELEROMETER;
    }

    // set G sensor listener
    final SensorEventListener myAccelerometerListener = new SensorEventListener(){

        long lastUpdate = 0;

        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                Log.i(msTAG,"onSensorChanged");

                long curTime = System.currentTimeMillis();
                // update every 100ms.
                if ((curTime - lastUpdate) > 100) {
                    long diffTime = (curTime - lastUpdate);
                    lastUpdate = curTime;

                    mf_new_G_vakyes[0] = sensorEvent.values[0];
                    mf_new_G_vakyes[1] = sensorEvent.values[1];
                    mf_new_G_vakyes[2] = sensorEvent.values[2];
//                    Log.i(msTAG, "\n heading " + mf_new_G_vakyes[0]);
//                    Log.i(msTAG, "\n pitch " + mf_new_G_vakyes[1]);
//                    Log.i(msTAG, "\n roll " + mf_new_G_vakyes[2]);

                    String x = String.valueOf(mf_new_G_vakyes[0]);
                    String y = String.valueOf(mf_new_G_vakyes[1]);
                    String z = String.valueOf(mf_new_G_vakyes[2]);

                    // shake rule
                    mfspeed = Math.abs(mf_new_G_vakyes[0] + mf_new_G_vakyes[1] + mf_new_G_vakyes[2] - mf_old_G_vakyes[0] - mf_old_G_vakyes[1] - mf_old_G_vakyes[2]);

                    // show in list view
                    processview(x, y, z);

                    // base on shake rule to decide close app
                    decisionClose();
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.i(msTAG, "onAccuracyChanged");
        }
    };

    private void processview(String x, String y, String z){
        final ListView listview = (ListView) findViewById(R.id.listview);
        String[] values ={x,y,z};

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, values);
        listview.setAdapter(adapter);
    }

    private void decisionClose(){

        // if significant shake
        if (mfspeed > SHAKE_THRESHOLD) {

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
        mfCurrentTime = System.currentTimeMillis();
        if ((mfCurrentTime - mf_old_time) > 1000){
            mf_old_G_vakyes[0] = mf_new_G_vakyes[0];
            mf_old_G_vakyes[1] = mf_new_G_vakyes[1];
            mf_old_G_vakyes[2] = mf_new_G_vakyes[2];
        }
    }

    private void closeapp(){
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    @Override
    protected void onStart() {
        Toast.makeText(this, "onStart Start", Toast.LENGTH_LONG).show();
        super.onStart();
    }

    @Override
    protected void onResume() {
        Toast.makeText(this, "onResume Start", Toast.LENGTH_LONG).show();
        // register
        if (m_sm != null) {
            m_sm.registerListener(myAccelerometerListener, m_sm.getDefaultSensor(mn_sensorType), SensorManager.SENSOR_DELAY_NORMAL);
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        Toast.makeText(this, "onStop Start", Toast.LENGTH_LONG).show();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Toast.makeText(this, "onDestroy Start", Toast.LENGTH_LONG).show();
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Toast.makeText(this, "onRestart Start", Toast.LENGTH_LONG).show();
        super.onRestart();
    }

    @Override
    public void onPause(){
        Toast.makeText(this, "onPause Start", Toast.LENGTH_LONG).show();
        m_sm.unregisterListener(myAccelerometerListener);
        super.onPause();
    }
}
