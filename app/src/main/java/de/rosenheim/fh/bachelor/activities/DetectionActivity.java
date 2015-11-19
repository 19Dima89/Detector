package de.rosenheim.fh.bachelor.activities;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;
import java.util.List;

import de.rosenheim.fh.bachelor.camera.CameraPreview;
import de.rosenheim.fh.bachelor.threads.CaptureThread;
import de.rosenheim.fh.bachelor.threads.MatcherThread;
import de.rosenheim.fh.bachelor.detector.R;
import de.rosenheim.fh.bachelor.types.ScanObject;

/**
 * Created by Dima-Desktop on 25.10.2015
 */
public class DetectionActivity extends ActionBarActivity{

    /**
     * MatcherThread ID.
     */
    public static final int MATCHER_THREAD = 30989;
    /**
     * CaptureThread ID.
     */
    public static final int CAPTURE_THREAD = 98903;
    /**
     * Camera orientation.
     */
    private final int PREVIEW_AND_PICTURE_ROTATION = 90;
    /**
     * Log tag.
     */
    private final String TAG = "de.rosenheim.fh.tag";
    /**
     * Camera service object.
     */
    private Camera mCamera = null;
    /**
     * Camera preview.
     */
    private CameraPreview mPreview = null;
    /**
     * Background thread, on which the camera service object is opened.
     */
    private CameraHandlerThread cameraThread = null;
    /**
     * Layout container which contains the camera preview.
     */
    private RelativeLayout previewFrame = null;
    /**
     * Text label which displays if anything was detected or not.
     */
    private TextView detectionResult = null;
    /**
     * Application buttons.
     */
    private ImageButton matchButton = null, camButton = null;
    /**
     * List of all captured objects, which serve as the base for the matching operations.
     */
    private List<ScanObject> comparisonObjects = null;

    /**
     * Needed to side-load the OpenCV Manager on startup (required to use OpenCV Library on Android)
     */
    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /**
     * Handles transactions with the various side threads
     */
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);

            if(msg.what == MATCHER_THREAD)//Handles messages from the MatcherThread
            {
                handleMatcherThread(msg);
            }
            else if(msg.what == CAPTURE_THREAD)
            {
                handleCaptureThread(msg);
            }
        }
    };

    /**
     * Gets called every time the Activity is created.
     *
     * @param savedInstanceState        bundle which holds saved data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Side loading the OpenCV Manager to integrate the OpenCV library (Needed to use the library)
        Log.i(TAG, "Trying to load OpenCV library");

        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mOpenCVCallBack))
        {
            Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        else
        {
            Log.i(TAG, "opencv successfull");
            System.out.println(java.lang.Runtime.getRuntime().maxMemory());
        }

        setContentView(R.layout.detection_layout);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Getting the layout in which the camera preview will be stored
        previewFrame = (RelativeLayout) findViewById(R.id.preview_container);
        detectionResult = (TextView) findViewById(R.id.detection_result);
        matchButton = (ImageButton) findViewById(R.id.match_button);
        camButton = (ImageButton) findViewById(R.id.cam_button);

        //Initiating comparisonObjects
        comparisonObjects = new ArrayList<ScanObject>();

        setUpCamera();

    }

    /**
     *Gets called every time the application brought back from the background.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (mCamera == null)
        {
            // Restarting the camera preview and re-initiating mCamera
            setUpCamera();
            this.mPreview.setCamera(mCamera);
        }
    }

    /**
     * Gets called every time before the application is sent into the background.
     */
    @Override
    protected void onPause() {
        super.onPause();

        // Releasing the camera so it can be properly used by other applications
        freeCamera();
    }

    /**
     * Gets called every time before the application is destroyed.
     */
    @Override
    protected void onStop() {
        super.onStop();

        // Releasing the camera so it can be properly used by other applications
        freeCamera();
    }

    /**
     * Gets called when the application is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Releasing the camera so it can be properly used by other applications
        freeCamera();
    }

    /**
     * Will be executed when the match_button is pressed
     *
     * @param view      view, which called this method.
     */
    public void matchAction(View view)
    {
        disableButtons();
        new MatcherThread(mPreview, mHandler, comparisonObjects).start();
    }

    /**
     * Will be executed when the cam_button is pressed
     *
     * @param view      view, which called this method.
     */
    public void captureAction(View view)
    {
        disableButtons();
        new CaptureThread(mPreview, mHandler, this, comparisonObjects).start();
    }

    /**
     * Handles messages from the MatherThread.
     *
     * @param msg       data sent from the MatcherThread (can be anything).
     */
    private void handleMatcherThread(Message msg)
    {
        if(msg.obj==null)
        {
            detectionResult.setText("Nothing detected");
            enableButtons();
            return;
        }

        int index = (Integer)msg.obj;

        if(index == -1)
        {
            detectionResult.setText("Nothing detected");
            enableButtons();
            return;
        }

        detectionResult.setText(comparisonObjects.get(index).getObjectName());
        enableButtons();
    }

    /**
     * Handles messages from the CaptureThread.
     *
     * @param msg       data sent from the CaptureThread (can be anything).
     */
    private void handleCaptureThread(Message msg)
    {
        if(msg.obj==null)
        {
            detectionResult.setText("Nothing captured");
            return;
        }

        boolean index = (boolean)msg.obj;

        if(index)
        {
            detectionResult.setText("Object captured");
        }
    }

    /**
     * Opens and configures the camera of the android device.
     */
    private void setUpCamera()
    {
        try
        {
            newOpenCamera();

            //Get optimal preview size for the layout in which the camera preview will be placed
            List<Camera.Size> mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            Camera.Size mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, 1560, 1760);

            Camera.Parameters params= mCamera.getParameters();
            this.mCamera.setDisplayOrientation(PREVIEW_AND_PICTURE_ROTATION); //Changing camera preview to portrait mode
            params.set("rotation", PREVIEW_AND_PICTURE_ROTATION); //Rotates the taken pictures so that they match the camera preview
            params.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(params); //Setting the camera parameters of the specified camera object

            if(this.mPreview != null)
            {
                this.previewFrame.removeView(this.mPreview);
                this.mPreview = null;
            }

            //Will only get instantiated if mPreview == null, so that it wont be re-instantiated every time onResume() gets called
            if(this.mPreview == null)
            {
                this.mPreview = new CameraPreview(getBaseContext(), this.mCamera); //Instantiating the camera preview
                this.previewFrame.addView(this.mPreview); //Adding the camera preview to a RelativeLayout of activity_main.xml
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Starts a new thread, to open the camera on that thread instead of the main thread.
     */
    private void newOpenCamera() {
        if (cameraThread == null) {
            cameraThread = new CameraHandlerThread();
        }

        synchronized (cameraThread) {
            cameraThread.openCamera();
        }
    }

    /**
     * Opens the camera on the current thread
     */
    private void oldOpenCamera() {
        try
        {
            mCamera = Camera.open();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Background thread on which the camera can be opened, so that onPreviewFrame gets called on a non UI-thread.
     */
    private class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;

        CameraHandlerThread()
        {
            super("CameraHandlerThread");
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened()
        {
            notify();
        }

        void openCamera() {
            mHandler.post(new Runnable()
            {
                @Override
                public void run() {
                    oldOpenCamera();
                    notifyCameraOpened();
                }
            });
            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops and frees the camera object and its preview.
     */
    private void freeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * Determines optimal dimensions of the CameraPreview.
     *
     * @param sizes         list of all available dimensions on this android device.
     * @param w             width of the preview container.
     * @param h             height of the preview container.
     * @return  Camera.Size optimal camera dimensions.
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * Enables all Buttons of the application.
     */
    public void enableButtons()
    {
        this.matchButton.setEnabled(true);
        this.camButton.setEnabled(true);
    }

    /**
     * Disables all Buttons of the application.
     */
    private void disableButtons()
    {
        this.matchButton.setEnabled(false);
        this.camButton.setEnabled(false);
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
