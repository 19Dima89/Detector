package de.rosenheim.fh.bachelor.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Dieter Schneider on 25.10.15.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{


    //Local variables
    private Camera mCamera = null;
    private SurfaceHolder mHolder = null;
    private byte[] rawPreviewData = null;

    //Constructor
    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try
        {
            this.mCamera.setPreviewDisplay(this.mHolder);
            this.mCamera.startPreview();
            setFocusMode();
            this.mCamera.setPreviewCallback(this);
            return;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if(this.mHolder.getSurface() == null)
        {
            return;
        }

        try
        {
            this.mCamera.stopPreview();

            this.mCamera.setPreviewDisplay(this.mHolder);
            this.mCamera.startPreview();
            setFocusMode();
            this.mCamera.setPreviewCallback(this);
            return;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        //TODO

    }

    //Gets a single frame from the camera
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        this.rawPreviewData = data;

    }

    //Focuses the camera continuously
    public void setFocusMode()
    {
        Camera.Parameters parameters = this.mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        this.mCamera.setParameters(parameters);
    }

    //Getters/Setters
    public byte[] getRawPreviewData()
    {
        return this.rawPreviewData;
    }

    public Camera getCamera()
    {
        return this.mCamera;
    }

    public void setCamera(Camera camera)
    {
        this.mCamera = camera;
    }
 }
