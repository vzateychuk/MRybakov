package ru.vzateychuk.mr2.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by vez on 1.11.15.
 */
public final class Utils {

    // debug purpose only
    private final static String TAG = "Utils";

    private final static String DEFAULT_IMAGE_NAME = "default_image.jpg";
    private final static String DEFAULT_HTML_NAME = "default_text.html";

    private final static String TAG_DATE_FORMAT = "dd.MM.yyyy";

    public final static String TYPE_DOCUMENT = "Documents";
    public final static String TYPE_PICTURE = Environment.DIRECTORY_PICTURES;
    public final static String TYPE_DATABASE = "Database";
    
    public final static String TAG_DATAFILE_NAME = "ArticlesDB.db";

    // this is file extension
    private final static String JPEG_SUFFIX = ".jpg";
    private final static String HTML_SUFFIX = ".html";
    private final static String HTML_PREFIX = "file:///";

    public static final int BUFFER_SIZE = 1024;

    // Checks if external storage is available for read and write
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Download files from url by the stream
     * used in DownloadDataStreamService
     * silently return if something goes wrong
     * */
    public static String downloadUsingStream(
            Context context,
            String urlStr,
            String fileType,
            String fileId) {

        Log.d(TAG, "downloadUsingStream()"
                +"; id=" +fileId
                +", type="+fileType
                +", url="+urlStr);

        String result = "";

        try {
            // Note that if external storage is not currently mounted this will silently fail.
            File storageDir = context.getExternalFilesDir(fileType);
            // define file name with extention out from fileType
            String fileName = fileId + (fileType == Utils.TYPE_PICTURE ? ".jpg" : ".html");
            File file = new File(storageDir, fileName);

            URL url = new URL(urlStr);
            BufferedInputStream bis = new BufferedInputStream(url.openStream());
            FileOutputStream fis = new FileOutputStream(file);
            byte[] buffer = new byte[Utils.BUFFER_SIZE];
            int count=0;
            while((count = bis.read(buffer, 0, Utils.BUFFER_SIZE)) != -1)
                fis.write(buffer, 0, count);

            fis.close();
            bis.close();
            result = file.getAbsolutePath();
        } catch (IOException ex) {
            Log.e(TAG, ".onHandleIntent(); Error downloadUsingStream(), url=" + urlStr, ex);
        }

        // return path to downloaded file
        return result;
    }


    // Creates and display progress dialog. Used in InitialActivity only
    public static ProgressDialog showProgressDialog(Context context, String message) {
        Log.d(TAG, ".showProgressDialog()");
        // no active calls indicator

        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.show();
        return dialog;
    }

    /**
     * Human readable date representation
     * used in ArticleViewAdapter.getView()
      */
    public static String getDateFromTimestamp(long timestamp) {
        Log.d(TAG, "getDateFromTimestamp(" + timestamp + ")");

        // convert value from long to Human readable format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return (String) DateFormat.format(TAG_DATE_FORMAT, calendar);
    }

    /**
     * Create and returns the Bitmap image from local storage optimized by size
     * @param imageID - filename of the image
     * @param reqSize - maximum side of image
     * returns default image (DEFAULT_IMAGE_NAME) if image can't be loaded
     * used in ArticleViewAdapter.getView()
     * */
    public static Bitmap getBitmapFromStorage(Context context, String imageID, int reqSize) {
        Log.d(TAG, ".getBitmapFromFilePath(), image=" + imageID + ", size=" + reqSize);

        Bitmap result = null;
        // construct full file name
        String fileName = imageID+JPEG_SUFFIX;
        File imageFile = new File(context.getExternalFilesDir(Utils.TYPE_PICTURE), fileName);

        // if image file exists trying to load, optimize and decode
        if (imageFile.exists()) {
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
            // Calculate inSampleSize
            bmOptions.inSampleSize = calculateImageInSampleSize(bmOptions, reqSize, reqSize);

            // Decode bitmap with inSampleSize set from file with given options
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inPurgeable = true;
            result = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
        }
            // if can't be loaded and decoded, load default image from local storage instead
        if (result == null) {
            Log.d(TAG, ".getBitmapFromFilePath(), cann't decode image:" + imageFile.getAbsolutePath());
            File file = new File(context.getExternalFilesDir(TYPE_PICTURE), DEFAULT_IMAGE_NAME);
            result = BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return result;
    }

    /**
     * Load html file from local storage
     * Used in ArticleFragment
     * */
    public static String getUrlFileFromStorage(Context context, String articleID) {
        Log.d(TAG, ".getUrlFileFromStorage(), articleID=" + articleID);

        //  check if the file exists
        File html = new File(context.getExternalFilesDir(Utils.TYPE_DOCUMENT), articleID + HTML_SUFFIX);

        // if file doesn't exists, return path к тексту по умолчанию
        if(!html.exists())
            html = new File(context.getExternalFilesDir(Utils.TYPE_DOCUMENT), DEFAULT_HTML_NAME);

        return (HTML_PREFIX + html.getAbsolutePath());
    }
    /**
     * Copy all Assets by Type to the local storage
     * returns boolean
     * Uses by InitialActivity
     */
    public static boolean copyAssets(Context context, String assetType, boolean rewrite){
        Log.d(TAG, "copyAssets(), assetType=" + assetType);

        // Get asset manager and define list of files to copy
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(assetType);
        } catch (IOException ioe) {
            // exit if something wrong with assets
            Log.d(TAG, "copyAssets(),Failed to get asset file list.", ioe);
            return false;
        }
        // copy files if exists
        for (String fileName : files) {
            InputStream in = null;
            OutputStream out = null;
            File outFile = null;

            // define full path to outFile (depends of asset_type)
            if (assetType == TYPE_DATABASE) {
                // check if database directory exists and create if necessary
                File dir = new File(context.getDatabasePath(fileName).getParent());
                if (!dir.exists()) dir.mkdirs(); //create folders where write files
                outFile = context.getDatabasePath(fileName);
            }  else {
                outFile = new File(context.getExternalFilesDir(assetType), fileName);
            }
            // copy file from asset if file doesn't exists or it need to be rewrited
            if (rewrite || (!outFile.exists())) {
                try {
                    // get input stream from Assets
                    in = new BufferedInputStream(assetManager.open(assetType + "/" + fileName), BUFFER_SIZE);
                    out = new BufferedOutputStream(new FileOutputStream(outFile), BUFFER_SIZE);
                    // copy from in to out
                    copyFile(in, out);
                } catch (IOException ioe) {
                    Log.e(TAG, "copyAssets(), Failed to copy asset: " + fileName, ioe);
                } finally {
                    // try to close input stream
                    try {
                        in.close();
                    } catch (IOException ein) {
                        Log.e(TAG, "copyAssets(), filed to close in, file: " + fileName, ein);
                    }
                    try {
                        out.close();
                    } catch (IOException eout) {
                        Log.e(TAG, "copyAssets(), filed to close out, file: " + fileName, eout);
                    }
                }

            }
        }
        return true;
    }

    /**
     * Creating new email intent to create email message
     *  Used in MainActivity
     */
    public static Intent makeEmailIntent(String email_to,
                                         String email_subject,
                                         String email_body) {

        Log.d(TAG, "createEmailIntent()");

        Intent intent = new Intent(Intent.ACTION_SEND);

        // "text/plain" MIME type
        intent.setType("text/plain");
        // to
        intent.putExtra(Intent.EXTRA_EMAIL,
                new String [] {email_to});
        // subject
        intent.putExtra(Intent.EXTRA_SUBJECT, email_subject);
        // body
        intent.putExtra(Intent.EXTRA_TEXT, email_body);

        return intent;
    }
    /**
     * Just copy data from input to output stream
     * Used in Utils.copyAssets
     * */
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }


    /**
     * Calculate a sample size value that is a power of two based on a target width and height
     * used by Utils.getBitmapFromStorage()
     * @param options   - size values of image provided
     * @param reqHeight - required height
     * @param reqWidth  - required width
     * see details: http://developer.android.com/intl/ru/training/displaying-bitmaps/load-bitmap.html
     * */
    private static int calculateImageInSampleSize(
            BitmapFactory.Options options,
            int reqWidth,
            int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
