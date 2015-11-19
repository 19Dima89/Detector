package de.rosenheim.fh.bachelor.types;

import android.graphics.Bitmap;

/**
 * Created by Dima-Desktop on 28.10.2015.
 */
public class ScanObject {

    /**
     * The reference picture.
     */
    private Bitmap referencePicture = null;
    /**
     * The name of the reference picture.
     */
    private String objectName = null;

    /**
     * Instantiates a new ScanObject instance.
     *
     * @param referencePicture      taken picture.
     * @param objectName            name of the taken picture.
     */
    public ScanObject(Bitmap referencePicture, String objectName)
    {
        this.referencePicture = referencePicture;
        this.objectName = objectName;
    }

    /**
     * Gets the reference picture.
     *
     * @return Bitmap   the reference picture.
     */
    public Bitmap getReferencePicture() {
        return referencePicture;
    }

    /**
     * Gets the name of the reference picture.
     *
     * @return String   the name of the reference picture.
     */
    public String getObjectName() {
        return objectName;
    }
}
