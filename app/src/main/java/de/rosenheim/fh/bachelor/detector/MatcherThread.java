package de.rosenheim.fh.bachelor.detector;

import android.graphics.Bitmap;
import android.os.Handler;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.util.List;

import de.rosenheim.fh.bachelor.camera.CameraPreview;
import de.rosenheim.fh.bachelor.types.ScanObject;

/**
 * Created by Dima-Desktop on 28.10.2015.
 */
public class MatcherThread extends Thread {

    //Local variables
    private final int DETECTION_THRESHOLD = 50;
    private CameraPreview preview = null;
    private Handler mHandler = null;
    private List<ScanObject> comparisonObjects = null;
    private Bitmap fetchedFrame = null;

    //Constructor
    public MatcherThread(CameraPreview preview, Handler mHandler, List<ScanObject> comparisonObjects)
    {
        this.preview = preview;
        this.mHandler = mHandler;
        this.comparisonObjects = comparisonObjects;
    }

    //Gets a frame from the CameraPreview and compares the frame to all the saved frames in comparisonObjects
    public void run()
    {
        fetchedFrame = UtilityClass.fetchRawFrameData(this.preview.getRawPreviewData(), this.preview.getCamera().getParameters().getPreviewSize());

        mHandler.sendMessage(mHandler.obtainMessage(DetectionActivity.MATCHER_THREAD, detectShape(fetchedFrame)));
    }

    //Returns a positive integer if a valid shape is detected ( can be used as an index of comparisonObjects ), or -1 if no shape was detected
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
            Mat descriptorsCapturedImage = new Mat();
            MatOfKeyPoint keypointsCapturedImage = new MatOfKeyPoint();

            //Getting the keypoints
            detector.detect(capturedImage, keypointsCapturedImage);
            descriptor.compute(capturedImage, keypointsCapturedImage, descriptorsCapturedImage);

            //Matching the keypoints of the captured Image with the keypoints of the ScanObjects in comparisonObjects
            for(int i=0;i < comparisonObjects.size(); i++)
            {
                Mat compareImage = new Mat();
                Utils.bitmapToMat(comparisonObjects.get(i).getReferencePicture(), compareImage);
                Mat descriptorsCompareImage = new Mat();
                MatOfKeyPoint keypointsCompareImage = new MatOfKeyPoint();

                detector.detect(compareImage, keypointsCompareImage);
                descriptor.compute(compareImage, keypointsCompareImage, descriptorsCompareImage);

                matcher.match(descriptorsCapturedImage, descriptorsCompareImage, matches);

                filteredMatches = UtilityClass.filterMatchesByDistance(matches).size().height;

                if(filteredMatches > highestMatchCount)
                {
                    highestMatchCount = filteredMatches;
                    highestMatchCountIndex = i;
                }
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
