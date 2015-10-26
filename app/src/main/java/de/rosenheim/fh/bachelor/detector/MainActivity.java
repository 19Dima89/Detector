package de.rosenheim.fh.bachelor.detector;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import java.util.List;

import de.rosenheim.fh.bachelor.camera.CameraPreview;


public class MainActivity extends ActionBarActivity{

    //Local variables
    private final int PREVIEW_AND_PICTURE_ROTATION = 90;
    private Camera mCamera = null;
    private CameraPreview mPreview = null;
    private CameraHandlerThread cameraThread = null;
    private RelativeLayout previewFrame = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sapui5_layout);

        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Getting the layout in which the camera preview will be stored
        previewFrame = (RelativeLayout) findViewById(R.id.preview_container);

        setUpCamera();

    }

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

    //Starts a new thread and opens the camera on that thread
    private void newOpenCamera() {
        if (cameraThread == null) {
            cameraThread = new CameraHandlerThread();
        }

        synchronized (cameraThread) {
            cameraThread.openCamera();
        }
    }

    private void oldOpenCamera() {
        try
        {
            mCamera = Camera.open();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    //Opens the camera of the smart-phone in a separate thread, so that onPreviewFrame gets called on a non UI-thread
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

    // This method will stop the camera preview and release the camera object
    private void freeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

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
