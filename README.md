默认最低 Android 运行版本为 Android 8.0，若版本过高，可修改 `app/build.gradle` 中的 `minSdkVersion`，具体配置可参考 https://medium.com/google-developers/picking-your-compilesdkversion-minsdkversion-targetsdkversion-a098a0341ebd 和 https://developer.android.com/guide/topics/manifest/uses-sdk-element?hl=zh-CN

## 生成
1. 使用 Android Studio，安装使用 Android 8.0 的 SDK 生成 APK
2. 直接下载 `app/build/outputs/apk/debug/app-debug.apk` 文件