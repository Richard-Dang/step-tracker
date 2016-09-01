package step_tracker.uwaterloo.ca.step_tracker;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class MultiSensorEventListener implements SensorEventListener {

    public void onAccuracyChanged (Sensor s, int i){}

    public void onSensorChanged (final SensorEvent se){

        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) { //checks linear acceleration readings from the phone
            StepTracker.linAccelValues = se.values;
        }
        if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            StepTracker.magneticValues = se.values;
        }
        if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            StepTracker.accelValues = se.values;
        }
    }


}
