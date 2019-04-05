package com.saad.grit.memedetect;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GallaryActivity extends AppCompatActivity {

    private static final String TAG = GallaryActivity.class.getSimpleName();
    private GridView gridView;
    private String fileLocation;

    private String targetFolder = "/sampleimages/";
    private String targetFolder2 = "/DCIM/Screenshots/";
    private String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
    private ArrayList<Bitmap> photo = new ArrayList<Bitmap>();
    public static String[] imageFileList;
    TextView gallerytxt;

    public static ImageAdapter imageAdapter;
    private HashMap<Bitmap, String> photoMap = new HashMap<>();
    private ArrayList<BitmapModel> bitmapModels = new ArrayList<>();

    private final int memeImageWidth = 1000;
    private final int memeImageHeight = 1040;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallary_view_base);

        gridView = (GridView) findViewById(R.id.gridView);
        gallerytxt = (TextView) findViewById(R.id.gallerytxt);

        ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GallaryActivity.this.finish();
            }
        });

        new MyGalleryAsy().execute();

    }

    public class MyGalleryAsy extends AsyncTask<Void, Void, ArrayList<BitmapModel>> {
        private ProgressDialog dialog;
        private Bitmap mBitmap;

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(GallaryActivity.this, "", "Loading ...", true);
            dialog.show();
        }

        @Override
        protected ArrayList<BitmapModel> doInBackground(Void... arg0) {
            return readImage();
        }

        @Override
        protected void onPostExecute(ArrayList<BitmapModel> result) {

            if (result.size() > 0) {
                imageAdapter = new ImageAdapter(GallaryActivity.this, result);
                gridView.setAdapter(imageAdapter);
            }

            dialog.dismiss();

        }

    }

    private boolean isImageMemeResolution(Bitmap bitmap) {

        boolean isMemeRes = true;

        int imageWidth = bitmap.getWidth();
        int imageHeight = bitmap.getHeight();

        Log.d(TAG, "readImage: Image Meme Res : Width : " + imageWidth + " : Height : " + imageHeight);

        if ((imageHeight > memeImageHeight) && (imageWidth > memeImageWidth)) {
            isMemeRes = false;
        }

        return isMemeRes;
    }

    private ArrayList<BitmapModel> readImage() {

        try {
            if (isSdPresent()) {
                fileLocation = extStorageDirectory + targetFolder;
            } else
                fileLocation = extStorageDirectory + targetFolder2;

            File file1 = new File(fileLocation);

            if (file1.isDirectory()) { // sdCard == true
                imageFileList = file1.list();
                if (imageFileList != null) {
                    for (int i = 0; i < imageFileList.length; i++) {

                        Log.d(TAG, "readImage: Files: " + imageFileList[i].trim());

                        try {

                            Bitmap bitmap = BitmapFactory.decodeFile(fileLocation + imageFileList[i].trim());

                            boolean isMeme = false;
                            boolean isMemeRes = isImageMemeResolution(bitmap);
                            Log.d(TAG, "readImage: File : Is Meme Res ? " + isMemeRes);

                            ExifInterface exifInterface = new ExifInterface(file1.getAbsolutePath() + "/" + imageFileList[i].trim());
                            showExif(exifInterface);

                            boolean hasExifData = hasExifData(exifInterface);

                            if (isMemeRes && !hasExifData) {
                                isMeme = true;
                            }

                            BitmapModel bitmapModel = new BitmapModel(bitmap, String.valueOf(isMeme));
                            bitmapModels.add(bitmapModel);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error!", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }

        return bitmapModels;

    }

    private void showExif(ExifInterface exif) {
        String myAttribute = "Exif information ---\n";
        myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif);
        myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
        myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
        myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
        myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
        myAttribute += getTagString(ExifInterface.TAG_APERTURE_VALUE, exif);
        myAttribute += getTagString(ExifInterface.TAG_FOCAL_LENGTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_EXPOSURE_TIME, exif);

        Log.d(TAG, "readImage: Files: Exif Data : " + myAttribute);

    }

    private String getTagString(String tag, ExifInterface exif) {
        return (tag + " : " + exif.getAttribute(tag) + "\n");
    }

    private boolean hasExifData(ExifInterface exif) {

        final String make = ExifInterface.TAG_MAKE;
        final String model = ExifInterface.TAG_MODEL;
        final String apertureValue = ExifInterface.TAG_APERTURE_VALUE;
        final String focalLength = ExifInterface.TAG_FOCAL_LENGTH;
        final String exposureTime = ExifInterface.TAG_EXPOSURE_TIME;

        boolean hasExifData = true;

        if (exif.getAttribute(make) == null) {
            hasExifData = false;
        }
        if (exif.getAttribute(model) == null) {
            hasExifData = false;
        }
        if (exif.getAttribute(apertureValue) == null) {
            hasExifData = false;
        }
        if (exif.getAttribute(focalLength) == null) {
            hasExifData = false;
        }
        if (exif.getAttribute(exposureTime) == null) {
            hasExifData = false;
        }

        return hasExifData;

    }

    public static boolean isSdPresent() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public class ImageAdapter extends BaseAdapter {

        private ArrayList<BitmapModel> bitmapModels;

        private Context context;
        private LayoutInflater layoutInflater;

        private int mGalleryItemBackground;

        public ImageAdapter(Context context, ArrayList<BitmapModel> bitmapModels) {
            this.context = context;
            this.bitmapModels = bitmapModels;
        }

        public int getCount() {
            return bitmapModels.size();
        }

        public Object getItem(int position) {
            return bitmapModels.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            BitmapModel bitmapModel = bitmapModels.get(position);

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = layoutInflater.inflate(R.layout.gallery_item, null);

            ImageView imageView = (ImageView) v.findViewById(R.id.imageView1);
            TextView textView = (TextView) v.findViewById(R.id.textView1);

            imageView.setImageBitmap(bitmapModel.getBitmap());
            textView.setText("Is a Meme Type ? " + bitmapModel.getIsMeme());

            return v;

        }

    }

    private class BitmapModel {

        private Bitmap bitmap;
        private String isMeme;

        public BitmapModel(Bitmap bitmap, String isMeme) {
            this.bitmap = bitmap;
            this.isMeme = isMeme;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public String getIsMeme() {
            return isMeme;
        }

        public void setIsMeme(String isMeme) {
            this.isMeme = isMeme;
        }
    }

}
