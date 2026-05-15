# PaddleOCR Android SDK

基于 PaddleOCR 和 Paddle Lite 的 Android 端 OCR 识别库，支持文本检测、方向分类和文本识别。

## 功能特性

- 支持 6 种运行模式：
  - 检测 + 分类 + 识别（完整流程）
  - 检测 + 识别
  - 分类 + 识别
  - 仅检测
  - 仅识别
  - 仅分类
- 内置中文简体识别模型（PP-OCRv2）
- 支持 CPU 和 GPU（OpenCL）加速
- 简洁易用的 API

## 环境要求

| 项目 | 版本 |
|-----|------|
| Android SDK | API 23+ (Android 6.0+) |
| NDK | 21.1.6352462 |
| CMake | 3.10.2 |
| Paddle Lite | v2.10 |
| OpenCV | 4.2.0 |

## 项目结构

```
android_ocr_demo/
├── ocr-sdk/          # OCR 核心 SDK 库模块
│   ├── src/main/java/com/baidu/paddle/ocr/
│   │   ├── PaddleOCR.java       # 主入口类
│   │   ├── config/
│   │   │   └── OCRConfig.java   # 配置类
│   │   ├── model/
│   │   │   └── OCRResult.java   # 结果模型类
│   │   └── core/                # 核心实现
│   ├── src/main/cpp/            # C++ 原生实现
│   └── src/main/assets/         # 模型和资源文件
│       ├── models/
│       │   └── ch_PP-OCRv2/     # 内置模型
│       └── labels/
│           └── ppocr_keys_v1.txt
├── app/              # 示例应用模块
├── jitpack.yml       # JitPack 构建配置
└── README.md
```

## 集成方式

### 方式一：源码集成（推荐用于开发）

1. 将 `ocr-sdk` 模块复制到你的项目中
2. 在 `settings.gradle` 中添加：
```groovy
include ':ocr-sdk'
```

3. 在应用模块的 `build.gradle` 中添加依赖：
```groovy
dependencies {
    implementation project(':ocr-sdk')
}
```

### 方式二：JitPack 远程依赖

1. 在项目根目录的 `build.gradle` 中添加：
```groovy
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

2. 在应用模块的 `build.gradle` 中添加依赖：
```groovy
dependencies {
    implementation 'com.github.SmilingBoy:android_ocr_demo:Tag'
}
```
> 注意：将 `Tag` 替换为实际的版本号或 Commit Hash

## 快速开始

### 1. 初始化

```java
import com.baidu.paddle.ocr.PaddleOCR;
import com.baidu.paddle.ocr.model.OCRResult;

// 使用默认配置
PaddleOCR paddleOCR = PaddleOCR.create(context);
boolean success = paddleOCR.init();

if (success) {
    Log.i("OCR", "初始化成功");
}
```

### 2. 自定义配置

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

### 3. 文本识别

```java
// 完整识别（检测 + 分类 + 识别）
List<OCRResult> results = paddleOCR.detect(bitmap);

// 自定义模式
List<OCRResult> results = paddleOCR.detect(bitmap, true, true, true);
// 参数：是否检测(runDet)、是否分类(runCls)、是否识别(runRec)

// 仅检测文本框
List<OCRResult> results = paddleOCR.detectBoxOnly(bitmap);

// 仅识别文本（适用于已裁剪好的文本区域）
List<OCRResult> results = paddleOCR.detectTextOnly(bitmap);
```

### 4. 处理结果

```java
for (OCRResult result : results) {
    String text = result.getText();           // 识别文字
    float confidence = result.getConfidence(); // 置信度
    int direction = result.getDirection();    // 方向 (0=0°, 1=180°)
    String dirLabel = result.getDirectionLabel(); // "0" 或 "180"
    List<Point> points = result.getPoints();  // 检测框四个角坐标
}
```

### 5. 绘制检测框

```java
Bitmap resultBitmap = paddleOCR.drawResults(originalBitmap, results);
imageView.setImageBitmap(resultBitmap);
```

### 6. 获取推理时间和释放资源

```java
// 获取上次推理耗时（毫秒）
float inferenceTime = paddleOCR.getLastInferenceTime();

// 检查是否已初始化
boolean isReady = paddleOCR.isInitialized();

// 释放资源
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
| `boolean init()` | 初始化模型，返回是否成功 |
| `List<OCRResult> detect(Bitmap bitmap)` | 完整识别（检测+分类+识别） |
| `List<OCRResult> detect(Bitmap bitmap, boolean runDet, boolean runCls, boolean runRec)` | 自定义模式识别 |
| `List<OCRResult> detectBoxOnly(Bitmap bitmap)` | 仅检测文本框 |
| `List<OCRResult> detectTextOnly(Bitmap bitmap)` | 仅识别文本 |
| `Bitmap drawResults(Bitmap bitmap, List<OCRResult> results)` | 在图片上绘制检测框 |
| `float getLastInferenceTime()` | 获取上次推理耗时（毫秒） |
| `boolean isInitialized()` | 检查是否已初始化 |
| `void release()` | 释放模型资源 |

### OCRConfig 配置类

| 方法 | 类型 | 默认值 | 描述 |
|-----|------|-------|------|
| `setModelPath(String)` | String | null | 自定义模型路径（assets 目录下） |
| `setLabelPath(String)` | String | null | 自定义标签路径（assets 目录下） |
| `setCpuThreadNum(int)` | int | 4 | CPU 线程数 (1-4) |
| `setCpuPowerMode(String)` | String | "LITE_POWER_HIGH" | CPU 功耗模式 |
| `setDetLongSize(int)` | int | 960 | 检测图像长边尺寸 |
| `setScoreThreshold(float)` | float | 0.1f | 置信度阈值 (0.0-1.0) |
| `setUseOpenCL(boolean)` | boolean | false | 是否使用 GPU(OpenCL) 加速 |

**CPU 功耗模式说明：**
- `LITE_POWER_HIGH` - 高性能（推荐）
- `LITE_POWER_LOW` - 低功耗
- `LITE_POWER_FULL` - 满功耗
- `LITE_POWER_NO_BIND` - 不绑定核心
- `LITE_POWER_RAND_HIGH` - 随机高性能
- `LITE_POWER_RAND_LOW` - 随机低功耗

### OCRResult 结果类

| 方法 | 返回类型 | 描述 |
|-----|---------|------|
| `getPoints()` | `List<Point>` | 获取检测框的四个角坐标 |
| `getText()` | `String` | 获取识别的文字 |
| `getConfidence()` | `float` | 获取识别置信度 |
| `getDirection()` | `int` | 获取方向 (0=0°, 1=180°) |
| `getDirectionLabel()` | `String` | 获取方向标签 ("0" 或 "180") |
| `getDirectionConfidence()` | `float` | 获取方向分类置信度 |

## 权限要求

在 `AndroidManifest.xml` 中添加以下权限：

```xml
<!-- 相机权限（如需拍照功能） -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- 存储权限（如需从相册读取图片） -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 最佳实践

### 1. 线程安全

`PaddleOCR` 类不保证线程安全，建议：
- 在单线程中使用同一个实例
- 多线程时为每个线程创建独立实例
- 或使用锁机制保护共享实例

### 2. 性能优化

```java
// 1. 初始化建议放在子线程
new Thread(() -> {
    paddleOCR.init();
}).start();

// 2. 使用合适的线程数
OCRConfig config = new OCRConfig()
    .setCpuThreadNum(2); // 低端机建议 2，高端机 4

// 3. 调整图片尺寸
// 输入图片过大时可先缩小再识别
Bitmap scaledBitmap = Bitmap.createScaledBitmap(
    originalBitmap,
    originalBitmap.getWidth() / 2,
    originalBitmap.getHeight() / 2,
    true
);
```

### 3. 内存管理

```java
// 1. 及时释放资源
@Override
protected void onDestroy() {
    super.onDestroy();
    if (paddleOCR != null) {
        paddleOCR.release();
        paddleOCR = null;
    }
}

// 2. 使用完 Bitmap 后回收
if (resultBitmap != null && !resultBitmap.isRecycled()) {
    resultBitmap.recycle();
}
```

## 运行模式说明

| 模式 | runDet | runCls | runRec | 适用场景 |
|-----|--------|--------|--------|---------|
| 完整识别 | ✓ | ✓ | ✓ | 通用文本识别 |
| 检测+识别 | ✓ | ✗ | ✓ | 已知方向的文本 |
| 分类+识别 | ✗ | ✓ | ✓ | 已裁剪的文本区域 |
| 仅检测 | ✓ | ✗ | ✗ | 文本定位 |
| 仅识别 | ✗ | ✗ | ✓ | 已知位置和方向的文本 |
| 仅分类 | ✗ | ✓ | ✗ | 方向判断 |

## 示例应用

项目中的 `app` 模块包含完整示例应用，演示了：
- 拍照识别
- 相册选图识别
- 6 种运行模式切换
- 识别结果展示和绘制

安装示例应用后即可体验 OCR 功能。

## 从源码构建

### 环境准备
1. 安装 Android Studio
2. 安装 NDK r21.1.6352462
3. 安装 CMake 3.10.2

### 构建步骤

```bash
# Clone 项目
git clone <repository_url>
cd android_ocr_demo

# 构建 SDK
./gradlew :ocr-sdk:assembleRelease

# 生成 AAR 位置
# ocr-sdk/build/outputs/aar/ocr-sdk-release.aar
```

### 发布到 JitPack

项目已配置 `jitpack.yml`，推送到 GitHub 后即可通过 JitPack 引用。

## 常见问题

### Q1: 初始化失败怎么办？

A: 检查以下几点：
- 确保设备有足够内存（建议 2GB 以上）
- 检查模型文件是否存在于 assets 目录
- 尝试减少线程数和图像尺寸

### Q2: 识别速度慢？

A: 优化建议：
- 减小输入图片尺寸
- 减少 CPU 线程数
- 使用更简单的运行模式（如跳过分类）
- 尝试开启 GPU 加速（`setUseOpenCL(true)`）

### Q3: 支持多语言吗？

A: 默认内置中文简体模型。如需其他语言：
1. 下载对应语言的 PaddleOCR 模型
2. 将模型文件放入 `ocr-sdk/src/main/assets/models/`
3. 将字典文件放入 `ocr-sdk/src/main/assets/labels/`
4. 初始化时指定自定义路径：
   ```java
   OCRConfig config = new OCRConfig()
       .setModelPath("models/your_model")
       .setLabelPath("labels/your_dict.txt");
   ```

### Q4: 支持哪些设备架构？

A: 当前仅支持 `arm64-v8a`（ARM 64 位）。如需支持其他架构，修改 `ocr-sdk/build.gradle` 中的 `abiFilter` 配置。

## 技术支持

- PaddleOCR 官方文档：https://github.com/PaddlePaddle/PaddleOCR
- Paddle Lite 官方文档：https://github.com/PaddlePaddle/Paddle-Lite
- 问题反馈：提交 Issue

## 许可证

本项目基于 PaddleOCR 和 Paddle Lite，遵循相应的开源许可证。
