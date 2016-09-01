package step_tracker.uwaterloo.ca.step_tracker;

/**
 * Created by Richard Dang on 2016-06-19.
 */
public class SteppingStateMachine {
    static int stepCount=0; //counts the number of steps taken
    static FSM_STATE currentState = FSM_STATE.RISING; //keeps track of the current state of the FSM

    public enum FSM_STATE {
    INITIAL, PEAK, RISING, FALLING, THROUGH
    }

    public void checkStepState (float sensorValue[]){
        //if (sensorValue [0] <-1  || sensorValue [0] >1 ||  sensorValue [1] <-1||  sensorValue [1] >1) //checks the invalid range on the X and Z axes to remove false positives such as rotating the phone
          //  currentState = FSM_STATE.INITIAL; //resets FSM
        switch (currentState) { //switches between states when valid Y ranges are passed
            case INITIAL:
                if (sensorValue[2] > 0 && sensorValue[2] < 0.2) {
                    currentState = FSM_STATE.RISING;
                }
                break;
            case RISING:
                if (sensorValue[2] > 0.2 && sensorValue[2] < 0.5) {
                    currentState = FSM_STATE.PEAK;
                }
                break;
            case PEAK:
                if (sensorValue[2] > 0.5 && sensorValue[2] < 1.8) {
                    currentState = FSM_STATE.FALLING;
                }
                break;
            case FALLING:
                if (sensorValue[2] > 0 && sensorValue[2] < 0.5) {
                    currentState = FSM_STATE.THROUGH;
                }
                break;
            case THROUGH:
                if (sensorValue[2] > -1.5 && sensorValue[2] < 0) {
                    currentState = FSM_STATE.INITIAL;
                    Pedometer.displacement();
                    stepCount += 1; //increase the step count by 1 and reset the FSM when full cycle has completed
                }
                break;
        }
    }
}
