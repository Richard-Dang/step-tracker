package step_tracker.uwaterloo.ca.step_tracker;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import ca.uwaterloo.sensortoy.LineGraphView;
import mapper.MapLoader;
import mapper.MapView;
import mapper.NavigationalMap;

public class StepTracker extends AppCompatActivity {
    LinearLayout l;
    static TextView stepCounterTV;
    static TextView azimuthTV;
    static TextView directionsTV;
    Sensor linearAccelSensor;
    Sensor accelSensor;
    Sensor magneticSensor;
    static LineGraphView filteredGraph;
    static Button resetButton,stepLengthButton;
    static MapView mv;
    static NavigationalMap map;


    static float[] accelValues = new float [3];
    static float [] linAccelValues = new float [3];
    static float[] magneticValues = new float [3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab4_203_11);

        l = (LinearLayout) findViewById(R.id.labels); //text id
        l.setOrientation(LinearLayout.VERTICAL); //set orientation to vertical

        filteredGraph = new LineGraphView(getApplicationContext(),100, Arrays.asList("x","y","z")); //create new line filteredGraph to map accelerometer readings
        l.addView(filteredGraph); //add filteredGraph to layout
        filteredGraph.setVisibility(View.VISIBLE); //set visibility of filteredGraph

        mv = new MapView (getApplicationContext(), 1200, 1200, 45,45);
        registerForContextMenu(mv);

        map = MapLoader.loadMap (getExternalFilesDir(null),"E2-3344.svg");
        mv.setMap(map);
        l.addView (mv);
        mv.setVisibility(View.VISIBLE);



        resetButton = new Button(this); //create new reset step counter button
        resetButton.setText("Reset Step Counter");
        l.addView(resetButton); //add button to layout

        stepLengthButton = new Button(this); //create new reset step counter button
        stepLengthButton.setText("Enter Step Length");
        l.addView(stepLengthButton); //add button to layout


        stepCounterTV = new TextView(getApplicationContext()); //create new step counter textview
        stepCounterTV.setTextColor(Color.BLACK);
        l.addView(stepCounterTV); //add textview to layout

        azimuthTV = new TextView(getApplicationContext());
        azimuthTV.setTextColor(Color.BLACK);
        l.addView (azimuthTV);

        directionsTV = new TextView(getApplicationContext());
        directionsTV.setTextColor(Color.BLACK);
        l.addView (directionsTV);

        SensorManager sensorManager = (SensorManager) getSystemService (SENSOR_SERVICE); //request sensor manager

        SensorEventListener s = new MultiSensorEventListener(); //create new accelerometer sensor event listener

        linearAccelSensor = sensorManager.getDefaultSensor (Sensor.TYPE_LINEAR_ACCELERATION); //get sensor type from sensor manager
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener (s, linearAccelSensor, SensorManager.SENSOR_DELAY_FASTEST); //register sensor listener
        sensorManager.registerListener(s,magneticSensor, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(s,accelSensor, SensorManager.SENSOR_DELAY_FASTEST);

        TimerTask t = new Pedometer(this);
        Timer timer = new Timer();
        timer.schedule(t,0,20);


    }


    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        mv.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected (MenuItem item){
        mv.onContextItemSelected(item);
        mv.setUserPoint(mv.getOriginPoint());
        Pedometer.b.calculatePath();
        Pedometer.userPath= Pedometer.b.userPath;
        directionsTV.setText("");
        Pedometer.directionInstructions();

        return super.onContextItemSelected(item) || mv.onContextItemSelected(item);
    }


}
