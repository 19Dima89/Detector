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

    /**
     * The camera preview.
     */
    private CameraPreview preview = null;
    /**
     * The handler instance to the main thread.
     */
    private Handler mHandler = null;
    /**
     * The main activity of the application.
     */
    private DetectionActivity detectionActivity = null;
    /**
     * A list of all the present reference pictures.
     */
    private List<ScanObject> comparisonObjects = null;
    /**
     * The taken frame.
     */
    private Bitmap fetchedFrame = null;

    /**
     * Instantiates a new CaptureThread instance
     *
     * @param preview               Camera preview.
     * @param mHandler              Handler to communicate with the main thread.
     * @param detectionActivity     MainActivity of the application.
     * @param comparisonObjects     List of all reference pictures.
     */
    public CaptureThread(CameraPreview preview, Handler mHandler, DetectionActivity detectionActivity, List<ScanObject> comparisonObjects)
    {
        this.preview = preview;
        this.mHandler = mHandler;
        this.detectionActivity = detectionActivity;
        this.comparisonObjects = comparisonObjects;
    }

    /**
     * Logic of the thread (capturing a picture and saving it, along with a name, in comparisonObjects)
     */
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
                        detectionActivity.enableButtons();
                    }
                });
                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                        detectionActivity.enableButtons();
                    }
                });

                builder.show();
            }
        });
    }
}
