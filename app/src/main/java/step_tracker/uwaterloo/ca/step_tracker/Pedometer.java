package step_tracker.uwaterloo.ca.step_tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.hardware.SensorManager;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import mapper.VectorUtils;

/**
 * Created by Richard Dang on 2016-07-01.
 */
public class Pedometer extends TimerTask{
    float[] filteredLinAccelValues = new float [3];
    static Activity lab;
    float[] R = new float[9];
    float[] I = new float[9];
    static float[] orientationValues = new float[3];
    static String direction ="";
    static float xDisplacement = 0;
    static float yDisplacement = 0;
    static float xDisplacementTemp = 0;
    static float yDisplacementTemp = 0;
    static float stepLength = 0.8f;
    static PointF currentPoint = StepTracker.mv.getOriginPoint();;
    static PointF nextPoint;
    static List<PointF> userPath = new ArrayList<PointF>();
    SteppingStateMachine stateMachine = new SteppingStateMachine();
    static PathAlgo b = new PathAlgo();


    BufferedWriter out = null;
    File sensorFile;

    public Pedometer (Activity lab){
        this.lab = lab;
    }



    public void run() {
        resetValuesOnClick();
        stepLengthOnClick();

        filteredLinAccelValues = lowPassFilter(StepTracker.linAccelValues, filteredLinAccelValues);
        stateMachine.checkStepState(filteredLinAccelValues);
        recordAccelerometerData(filteredLinAccelValues);
        if (SensorManager.getRotationMatrix(R, I, StepTracker.accelValues, StepTracker.magneticValues)) {
            SensorManager.getOrientation(R, orientationValues);
            direction = directionConversion(orientationValues[0]);
        }
        lab.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                StepTracker.stepCounterTV.setText("Step Count: "+SteppingStateMachine.stepCount); //displays the current number of steps taken
                StepTracker.filteredGraph.addPoint(StepTracker.linAccelValues); //add accelerometer readings to filteredGraph
                StepTracker.azimuthTV.setText(String.format("Azimuth: %.4f\nDirection: %s \nNorth: %.2fm \nEast: %.2fm", orientationValues[0],direction,yDisplacement,xDisplacement));
            }
        });
    }

    public static void displacement (){

        xDisplacementTemp=(float)Math.sin(orientationValues[0])*stepLength;
        yDisplacementTemp=(float)Math.cos(orientationValues[0])*stepLength;

        if (!(StepTracker.mv.getDestinationPoint().equals(new PointF(0,0))) && recalculatePosition(xDisplacementTemp,-yDisplacementTemp)){
            xDisplacement=xDisplacementTemp;
            yDisplacement=yDisplacementTemp;
        }

    }

    static public void directionInstructions (){
            float xDiff;
            float yDiff;
            String NS ="";
            String EW = "";

            if (userPath.size()!=0) {
                xDiff = userPath.get(1).x - StepTracker.mv.getUserPoint().x;
                yDiff = userPath.get(1).y - StepTracker.mv.getUserPoint().y;
                if (xDiff < 0)
                    EW = "WEST";
                else if (xDiff > 0)
                    EW = "EAST";

                if (yDiff < 0)
                    NS = "NORTH";
                else if (yDiff > 0)
                    NS = "SOUTH";

                StepTracker.directionsTV.setText("Move " + Math.round(Math.abs(xDiff)/stepLength) + " steps " + EW + " and " + Math.round(Math.abs(yDiff)/stepLength) + " steps " + NS);
            }
        }

    public String directionConversion (float rotationValue){
        String direction = "";
        if(rotationValue <= Math.PI/8 && rotationValue>=-Math.PI*1/8)
            direction = "NORTH";
        else if (rotationValue > Math.PI/8 && rotationValue<Math.PI*3/8)
            direction = "NORTH EAST";
        else if(rotationValue >= Math.PI*3/8 && rotationValue<=Math.PI*5/8)
            direction = "EAST";
        else if(rotationValue > Math.PI*5/8 && rotationValue<Math.PI*7/8)
            direction = "SOUTH EAST";
        else if (rotationValue >= Math.PI*7/8 &&rotationValue<=Math.PI || rotationValue <= -Math.PI*7/8 &&rotationValue>=-Math.PI)
            direction = "SOUTH";
        else if (rotationValue<-Math.PI*5/8&& rotationValue>-Math.PI*7/8)
            direction = "SOUTH WEST";
        else if (rotationValue > -Math.PI*3/8 && rotationValue<-Math.PI/8)
            direction = "NORTH WEST";
        else if(rotationValue >= -Math.PI*5/8 && rotationValue<=-Math.PI*3/8)
            direction = "WEST";
        else
            direction = "Please keep turning";
        return direction;
    }

    public void resetValuesOnClick (){
        StepTracker.resetButton.setOnClickListener(
                new View.OnClickListener() { //create anonymous object of class Button that implements OnClickListener
                    public void onClick (View v) { //button callback method
                        StepTracker.filteredGraph.purge();
                        SteppingStateMachine.stepCount =0;

                        if (sensorFile.exists()){ //delete the data file if it exists
                            sensorFile.delete();
                        }
                    }
                }
        );
    }
    
    public void stepLengthOnClick () {
        StepTracker.stepLengthButton.setOnClickListener(
                new View.OnClickListener() { //create anonymous object of class Button that implements OnClickListener
                    public void onClick (View v) { //button callback method
                        AlertDialog.Builder builder = new AlertDialog.Builder(lab);
                        builder.setTitle("Enter Step Length");
                
                        final EditText input = new EditText(lab);
                
                        input.setInputType(InputType.TYPE_CLASS_PHONE);
                        builder.setView(input);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stepLength = Float.parseFloat(input.getText().toString());
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                }
        );
    }

    static public boolean recalculatePosition (float xD, float yD){
        nextPoint = new PointF(currentPoint.x+xD,currentPoint.y+yD);

        lab.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (VectorUtils.distance(StepTracker.mv.getUserPoint(), StepTracker.mv.getDestinationPoint()) <= stepLength){
                    Toast.makeText(lab, "You have reached your destination", Toast.LENGTH_LONG).show();
                }
            }
        });



        if (StepTracker.map.calculateIntersections(currentPoint, nextPoint).size() == 0) {
            lab.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StepTracker.mv.setUserPoint(nextPoint);

                    b.calculatePath();
                    userPath= b.userPath;
                    StepTracker.directionsTV.setText("");
                    directionInstructions();

                }
            });
            currentPoint = nextPoint;

            return true;
        }
        return false;

    }

    public void recordAccelerometerData (float[] data){
        try { //Stores the stepping data onto the phone
            File root = new File(Environment.getExternalStorageDirectory(), "SensorData");
            if (!root.exists()) {
                root.mkdirs();
            }
            sensorFile = new File(root, "SENSOR_DATA.txt");
            out = new BufferedWriter(new FileWriter(sensorFile,true));
            out.write(String.format("%.5f %.5f %.5f \n",data[0],data[1],data[2]));
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public float[] lowPassFilter (float values[], float filteredValues[]){
        for (int x= 0;x<filteredValues.length;x++) //smoothes out the data using a basic low-pass filter to remove bias/noise
        {
            filteredValues[x] += (values[x] - filteredValues[x])/5;
        }
        return filteredValues;
    }
}
