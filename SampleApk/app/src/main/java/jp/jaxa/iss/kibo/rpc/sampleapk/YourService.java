package jp.jaxa.iss.kibo.rpc.sampleapk;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;

import jp.jaxa.iss.kibo.rpc.api.KiboRpcService;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import gov.nasa.arc.astrobee.types.Point;
import gov.nasa.arc.astrobee.types.Quaternion;

import org.opencv.android.Utils;
import org.opencv.aruco.Aruco;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.aruco.Dictionary;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;


import static org.opencv.aruco.Aruco.drawDetectedMarkers;


/**
 * Class meant to handle commands from the Ground Data System and execute them in Astrobee.
 */

public class YourService extends KiboRpcService {

    private final String[] TEMPLATE_FILE_NAME = {
            "coin.png",
            "compass.png",
            "coral.png",
            "crystal.png",
            "diamond.png",
            "emerald.png",
            "fossil.png",
            "key.png",
            "letter.png",
            "shell.png",
            "treasure_box.png"

    };

    private final String[] TEMPLATE_NAME = {
            "coin",
            "compass",
            "coral",
            "crystal",
            "diamond",
            "emerald",
            "fossil",
            "key",
            "letter",
            "shell",
            "treasure_box"

    };




    private final boolean debugging = true;
    private int areaNum = 0;
    private String TAG = this.getClass().getSimpleName();
    Mat[] templates;

    // Object Detection
    // similarity threshold
    private final double threshold = 0.3;
    // duplicates threshold
    private final int length = 10; // Within [length] pixels, only one template image should match.

    @Override
    protected void runPlan1(){

            // The mission starts.
            api.startMission();

            // Move to a point.
            areaNum += 1;
            Point point = new Point(11d, -10.18d, 5.2d);
            Quaternion quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
            api.moveTo(point, quaternion, false);

            //Loading template images

            templates = new Mat[TEMPLATE_FILE_NAME.length];
            for (int i = 0; i < TEMPLATE_FILE_NAME.length; i++) {

                try{

                    //Convert Bitmap to Mat
                    InputStream inputstream = getAssets().open(TEMPLATE_FILE_NAME[i]);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputstream);
                    Mat mat = new Mat();
                    Utils.bitmapToMat(bitmap, mat);

                    // Convert to Grayscale
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY);

                    // Push to the templates array
                    templates[i] = mat;

                    inputstream.close();

                } catch(IOException e){
                    e.printStackTrace();
                }

            }

            // Get a camera image.
            Mat area1 = api.getMatNavCam();
            api.saveMatImage(area1, "Area1");

            Log.i(TAG, "Running object detection:");
            detectItems(processImage(area1));
            Log.i(TAG, "Object detection finished:");

//            point = new Point(10.42d, -10.58d, 4.82d);
//            quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
//            api.moveTo(point, quaternion, false);
//            /* ******************************************************************************** */
//            /* Write your code  to recognize the type and number of landmark items in each area! */
//            /* If there is a treasure item, remember it.                                        */
//            /* ******************************************************************************** */
//            point = new Point(10.3d, -9.254d, 3.76203d);
//            quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
//            api.moveTo(point, quaternion, false);
//            // When you recognize landmark items, let’s set the type and number.
////        api.setAreaInfo(1, "item_name", 1);
//            point = new Point(10.3d, -8.4d, 3.76203d);
//            quaternion = new Quaternion(0f, 0f, -0.707f, 0.707f);
//            api.moveTo(point, quaternion, false);
//            /* **************************************************** */
//            /* Let's move to each area and recognize the items. */
//            /* **************************************************** */
//
            // When you move to the front of the astronaut, report the rounding completion.
            point = new Point(10.9d, -9.92284d, 5.195d);
            quaternion = new Quaternion(0f, 0f, 0.707f, 0.707f);
            api.moveTo(point, quaternion, false);




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


    private Mat processImage(Mat image) {

        Log.i(TAG, "Undistorting the image...");
        Dictionary dictionary = Aruco.getPredefinedDictionary(Aruco.DICT_5X5_250);
        Mat ids = new Mat();
        ArrayList<Mat> corners = new ArrayList<>();
        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        Mat cameraCoefficients = new Mat(1, 5, CvType.CV_64F);;
        Mat undistortedImage = new Mat();

        try {
            // Get camera Matrix

            cameraMatrix.put(0, 0, api.getNavCamIntrinsics()[0]);

            // Get lens distortion parameters

            cameraCoefficients.put(0, 0, api.getNavCamIntrinsics()[1]);
            cameraCoefficients.convertTo(cameraCoefficients, CvType.CV_64F);

            // Undistort the image

            Calib3d.undistort(image, undistortedImage, cameraMatrix, cameraCoefficients);

            api.saveMatImage(undistortedImage, "_undistorted");
        } catch(Exception error){
            Log.i(TAG, "There is an " + error + " error when undistorting the image.");
        }
        try {

       /* This method will use the image and dictionary provided and
       return a list called corners containing 4 x,y coordinates of the corners of the
       Aruco tags, along with ids, a list of all Aruco Ids detected. */

            Aruco.detectMarkers(image, dictionary, corners, ids);
        } catch(Exception error){
            Log.i(TAG, "There is an " + error + " error before or at the detect markers method.");
        }

      if (ids.empty()){
          return image;
      }

        Mat rvecs = new Mat();
        Mat tvecs = new Mat();
        Mat rot_trans_matrix = new Mat(2, 3, CvType.CV_64FC1);



        Aruco.estimatePoseSingleMarkers(corners, 0.05f, cameraMatrix, cameraCoefficients, rvecs, tvecs);
        Log.i(TAG, "Returning the undistorted image...");

        Calib3d.Rodrigues(rvecs, rvecs);
        for (int r = 0; r < 2; r++){
            for (int c = 0; c < 3; c++) {
                rot_trans_matrix.put(r, c, rvecs.get(r, c));
            }
        }


        double c = rot_trans_matrix.get(1, 0)[0];
        double a = rot_trans_matrix.get(0, 0)[0];

        double angle = Math.atan2(c, a);
        undistortedImage = rotateImg(undistortedImage, Math.toDegrees(angle));

        corners = new ArrayList<Mat>();
        ids = new Mat();

        // Refresh the corners list for the rotated image.
        Aruco.detectMarkers(undistortedImage, dictionary, corners, ids);

        Mat cornerMat = corners.get(0); // Get the first marker's corners
        org.opencv.core.Point[] markerCorners = new org.opencv.core.Point[4]; // ArUco has 4 corners

        for (int i = 0; i < 4; i++) {
            double[] data = cornerMat.get(0, i); // get(row=0, col=i)
            markerCorners[i] = new org.opencv.core.Point(data[0], data[1]);
        }

        markerCorners = orderCorners(markerCorners);

        double pixelLength = Math.sqrt(
                Math.pow(markerCorners[0].x - markerCorners[1].x, 2) +
                        Math.pow(markerCorners[0].y - markerCorners[1].y, 2)
        );

        double pixelPerCM = pixelLength / 5;
        double x1 = -22; // cm relative to the top left corner of the ArUco tag
        double y1 = 2; // cm
        double x2 = 7; // cm
        double y2 = -10; // cm
        int x1px = (int) (x1 * pixelPerCM); // pixels relative to the top left corner of the ArUco tag
        int y1px = (int) (y1 * pixelPerCM); // pixels
        int x2px = (int) (x2 * pixelPerCM); // pixels
        int y2px = (int) (y2 * pixelPerCM); // pixels

        org.opencv.core.Point startPoint = new org.opencv.core.Point((int) (markerCorners[0].x + x1px), (int) (markerCorners[0].y + y1px));
        org.opencv.core.Point endPoint = new org.opencv.core.Point((int) (markerCorners[0].x + x2px), (int) (markerCorners[0].y + y2px));

        Rect cropRect = new Rect(startPoint, endPoint);
        undistortedImage = new Mat(undistortedImage, cropRect);





        api.saveMatImage(undistortedImage, "Rotated_Undistorted.png");



        return undistortedImage;


    }

    // image must already be undistorted before running this method!!!!
    private void detectItems(Mat img){
        Log.i(TAG, "Detecting the objects...");
        // Matches for each template
        int templateMatchCnt[] = new int[templates.length];

        for (int i = 0; i < templates.length; i++) {

            int matchCnt = 0;
            // Coordinates of matched locations
            List<org.opencv.core.Point> matches = new ArrayList<>();


            // Loading Template and Target Image
            Mat template = templates[i].clone();
            Mat targetImage = img.clone();

            // Parameters for matching the templates
            int widthMin = 50; //[px]
            int widthMax = 125; //[px]
            int changeWidth = 5; //[px]
            int changeAngle = 45; //[px]

            for (int size = widthMin; size <= widthMax; size += changeWidth){
                for (int angle = 0; angle < 360; angle += changeAngle) {
                    Mat resizedTemp = resizeImg(template, size);
                    Mat rotResizedTemp = rotateImg(resizedTemp, angle);

                    Mat result = new Mat();
                    Imgproc.matchTemplate(targetImage, rotResizedTemp, result, Imgproc.TM_CCOEFF_NORMED);
                    Log.i(TAG, result.dump());
                    // Get coordinates with similarity rating and compare them to a threshold.
                    Core.MinMaxLocResult mmlr = Core.minMaxLoc(result);
                    double maxVal = mmlr.maxVal;
                    Log.i(TAG, String.valueOf(maxVal));
                    if (maxVal >= threshold) {
                        // Extract results greater than or equal to the similarity threshold
                        Mat thresholdedResult = new Mat();
                        Imgproc.threshold(result, thresholdedResult, threshold, 1.0, Imgproc.THRESH_TOZERO);

                        // Get coordinates of the matches
                        Log.i(TAG, thresholdedResult.dump());
                        for (int y = 0; y < thresholdedResult.rows(); y++){
                            for (int x = 0; x < thresholdedResult.cols(); x++){
                                try {
                                    if (thresholdedResult.get(y, x)[0] > 0) {
                                        matches.add(new org.opencv.core.Point(x, y));
                                    }
                                } catch (Exception err) {

                                }
                            }

                        }

                    }
                }



            }


            //Avoid detecting the same template multiple times
            List<org.opencv.core.Point> filteredMatches = removeDuplicates(matches);
            matchCnt += filteredMatches.size();

            // Number of matches for each template
            templateMatchCnt[i] = matchCnt;
//            int mostMatchTemplateNum = getMaxIndex(templateMatchCnt);
            api.setAreaInfo(areaNum, TEMPLATE_NAME[i], templateMatchCnt[i]);

        }



    }

    private double calculateDistance(org.opencv.core.Point p1, org.opencv.core.Point p2){
        double dx = Math.abs(p1.x - p2.x);
        double dy = Math.abs(p1.y - p2.y);

        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }

    private List<org.opencv.core.Point> removeDuplicates(List<org.opencv.core.Point> points){
        List<org.opencv.core.Point> filteredList = new ArrayList<>();

        for (org.opencv.core.Point point : points) {
            boolean isIncluded = false;
            for (org.opencv.core.Point checkPoint :filteredList){
                double distance = calculateDistance(point, checkPoint);

                if (distance <= length) {
                    isIncluded = true;
                    break;
                }
            }

            if (!isIncluded){
                filteredList.add(point);
            }
        }

        return filteredList;
    }

    private Mat resizeImg(Mat img, int width){
        int height = (int) (img.rows() * ((double) width / img.cols()));
        Mat resizedImg = new Mat();
        Imgproc.resize(img, resizedImg, new Size(width,height));

        return resizedImg;
    }

    private Mat rotateImg(Mat src, double angle) {
        // Get the center of the image
        org.opencv.core.Point center = new org.opencv.core.Point(src.cols() / 2.0, src.rows() / 2.0);

        // Compute rotation matrix
        Mat rotationMat = Imgproc.getRotationMatrix2D(center, angle, 1.0);

        // Compute the bounding box of the rotated image
        double absCos = Math.abs(rotationMat.get(0, 0)[0]);
        double absSin = Math.abs(rotationMat.get(0, 1)[0]);

        int newWidth = (int) (src.height() * absSin + src.width() * absCos);
        int newHeight = (int) (src.height() * absCos + src.width() * absSin);

        // Adjust rotation matrix to move the image center to the new bounding box center
        rotationMat.put(0, 2, rotationMat.get(0, 2)[0] + newWidth / 2.0 - center.x);
        rotationMat.put(1, 2, rotationMat.get(1, 2)[0] + newHeight / 2.0 - center.y);

        // Perform the actual rotation
        Mat rotated = new Mat();
        Imgproc.warpAffine(src, rotated, rotationMat, new Size(newWidth, newHeight), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, Scalar.all(0));

        return rotated;
    }

    private int getMaxIndex(int[] array) {
        int max = 0;
        int maxIndex = 0;

        // Find the max index of an array
        for (int i = 0; i < array.length; i++){
            if (array[i] > max){
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private void drawMarkers(Mat img, Dictionary dict, ArrayList<Mat> corners, Mat ids){
        Mat arImage = img.clone();
        drawDetectedMarkers(arImage, corners, ids, new Scalar(0, 0, 255));
        api.saveMatImage(arImage, "area" + areaNum + ".png");
    }
    private org.opencv.core.Point[] orderCorners(org.opencv.core.Point[] pts) {
        org.opencv.core.Point[] ordered = new org.opencv.core.Point[4];

        // Sum and diff of points
        double[] sums = new double[4];
        double[] diffs = new double[4];

        for (int i = 0; i < 4; i++) {
            sums[i] = pts[i].x + pts[i].y;
            diffs[i] = pts[i].y - pts[i].x;
        }

        // Top-left = min sum
        // Bottom-right = max sum
        // Top-right = min diff
        // Bottom-left = max diff
        ordered[0] = pts[minIndex(sums)]; // top-left
        ordered[2] = pts[maxIndex(sums)]; // bottom-right
        ordered[1] = pts[minIndex(diffs)]; // top-right
        ordered[3] = pts[maxIndex(diffs)]; // bottom-left

        return ordered;
    }
    private int minIndex(double[] arr) {
        int idx = 0;
        for (int i = 1; i < arr.length; i++) if (arr[i] < arr[idx]) idx = i;
        return idx;
    }

    private int maxIndex(double[] arr) {
        int idx = 0;
        for (int i = 1; i < arr.length; i++) if (arr[i] > arr[idx]) idx = i;
        return idx;
    }

}