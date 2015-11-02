package de.rosenheim.fh.bachelor.threads;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.InputType;
import android.widget.EditText;

import java.util.List;

import de.rosenheim.fh.bachelor.activities.DetectionActivity;
import de.rosenheim.fh.bachelor.camera.CameraPreview;
import de.rosenheim.fh.bachelor.detector.UtilityClass;
import de.rosenheim.fh.bachelor.types.ScanObject;

/**
 * Created by Dima-Desktop on 28.10.2015.
 */
public class CaptureThread extends Thread{

    //Local variable
    private CameraPreview preview = null;
    private Handler mHandler = null;
    private DetectionActivity detectionActivity = null;
    private List<ScanObject> comparisonObjects = null;
    private Bitmap fetchedFrame = null;

    //Constructor
    public CaptureThread(CameraPreview preview, Handler mHandler, DetectionActivity detectionActivity, List<ScanObject> comparisonObjects)
    {
        this.preview = preview;
        this.mHandler = mHandler;
        this.detectionActivity = detectionActivity;
        this.comparisonObjects = comparisonObjects;
    }

    //Receiving a Frame from the camera and saving it in comparisonObjects
    @Override
    public void run()
    {
        /*comparisonObjects.add(new ScanObject(UtilityClass.fetchRawFrameData(this.preview.getRawPreviewData(), this.preview.getCamera().getParameters().getPreviewSize()), "Bild Nr. "+comparisonObjects.size()));

        mHandler.sendMessage(mHandler.obtainMessage(DetectionActivity.CAPTURE_THREAD, true));*/

        fetchedFrame = UtilityClass.fetchRawFrameData(this.preview.getRawPreviewData(), this.preview.getCamera().getParameters().getPreviewSize());

        mHandler.post(new Runnable()
        {
            public void run(){
                AlertDialog.Builder builder = new AlertDialog.Builder(detectionActivity);
                builder.setTitle("Objektnamen eingeben:");
                builder.setCancelable(false);

                // Set up the input
                final EditText input = new EditText(detectionActivity);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Speichern", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        comparisonObjects.add(new ScanObject(fetchedFrame, input.getText().toString()));
                    }
                });
                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }
}
