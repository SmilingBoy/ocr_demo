package com.baidu.paddle.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.baidu.paddle.ocr.config.OCRConfig;
import com.baidu.paddle.ocr.core.Predictor;
import com.baidu.paddle.ocr.model.OCRResult;

import java.util.List;

public class PaddleOCR {
    private static final String TAG = "PaddleOCR";
    private static final String DEFAULT_MODEL_PATH = "models/ch_PP-OCRv2";
    private static final String DEFAULT_LABEL_PATH = "labels/ppocr_keys_v1.txt";
    
    private Context context;
    private OCRConfig config;
    private Predictor predictor;
    private boolean initialized = false;

    private PaddleOCR(Context context, OCRConfig config) {
        this.context = context.getApplicationContext();
        this.config = config != null ? config : new OCRConfig();
        this.predictor = new Predictor();
    }

    public static PaddleOCR create(Context context) {
        return new PaddleOCR(context, null);
    }

    public static PaddleOCR create(Context context, OCRConfig config) {
        return new PaddleOCR(context, config);
    }

    public boolean init() {
        if (initialized) {
            Log.w(TAG, "PaddleOCR already initialized");
            return true;
        }

        String modelPath = config.modelPath != null ? config.modelPath : DEFAULT_MODEL_PATH;
        String labelPath = config.labelPath != null ? config.labelPath : DEFAULT_LABEL_PATH;

        try {
            initialized = predictor.init(
                    context,
                    modelPath,
                    labelPath,
                    config.useOpenCL ? 1 : 0,
                    config.cpuThreadNum,
                    config.cpuPowerMode,
                    config.detLongSize,
                    config.scoreThreshold
            );
            if (initialized) {
                Log.i(TAG, "PaddleOCR initialized successfully");
            } else {
                Log.e(TAG, "Failed to initialize PaddleOCR");
            }
            return initialized;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing PaddleOCR", e);
            return false;
        }
    }

    public List<OCRResult> detect(Bitmap image) {
        return detect(image, true, true, true);
    }

    public List<OCRResult> detect(Bitmap image, boolean runDet, boolean runCls, boolean runRec) {
        if (!initialized || !predictor.isLoaded()) {
            Log.e(TAG, "PaddleOCR not initialized. Call init() first.");
            return null;
        }
        if (image == null) {
            Log.e(TAG, "Input image is null");
            return null;
        }
        return predictor.detect(image, runDet, runCls, runRec);
    }

    public List<OCRResult> detectTextOnly(Bitmap image) {
        return detect(image, false, true, true);
    }

    public List<OCRResult> detectBoxOnly(Bitmap image) {
        return detect(image, true, false, false);
    }

    public Bitmap drawResults(Bitmap image, List<OCRResult> results) {
        return Predictor.drawResults(image, results);
    }

    public float getLastInferenceTime() {
        return predictor.getInferenceTime();
    }

    public boolean isInitialized() {
        return initialized && predictor.isLoaded();
    }

    public void release() {
        if (predictor != null) {
            predictor.releaseModel();
        }
        initialized = false;
        Log.i(TAG, "PaddleOCR released");
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }
}
