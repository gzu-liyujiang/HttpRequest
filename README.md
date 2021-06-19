# HttpRequest

![Release APK](https://github.com/gzu-liyujiang/HttpRequest/workflows/Release%20APK/badge.svg)
[![jitpack](https://jitpack.io/v/gzu-liyujiang/HttpRequest.svg)](https://jitpack.io/#gzu-liyujiang/HttpRequest)

自用的 Android/Java 网络请求组件，面向接口编程，使用接口对各模块进行解耦，增强对第三方库的管控，底层可无缝切换底层的具体实现。默认实现了 okhttp-OkGo 及 Fast-Android-Networking 。


```groovy
    allprojects {
        repositories {
            maven { url 'https://www.jitpack.io' }
        }
    }
```

```groovy
    dependencies {
        implementation 'com.github.gzu-liyujiang:HttpRequest:版本号'
        runtimeOnly 'com.lzy.net:okgo:3.0.4'
        //runtimeOnly 'com.amitshekhar.android:android-networking:1.0.2'
    }
```
