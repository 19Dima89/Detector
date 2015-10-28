package de.rosenheim.fh.bachelor.types;

import android.graphics.Bitmap;

/**
 * Created by Dima-Desktop on 28.10.2015.
 */
public class ScanObject {

    //Local variables
    private Bitmap referencePicture = null;
    private String objectName = null;

    public ScanObject(Bitmap referencePicture, String objectName)
    {
        this.referencePicture = referencePicture;
        this.objectName = objectName;
    }

    public Bitmap getReferencePicture() {
        return referencePicture;
    }

    public String getObjectName() {
        return objectName;
    }
}
