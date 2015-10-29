package de.rosenheim.fh.bachelor.detector;

import android.graphics.Camera;
import android.os.Handler;

import java.util.List;

import de.rosenheim.fh.bachelor.camera.CameraPreview;
import de.rosenheim.fh.bachelor.types.ScanObject;

/**
 * Created by Dima-Desktop on 28.10.2015.
 */
public class CaptureThread extends Thread{

    //Local variable
    private CameraPreview preview = null;
    private Handler mHandler = null;
    private List<ScanObject> comparisonObjects = null;

    //Constructor
    public CaptureThread(CameraPreview preview, Handler mHandler, List<ScanObject> comparisonObjects)
    {
        this.preview = preview;
        this.mHandler = mHandler;
        this.comparisonObjects = comparisonObjects;
    }

    //Receiving a Frame from the camera and saving it in comparisonObjects
    @Override
    public void run()
    {
        comparisonObjects.add(new ScanObject(UtilityClass.fetchRawFrameData(this.preview.getRawPreviewData(), this.preview.getCamera().getParameters().getPreviewSize()), "Bild Nr. "+comparisonObjects.size()));

        mHandler.sendMessage(mHandler.obtainMessage(DetectionActivity.CAPTURE_THREAD, true));
    }
}
