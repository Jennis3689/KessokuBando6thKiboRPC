package jp.jaxa.iss.kibo.rpc.sampleapk;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.util.ArrayList;
import java.util.list;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.core.Mat;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;


/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

public class YourService extends KiboRpcService {

    private final boolean debugging = true;

    @Override
    protected void runPlan1(){
        // The mission starts.
        api.startMission();

        // Move to a point.
        Point point = new Point(10.9d, -9.92284d, 5.195d);
        Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
        api.moveTo(point, quaternion, false);

        // Get a camera image.
        Mat image = api.getMatNavCam();
        saveMatImage(image, "Area1");

        /* ******************************************************************************** */
        /* Write your code to recognize the type and number of landmark items in each area! */
        /* If there is a treasure item, remember it.                                        */
        /* ******************************************************************************** */

        // When you recognize landmark items, let’s set the type and number.
//        api.setAreaInfo(1, "item_name", 1);

        /* **************************************************** */
        /* Let's move to each area and recognize the items. */
        /* **************************************************** */

        // When you move to the front of the astronaut, report the rounding completion.
//        point = new Point(11.143d, -6.7607d, 4.9654d);
//        quaternion = new Quaternion(0f, 0f, 0.707f, 0.707f);
//        api.moveTo(point, quaternion, false);
//        api.reportRoundingCompletion();


        /* ********************************************************** */
        /* Write your code to recognize which target item the astronaut has. */
        /* ********************************************************** */

        // Let's notify the astronaut when you recognize it.
        api.notifyRecognitionItem();

        /* ******************************************************************************************************* */
        /* Write your code to move Astrobee to the location of the target item (what the astronaut is looking for) */
        /* ******************************************************************************************************* */

        // Take a snapshot of the target item.
        api.takeTargetItemSnapshot();
    }

    @Override
    protected void runPlan2(){
       // write your plan 2 here.
    }

    @Override
    protected void runPlan3(){
        // write your plan 3 here.
    }

    // You can add your method.
    private String processImage(Mat image) {

       // Detect ArUco tags in the image. 
       // This dictionary is of 6 pixel by 6 pixel AR Tags with 250 unique tags
       Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_6X6_250);

       Mat ids = new Mat();

       Mat corners = new Mat();

       /* This method will use the image and dictionary provided and
       return a list called corners containing 4 x,y coordinates of the corners of the
       Aruco tags, along with ids, a list of all Aruco Ids detected. */

       Aruco.detectMarkers(image, dictionary, corners, ids);

        if (debugging){
            drawMarkers(image, dictionary, corners, ids);
        }

       

        // Get camera Matrix
        Mat cameraMatrix = new mat(3, 3, CvType.CV_64F);
        cameraMatrix.put(0, 0, 1.0, api.getNavCamIntrinsics()[1]);

       // Get lens distortion parameters
        Mat cameraCoefficients = new Mat(1, 5, CvType.CV_64F);
        cameraCoefficients.put(0, 0, api.getNavCamIntrinsics()[1]);
        cameraCoefficients.convertTo(cameraCoefficients, CvType.CV_64F);

        // Undistort the image
        Mat undistortedImage = new Mat();
        Calib3d.undistort(image, undistortedImage, cameraMatrix, cameraCoefficients);

        saveMatImage(undistortedImage, "_undistorted");


    }

    private void drawMarkers(img, dict, corners, ids){
        private static Mat arImage = new Mat();
        drawDetectedMarkers(arImage, corners, ids);
        saveMatImage(arImage, "ArucoImage");
    }