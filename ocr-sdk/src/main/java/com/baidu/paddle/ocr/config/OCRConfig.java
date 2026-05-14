package com.baidu.paddle.ocr.config;

public class OCRConfig {
    public String modelPath = null;
    public String labelPath = null;
    public int cpuThreadNum = 4;
    public String cpuPowerMode = "LITE_POWER_HIGH";
    public int detLongSize = 960;
    public float scoreThreshold = 0.1f;
    public boolean useOpenCL = false;

    public OCRConfig() {
    }

    public OCRConfig setModelPath(String modelPath) {
        this.modelPath = modelPath;
        return this;
    }

    public OCRConfig setLabelPath(String labelPath) {
        this.labelPath = labelPath;
        return this;
    }

    public OCRConfig setCpuThreadNum(int cpuThreadNum) {
        this.cpuThreadNum = cpuThreadNum;
        return this;
    }

    public OCRConfig setCpuPowerMode(String cpuPowerMode) {
        this.cpuPowerMode = cpuPowerMode;
        return this;
    }

    public OCRConfig setDetLongSize(int detLongSize) {
        this.detLongSize = detLongSize;
        return this;
    }

    public OCRConfig setScoreThreshold(float scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
        return this;
    }

    public OCRConfig setUseOpenCL(boolean useOpenCL) {
        this.useOpenCL = useOpenCL;
        return this;
    }
}
