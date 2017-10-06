package com.codephillip.app.imageupload;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_CAPTURE = 3569;
    private static final int REQUEST_GALLERY_IMAGE = 5884;
    private final String URL = "https://chapchapafrica.com/attach_file.php";
    private TextView textTargetUri;
    private ImageView targetImage;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textTargetUri = (TextView)findViewById(R.id.targeturi);
        targetImage = (ImageView)findViewById(R.id.targetimage);
        pDialog = new ProgressDialog(this);
    }

    /**
     * User uploads image from gallery or takes a picture using the camera
     * */
    public void uploadImage(View view) {
        Log.d(TAG, "uploadImage: clicked");
        openGallery();
//        openCamera();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap = null;

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            targetImage.setImageBitmap(bitmap);
        }

        if (requestCode == REQUEST_GALLERY_IMAGE &&resultCode == RESULT_OK){
            try {
                Uri targetUri = data.getData();
                textTargetUri.setText(targetUri.toString());
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                targetImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SendImageTask task = new SendImageTask();
        task.execute(bitmap);
    }


    private class SendImageTask extends AsyncTask<Bitmap, Integer, String> {
        @Override
        protected String doInBackground(Bitmap... bitmap) {

            try {
                return initialiseImageUpload(bitmap[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            displayProgressDialog();
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();
            Toast.makeText(MainActivity.this, "Uploaded image", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onPostExecute: " + result);
        }
    }

    private String initialiseImageUpload(Bitmap bitmap) {
        try {
            if (bitmap != null) {
                // first convert bitmap because Okhttp only accepts files or URIs
                File file = convertBitmapToFile(bitmap);
                String response = sendImageToServer(file, "0756878434");
//                String response = sendImageToServer(uri, "0756878434");
                Log.d(TAG, "onActivityResult: RESPONSE " + response);
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private File convertBitmapToFile(@NonNull Bitmap bitmap) {
        try {
            // use getCacheDir to temporarily store the file
            File file = new File(getCacheDir(), "image.png");
            file.createNewFile();

            //Convert bitmap to byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();

            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void displayProgressDialog() {
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void dismissProgressDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private String sendImageToServer(File file, String phoneNumber) throws IOException {
        Log.d(TAG, "sendImageToServer: started");

        try {
            MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
            String imageName = generateImageName();
            Log.d(TAG, "sendImageToServer: ImageName " + imageName);
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(180, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS).build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("theFile", imageName, RequestBody.create(MEDIA_TYPE_PNG, file))
                    .addFormDataPart("phoneNumber", phoneNumber)
                    .build();
            Request request = new Request.Builder().url(URL).post(body).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Network failure";
    }



    /**
     * Incase you store the image in gallery, pass the URI.
     * This seems to give a high quality image compared to converting the bitmap to file
     */
    private String sendImageToServer(String fileUri, String phoneNumber) throws IOException {
        Log.d(TAG, "sendImageToServer: started");

        try {
            MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
            String imageName = generateImageName();
            Log.d(TAG, "sendImageToServer: ImageName " + imageName);
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(180, TimeUnit.SECONDS).readTimeout(180, TimeUnit.SECONDS).build();
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("theFile", imageName, RequestBody.create(MEDIA_TYPE_PNG, fileUri))
                    .addFormDataPart("phoneNumber", phoneNumber)
                    .build();
            Request request = new Request.Builder().url(URL).post(body).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Network failure";
    }

    @NonNull
    private String generateImageName() {
        String imageName = UUID.randomUUID().toString().substring(0, 5);
        return imageName + ".png";
    }
}
