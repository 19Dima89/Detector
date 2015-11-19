package de.rosenheim.fh.bachelor.camera;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Dieter Schneider on 25.10.15.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{


    /**
     * The Camera service object.
     */
    private Camera mCamera = null;
    /**
     * The SurfaceHolder of the camera preview.
     */
    private SurfaceHolder mHolder = null;
    /**
     * The raw preview frame
     */
    private byte[] rawPreviewData = null;

    /**
     * Instantiates a new CameraPreview Object.
     *
     * @param context       Context of the application.
     * @param camera        the Camera service object.
     */
    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
    }

    /**
     * Handles the initial creation of the CameraPreview surface.
     *
     * @param holder        The SurfaceHolder whose surface has changed.
     */
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

    /**
     * Handles the recreation of the camera preview surface (in case app gets closed and reopened or
     * the android device changes from landscape to portrait or vice versa)
     *
     * @param holder        The SurfaceHolder whose surface has changed.
     * @param format        The new PixelFormat of the surface.
     * @param width         The new width of the surface.
     * @param height        The new height of the surface.
     */
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

    /**
     * Handles the destruction of the camera preview surface.
     *
     * @param holder        The SurfaceHolder whose surface has changed.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        //TODO

    }

    /**
     * Gets a single frame from the camera
     *
     * @param data          the contents of the preview frame in the format of an byte array.
     * @param camera        the Camera service object.
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        this.rawPreviewData = data;

    }

    /**
     * Manually focuses the Camera service object.
     */
    public void setFocusMode()
    {
        Camera.Parameters parameters = this.mCamera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        this.mCamera.setParameters(parameters);
    }

    /**
     * Gets the saved preview frame of onPreviewFrame
     *
     * @return byte[]   preview frame.
     */
    public byte[] getRawPreviewData()
    {
        return this.rawPreviewData;
    }

    /**
     * Gets Camera service object.
     *
     * @return Camera   Camera service object.
     */
    public Camera getCamera()
    {
        return this.mCamera;
    }

    /**
     * Sets Camera service object.
     *
     * @param camera    Camera service object.
     */
    public void setCamera(Camera camera)
    {
        this.mCamera = camera;
    }
 }
