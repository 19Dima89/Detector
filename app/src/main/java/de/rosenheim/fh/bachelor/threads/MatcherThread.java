package de.rosenheim.fh.bachelor.threads;

import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

import de.rosenheim.fh.bachelor.activities.DetectionActivity;
import de.rosenheim.fh.bachelor.camera.CameraPreview;
import de.rosenheim.fh.bachelor.detector.UtilityClass;
import de.rosenheim.fh.bachelor.types.ScanObject;

/**
 * Created by Dima-Desktop on 28.10.2015.
 */
public class MatcherThread extends Thread {

    /**
     * Defines how much matching keypoints need to be found for a positive result.
     */
    private final int DETECTION_THRESHOLD = 50;
    /**
     * The camera preview.
     */
    private CameraPreview preview = null;
    /**
     * The handler instance to the main thread.
     */
    private Handler mHandler = null;
    /**
     * A list of all the present reference pictures.
     */
    private List<ScanObject> comparisonObjects = null;
    /**
     * The frame that needs to get matched.
     */
    private Bitmap fetchedFrame = null;

    private boolean drawMatchesFlag = false, drawFilteredMatchesFlag = false, drawKeypointsFlag = false;

    /**
     * Instantiates a new MatcherThread instance.
     *
     * @param preview               camera preview.
     * @param mHandler              handler to communicate with the main thread.
     * @param comparisonObjects     list of all reference pictures.
     */
    public MatcherThread(CameraPreview preview, Handler mHandler, List<ScanObject> comparisonObjects, boolean drawMatchesFlag, boolean drawFilteredMatchesFlag, boolean drawKeypointsFlag)
    {
        this.preview = preview;
        this.mHandler = mHandler;
        this.comparisonObjects = comparisonObjects;
        this.drawMatchesFlag = drawMatchesFlag;
        this.drawFilteredMatchesFlag = drawFilteredMatchesFlag;
        this.drawKeypointsFlag = drawKeypointsFlag;
    }

    /**
     * Logic of the thread (Gets a frame from the CameraPreview matches it with the help of
     * detectShape and informs the main thread if anything is found.)
     */
    public void run()
    {
        fetchedFrame = UtilityClass.fetchRawFrameData(this.preview.getRawPreviewData(), this.preview.getCamera().getParameters().getPreviewSize());

        mHandler.sendMessage(mHandler.obtainMessage(DetectionActivity.MATCHER_THREAD, detectShape(fetchedFrame)));
    }

    /**
     * Compares a frame with all the saved frames in comparisonObjects.
     *
     * @param targetBitmap          Bitmap which needs to get matched.
     * @return  int                 Returns a positive integer if a valid shape is detected ( can be used as an index of
     *                              comparisonObjects ), or -1 if no shape was detected.
     */
    private int detectShape(Bitmap targetBitmap)
    {
        if(targetBitmap != null)
        {
            double filteredMatches = 0;
            double highestMatchCount = 0;
            int highestMatchCountIndex = 0;

            //Setting the FeatureDetector, DescriptorExtractor and DescriptorMatcher algorithms
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
            DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

            //Preparation to get the keypoints of the captured Image
            MatOfDMatch matches = new MatOfDMatch();
            Mat capturedImage = new Mat();
            Utils.bitmapToMat(targetBitmap, capturedImage);
            Imgproc.cvtColor(capturedImage, capturedImage, Imgproc.COLOR_RGBA2GRAY);
            Mat descriptorsCapturedImage = new Mat();
            MatOfKeyPoint keypointsCapturedImage = new MatOfKeyPoint();

            //Getting the keypoints
            detector.detect(capturedImage, keypointsCapturedImage);
            descriptor.compute(capturedImage, keypointsCapturedImage, descriptorsCapturedImage);

            //Preperation to get the keypoints of the coparison Images
            Mat compareImage = new Mat();
            Mat descriptorsCompareImage = new Mat();
            MatOfKeyPoint keypointsCompareImage = new MatOfKeyPoint();
            String nameComparisonObject = null;

            //Matching the keypoints of the captured Image with the keypoints of the ScanObjects in comparisonObjects
            for(int i=0;i < comparisonObjects.size(); i++)
            {
                Utils.bitmapToMat(comparisonObjects.get(i).getReferencePicture(), compareImage);
                Imgproc.cvtColor(compareImage, compareImage, Imgproc.COLOR_RGBA2GRAY);

                detector.detect(compareImage, keypointsCompareImage);
                descriptor.compute(compareImage, keypointsCompareImage, descriptorsCompareImage);

                matcher.match(descriptorsCapturedImage, descriptorsCompareImage, matches);

                filteredMatches = UtilityClass.filterMatchesByDistance(matches).size().height;

                if(filteredMatches > highestMatchCount)
                {
                    nameComparisonObject = comparisonObjects.get(i).getObjectName();
                    highestMatchCount = filteredMatches;
                    highestMatchCountIndex = i;
                }
            }

            if(drawMatchesFlag)
            {
                UtilityClass.drawMatchesAndSaveThem(capturedImage, keypointsCapturedImage, compareImage, keypointsCompareImage, matches, nameComparisonObject, false);
            }

            if(drawFilteredMatchesFlag)
            {
                UtilityClass.drawMatchesAndSaveThem(capturedImage, keypointsCapturedImage, compareImage, keypointsCompareImage, UtilityClass.filterMatchesByDistance(matches), nameComparisonObject, true);
            }

            if(drawKeypointsFlag)
            {
                UtilityClass.drawKeypointsAndSaveThem(capturedImage, keypointsCapturedImage, nameComparisonObject);
            }

            //If highestMatchCount is larger then DETECTION_THRESHOLD, a valid match has been found
            if(highestMatchCount > DETECTION_THRESHOLD)
            {
                return highestMatchCountIndex;
            }
            else
            {
                //If highestMatchCountIndex is smaller then DETECTION_THRESHOLD, no match has been found
                return -1;
            }
        }
        else
        {
            //If targetBitmap is null, no match has been found
            return -1;
        }
    }
}
