package step_tracker.uwaterloo.ca.step_tracker;

import android.graphics.PointF;

import java.util.ArrayList;

/**
 * Created by Teng on 10/07/2016.
 */
public class PathAlgo {
    ArrayList<PointF> userPath = new ArrayList<PointF>();
    ArrayList<PointF> nodes = new ArrayList<>();
//E2-3344
    PointF p1 = new PointF(5, 18);
    PointF p2 = new PointF(12, 18);
    PointF p3 = new PointF(20, 18);
    PointF p4 = new PointF(4, 5);
    PointF p5 = new PointF(12, 5);
    PointF p6 = new PointF(20, 5);

    public PathAlgo () {
        nodes.add(p1);
        nodes.add(p2);
        nodes.add(p3);
        nodes.add(p4);
        nodes.add(p5);
        nodes.add(p6);
    }

    public void calculatePath() {
        ArrayList<PointF> path = new ArrayList<PointF>();
        PointF curentUserPoint = StepTracker.mv.getUserPoint();
        PointF secondNode = new PointF();
        PointF thirdNode = new PointF();
        boolean useTwoNodes = false;

        path.add(curentUserPoint); //Always begin by adding in the user's current point, this will ensure the red line will connect to the user's red dot
        if (StepTracker.map.calculateIntersections(curentUserPoint, StepTracker.mv.getDestinationPoint()).size() == 0){
            path.add(StepTracker.mv.getDestinationPoint());
            StepTracker.mv.setUserPath(path);
            userPath = path;
            return;
        }
        for (PointF i : nodes) { //Check each PointF within the node Arraylist
            if ((StepTracker.map.calculateIntersections(curentUserPoint, i).size() == 0) && StepTracker.map.calculateIntersections(i, StepTracker.mv.getDestinationPoint()).size() == 0) {
                secondNode = i;
                path.add(secondNode); //Add the intermediate node to the ArrayList
                path.add(StepTracker.mv.getDestinationPoint()); //Add the destination to the ArrayList
                StepTracker.mv.setUserPath(path); //Connect the dots
                userPath = path;
                return; //Ends the method by only using 1 intermediate node
            }
        }
        for (PointF k : nodes) { //Check each PointF within the node Arraylist
            if (StepTracker.map.calculateIntersections(StepTracker.mv.getDestinationPoint(), k).size() == 0) {
                secondNode = k;
                useTwoNodes = true; //Signifies that 2 intermediate nodes are neccessary to connect the user to destination
                break; //End this loop
            }
        }
        for (PointF j : nodes) { //Check each PointF within the node ArrayList
            if ((useTwoNodes && StepTracker.map.calculateIntersections(secondNode, j).size() == 0)&& StepTracker.map.calculateIntersections(j, StepTracker.mv.getUserPoint()).size() == 0) {
                thirdNode = j;
                path.add(thirdNode); //Add the node closest to the user's position into the ArrayList
                path.add(secondNode); //Add the node closest to the destination into the ArrayList
                path.add(StepTracker.mv.getDestinationPoint()); //Add the destination to the ArrayList
                StepTracker.mv.setUserPath(path); //Connect the dots
                userPath = path;
                return;
            }
        }

    }
}
