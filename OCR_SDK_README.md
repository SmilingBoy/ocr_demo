# PaddleOCR Android SDK

## 简介

这是一个基于 PaddleOCR 和 Paddle Lite 的 Android 端 OCR 识别库，可以快速方便地在 Android 项目中集成文字识别功能。

## 项目结构

```
android_ocr_demo/
├── ocr-sdk/           # OCR 核心 SDK 库模块
│   ├── src/main/java/com/baidu/paddle/ocr/
│   │   ├── PaddleOCR.java        # 主入口类
│   │   ├── config/
│   │   │   └── OCRConfig.java    # 配置类
│   │   ├── model/
│   │   │   └── OCRResult.java    # 结果模型类
│   │   └── core/                  # 核心实现
│   ├── src/main/cpp/             # C++ 原生实现
│   └── src/main/assets/          # 模型和资源文件
└── app/              # 示例应用模块
```

## 快速开始

### 1. 添加依赖

在你的项目 `settings.gradle` 中添加：

```groovy
include ':ocr-sdk'
```

在应用模块的 `build.gradle` 中添加依赖：

```groovy
dependencies {
    implementation project(':ocr-sdk')
}
```

### 2. 基本使用

#### 初始化

```java
import com.baidu.paddle.ocr.PaddleOCR;
import com.baidu.paddle.ocr.model.OCRResult;

// 初始化（使用默认配置）
PaddleOCR paddleOCR = PaddleOCR.create(context);
boolean success = paddleOCR.init();
```

#### 使用自定义配置

```java
import com.baidu.paddle.ocr.config.OCRConfig;

OCRConfig config = new OCRConfig()
    .setCpuThreadNum(4)
    .setDetLongSize(960)
    .setScoreThreshold(0.1f)
    .setUseOpenCL(false);

PaddleOCR paddleOCR = PaddleOCR.create(context, config);
paddleOCR.init();
```

#### 进行识别

```java
// 完整识别（检测+分类+识别）
List<OCRResult> results = paddleOCR.detect(bitmap);

// 自定义模式
List<OCRResult> results = paddleOCR.detect(bitmap, true, true, true); 
// 参数依次：是否检测、是否分类、是否识别

// 仅检测文本框
List<OCRResult> results = paddleOCR.detectBoxOnly(bitmap);

// 仅识别文本（假设已裁剪好）
List<OCRResult> results = paddleOCR.detectTextOnly(bitmap);
```

#### 处理结果

```java
for (OCRResult result : results) {
    // 获取识别的文字
    String text = result.getText();
    
    // 获取置信度
    float confidence = result.getConfidence();
    
    // 获取文本方向
    int direction = result.getDirection();
    String directionLabel = result.getDirectionLabel(); // "0" 或 "180"
    
    // 获取检测框的四个角坐标
    List<Point> points = result.getPoints();
}
```

#### 绘制结果

```java
Bitmap resultBitmap = paddleOCR.drawResults(originalBitmap, results);
imageView.setImageBitmap(resultBitmap);
```

#### 获取推理时间

```java
float inferenceTime = paddleOCR.getLastInferenceTime();
```

#### 释放资源

```java
paddleOCR.release();
```

## API 文档

### PaddleOCR 类

#### 静态方法

| 方法 | 描述 |
|-----|------|
| `static PaddleOCR create(Context context)` | 创建实例，使用默认配置 |
| `static PaddleOCR create(Context context, OCRConfig config)` | 创建实例，使用自定义配置 |

#### 实例方法

| 方法 | 描述 |
|-----|------|
| `boolean init()` | 初始化模型 |
| `List<OCRResult> detect(Bitmap bitmap)` | 完整识别（检测+分类+识别） |
| `List<OCRResult> detect(Bitmap bitmap, boolean runDet, boolean runCls, boolean runRec)` | 自定义模式识别 |
| `List<OCRResult> detectBoxOnly(Bitmap bitmap)` | 仅检测文本框 |
| `List<OCRResult> detectTextOnly(Bitmap bitmap)` | 仅识别文本 |
| `Bitmap drawResults(Bitmap bitmap, List<OCRResult> results)` | 在图片上绘制检测框 |
| `float getLastInferenceTime()` | 获取上次推理耗时（毫秒） |
| `boolean isInitialized()` | 检查是否已初始化 |
| `void release()` | 释放资源 |

### OCRConfig 类

配置选项：

| 属性 | 类型 | 默认值 | 描述 |
|-----|------|-------|------|
| `modelPath` | String | null | 自定义模型路径（assets 目录下） |
| `labelPath` | String | null | 自定义标签路径（assets 目录下） |
| `cpuThreadNum` | int | 4 | CPU 线程数 |
| `cpuPowerMode` | String | "LITE_POWER_HIGH" | CPU 功耗模式 |
| `detLongSize` | int | 960 | 检测图像长边尺寸 |
| `scoreThreshold` | float | 0.1f | 置信度阈值 |
| `useOpenCL` | boolean | false | 是否使用 OpenCL |

### OCRResult 类

结果模型：

| 方法 | 返回类型 | 描述 |
|-----|---------|------|
| `getPoints()` | List<Point> | 获取检测框的四个角坐标 |
| `getText()` | String | 获取识别的文字 |
| `getConfidence()` | float | 获取置信度 |
| `getDirection()` | int | 获取方向（0 或 1） |
| `getDirectionLabel()` | String | 获取方向标签（"0" 或 "180"） |
| `getDirectionConfidence()` | float | 获取方向识别置信度 |

## 权限要求

在 AndroidManifest.xml 中添加以下权限：

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.CAMERA" />
```

## 注意事项

1. **初始化建议在子线程进行**：模型加载比较耗时，建议在非 UI 线程初始化
2. **Bitmap 要求**：传入的 Bitmap 建议使用 ARGB_8888 格式
3. **及时释放资源**：使用完后务必调用 `release()` 释放资源
4. **线程安全**：PaddleOCR 类不保证线程安全，建议单线程使用或自行加锁

## 示例代码

完整示例请参考项目中的 app 模块。

## 技术支持

更多信息请参考 PaddleOCR 和 Paddle Lite 官方文档。
