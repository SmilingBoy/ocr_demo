package com.baidu.paddle.lite.demo.ocr;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.baidu.paddle.ocr.PaddleOCR;
import com.baidu.paddle.ocr.model.OCRResult;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final int OPEN_GALLERY_REQUEST_CODE = 0;
    public static final int TAKE_PHOTO_REQUEST_CODE = 1;

    public static final int REQUEST_LOAD_MODEL = 0;
    public static final int REQUEST_RUN_MODEL = 1;
    public static final int RESPONSE_LOAD_MODEL_SUCCESSED = 0;
    public static final int RESPONSE_LOAD_MODEL_FAILED = 1;
    public static final int RESPONSE_RUN_MODEL_SUCCESSED = 2;
    public static final int RESPONSE_RUN_MODEL_FAILED = 3;

    protected ProgressDialog pbLoadModel = null;
    protected ProgressDialog pbRunModel = null;

    protected Handler receiver = null;
    protected Handler sender = null;
    protected HandlerThread worker = null;

    protected TextView tvInputSetting;
    protected TextView tvStatus;
    protected ImageView ivInputImage;
    protected TextView tvOutputResult;
    protected TextView tvInferenceTime;
    protected Spinner spRunMode;

    private String currentPhotoPath;
    private Bitmap currentImage = null;
    
    private PaddleOCR paddleOCR = null;
    private List<OCRResult> ocrResults = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvInputSetting = findViewById(R.id.tv_input_setting);
        tvStatus = findViewById(R.id.tv_model_img_status);
        ivInputImage = findViewById(R.id.iv_input_image);
        tvInferenceTime = findViewById(R.id.tv_inference_time);
        tvOutputResult = findViewById(R.id.tv_output_result);
        spRunMode = findViewById(R.id.sp_run_mode);
        tvInputSetting.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvOutputResult.setMovementMethod(ScrollingMovementMethod.getInstance());

        receiver = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RESPONSE_LOAD_MODEL_SUCCESSED:
                        if (pbLoadModel != null && pbLoadModel.isShowing()) {
                            pbLoadModel.dismiss();
                        }
                        onLoadModelSuccessed();
                        break;
                    case RESPONSE_LOAD_MODEL_FAILED:
                        if (pbLoadModel != null && pbLoadModel.isShowing()) {
                            pbLoadModel.dismiss();
                        }
                        Toast.makeText(MainActivity.this, "Load model failed!", Toast.LENGTH_SHORT).show();
                        onLoadModelFailed();
                        break;
                    case RESPONSE_RUN_MODEL_SUCCESSED:
                        if (pbRunModel != null && pbRunModel.isShowing()) {
                            pbRunModel.dismiss();
                        }
                        onRunModelSuccessed();
                        break;
                    case RESPONSE_RUN_MODEL_FAILED:
                        if (pbRunModel != null && pbRunModel.isShowing()) {
                            pbRunModel.dismiss();
                        }
                        Toast.makeText(MainActivity.this, "Run model failed!", Toast.LENGTH_SHORT).show();
                        onRunModelFailed();
                        break;
                }
            }
        };

        worker = new HandlerThread("Predictor Worker");
        worker.start();
        sender = new Handler(worker.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REQUEST_LOAD_MODEL:
                        if (onLoadModel()) {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_FAILED);
                        }
                        break;
                    case REQUEST_RUN_MODEL:
                        if (onRunModel()) {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_FAILED);
                        }
                        break;
                }
            }
        };

        setDefaultImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (paddleOCR == null) {
            loadModel();
        }
    }

    private void setDefaultImage() {
        try {
            InputStream is = getAssets().open("images/det_0.jpg");
            currentImage = BitmapFactory.decodeStream(is);
            ivInputImage.setImageBitmap(currentImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadModel() {
        pbLoadModel = ProgressDialog.show(this, "", "Loading model...", false, false);
        sender.sendEmptyMessage(REQUEST_LOAD_MODEL);
    }

    public void runModel() {
        if (currentImage == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show();
            return;
        }
        pbRunModel = ProgressDialog.show(this, "", "Running model...", false, false);
        sender.sendEmptyMessage(REQUEST_RUN_MODEL);
    }

    public boolean onLoadModel() {
        try {
            paddleOCR = PaddleOCR.create(this);
            boolean success = paddleOCR.init();
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Error loading model", e);
            return false;
        }
    }

    public boolean onRunModel() {
        if (currentImage == null || paddleOCR == null) {
            return false;
        }
        
        try {
            String mode = spRunMode.getSelectedItem().toString();
            boolean runDet = mode.contains("检测");
            boolean runCls = mode.contains("分类");
            boolean runRec = mode.contains("识别");
            
            if (!runDet && !runCls && !runRec) {
                runDet = true;
                runCls = true;
                runRec = true;
            }
            
            ocrResults = paddleOCR.detect(currentImage, runDet, runCls, runRec);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error running model", e);
            return false;
        }
    }

    public void onLoadModelSuccessed() {
        tvStatus.setText("Status: Model loaded successfully");
        tvInputSetting.setText("PaddleOCR SDK initialized");
    }

    public void onLoadModelFailed() {
        tvStatus.setText("Status: Failed to load model");
    }

    public void onRunModelSuccessed() {
        tvStatus.setText("Status: Run model successfully");
        tvInferenceTime.setText("Inference time: " + paddleOCR.getLastInferenceTime() + " ms");
        
        if (ocrResults != null && ocrResults.size() > 0) {
            Bitmap resultImage = paddleOCR.drawResults(currentImage, ocrResults);
            ivInputImage.setImageBitmap(resultImage);
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ocrResults.size(); i++) {
                OCRResult result = ocrResults.get(i);
                sb.append(i + 1).append(". ");
                sb.append(result.getText()).append("\n");
                sb.append("   Confidence: ").append(String.format("%.2f", result.getConfidence())).append("\n");
                sb.append("   Direction: ").append(result.getDirectionLabel()).append("\n");
                sb.append("\n");
            }
            tvOutputResult.setText(sb.toString());
        } else {
            tvOutputResult.setText("No text detected");
        }
    }

    public void onRunModelFailed() {
        tvStatus.setText("Status: Run model failed");
    }

    public void btn_reset_img_click(View view) {
        setDefaultImage();
    }

    public void btn_run_model_click(View view) {
        runModel();
    }

    public void btn_choice_img_click(View view) {
        if (requestAllPermissions()) {
            openGallery();
        }
    }

    public void btn_take_photo_click(View view) {
        if (requestAllPermissions()) {
            takePhoto();
        }
    }

    private boolean requestAllPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA},
                    0);
            return false;
        }
        return true;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE);
    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage(), ex);
                Toast.makeText(MainActivity.this,
                        "Create camera temp file failed: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.baidu.paddle.lite.demo.ocr.fileprovider",
                        photoFile);
                currentPhotoPath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case OPEN_GALLERY_REQUEST_CODE:
                    if (data == null) {
                        break;
                    }
                    try {
                        ContentResolver resolver = getContentResolver();
                        Uri uri = data.getData();
                        currentImage = MediaStore.Images.Media.getBitmap(resolver, uri);
                        ivInputImage.setImageBitmap(currentImage);
                    } catch (IOException e) {
                        Log.e(TAG, e.toString());
                    }
                    break;
                case TAKE_PHOTO_REQUEST_CODE:
                    if (currentPhotoPath != null) {
                        ExifInterface exif = null;
                        try {
                            exif = new ExifInterface(currentPhotoPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);
                        Log.i(TAG, "Rotation: " + orientation);
                        Bitmap image = BitmapFactory.decodeFile(currentPhotoPath);
                        currentImage = Utils.rotateBitmap(image, orientation);
                        if (currentImage != null) {
                            ivInputImage.setImageBitmap(currentImage);
                        }
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (paddleOCR != null) {
            paddleOCR.release();
        }
        worker.quit();
        super.onDestroy();
    }
}
