#include <jni.h>
#include <string>

#include <android/log.h>
#include "SensorStore.cpp"

extern "C"
jstring
Java_edu_ucla_cs_jet_sensorstore_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
void
Java_edu_ucla_cs_jet_sensorstore_MainActivity_SensorStoreSetup(
        JNIEnv *env,
        jobject callingObject /* this */,
        jstring logfile,
        jstring indexfile) {

    const char *logFile = env->GetStringUTFChars(logfile, 0);
    const char *indexFile = env->GetStringUTFChars(indexfile, 0);
    __android_log_print(ANDROID_LOG_INFO, "SensorStoreSetup", "files:\n%s\n%s", logFile, indexFile);
    SS_setup(logFile, indexFile);
}

extern "C"
void
Java_edu_ucla_cs_jet_sensorstore_MainActivity_SensorStoreWrite(
        JNIEnv *env,
        jobject callingObject /* this */,
        jint topic,
        jstring value) {


    const int top = (int) topic;
    const char *val = env->GetStringUTFChars(value, 0);

//    if(SS_write(top,val)) {
//        __android_log_print(ANDROID_LOG_INFO, "SensorStoreWrite", "%s", "FLUSHED");
//    }
}

extern "C"
void
Java_edu_ucla_cs_jet_sensorstore_MainActivity_SensorStoreClose(
        JNIEnv *env,
        jobject callingObject /* this */) {
    __android_log_print(ANDROID_LOG_INFO, "SensorStoreClose", "probably flushing");
    SS_close();
}