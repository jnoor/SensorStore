#include <jni.h>
#include <string>

#include <android/log.h>

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
        jstring indexfile,
        jstring offsetfile) {

    const char *logFile = env->GetStringUTFChars(logfile, 0);
    const char *indexFile = env->GetStringUTFChars(indexfile, 0);
    const char *offsetFile = env->GetStringUTFChars(offsetfile, 0);
    __android_log_print(ANDROID_LOG_INFO, "SensorStoreSetup", "files:\n%s\n%s\n%s", logFile, indexFile, offsetFile);
}