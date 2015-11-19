package de.rosenheim.fh.bachelor.detector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfDMatch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera.Size;
import android.os.Environment;

/**
 * Created by Dima-Desktop on 28.10.2015.
 */
public final class UtilityClass {

    /**
     * Max distance matches can have.
     */
    private static final int DIST_LIMIT = 30;

    /**
     * Private constructor so that the class can not be instantiated
     */
    private UtilityClass()
    {

    }

    /**
     * Converts raw picture data which we get from the onPreviewFrame method in CameraPreview
     * into a Bitmap object
     *
     * @param rawData           raw preview frame byte array.
     * @param previewSize       dimensions of the preview frame.
     * @return Bitmap           converted preview frame.
     */
    public static Bitmap fetchRawFrameData(byte[] rawData, Size previewSize)
    {
        if(rawData != null && previewSize != null)
        {
            YuvImage yuvimage=new YuvImage(rawData, ImageFormat.NV21, previewSize.width, previewSize.height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 70, baos);
            byte[] jdata = baos.toByteArray();

            //Convert to Bitmap
            Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length);

            //Rotate Bitmap 90 degrees
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp,bmp.getWidth(),bmp.getHeight(),true);
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            return rotatedBitmap;
        }
        else
            return null;

    }

    /**
     * Filters matches according to a certain distance limit
     *
     * @param matches       unfiltered matches.
     * @return MatOfDMatch  filtered matches.
     */
    public static MatOfDMatch filterMatchesByDistance(MatOfDMatch matches)
    {
        List<org.opencv.features2d.DMatch> matches_original = matches.toList();
        List<org.opencv.features2d.DMatch> matches_filtered = new ArrayList<org.opencv.features2d.DMatch>();

        //int DIST_LIMIT = 30;
        // Check all the matches distance and if it passes add to list of filtered matches
        for (int i = 0; i < matches_original.size(); i++) {
            org.opencv.features2d.DMatch d = matches_original.get(i);
            if (Math.abs(d.distance) <= DIST_LIMIT) {
                matches_filtered.add(d);
            }
        }

        MatOfDMatch mat = new MatOfDMatch();
        mat.fromList(matches_filtered);
        return mat;
    }

    /**
     * Saves a Bitmap with a certain name onto the internal storage of the android device.
     *
     * @param bitmapImage       Bitmap which gets saved.
     * @param name              name of the Bitmap which gets saved.
     */
    public static void saveToInternalStorage(Bitmap bitmapImage, String name)
    {

        File mypath=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name+".jpg");

        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(mypath);

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
