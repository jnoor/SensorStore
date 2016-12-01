//
// Created by Joseph Noor on 10/31/16.
//

#include <iostream>
#include <android/log.h>
#include <stdio.h>
#include <errno.h>
#include <sys/time.h>

using namespace std;

//Buffer Page Size
const unsigned long pagesize = 512L * 1024L;

//Buffer
char buffer[pagesize];

//Current Buffer Offset
unsigned long b_offset = 0L;

//Total Size of Circular Log
const unsigned long logsize = 2L * 1024L * 1024L * 1024L;

//Current Log Offset
unsigned long l_offset = 0L;

//Filename to Log File
const char * logfile;

//Filename to Log Offset File
const char * offsetfile;


//SETUP: Call this when you fist initialize the application to point to the proper Log file
//TODO: We should have a static parameter that will point to it
//NOTE: OS is restricting file access if we don't pass them in as a parameter
int DS_setup(const char * logfilename) {
    logfile = logfilename;
    cout << "logfile: " << logfilename << endl;

    //reset buffer
    memset(buffer, 0, pagesize*sizeof(char));
    b_offset = 0;

    return 0;
}

int flushBuffer();

//CLOSE: Call this when you are finished to guarantee the buffer is flushed to disk.
int DS_close() {
    cout << "closing SS stream" << endl;
    flushBuffer();
    return 0;
}

//Flush the in-memory buffer to disk
int flushBuffer() {
    cout << "flushing buffer" << endl;
//    __android_log_print(ANDROID_LOG_INFO, "SensorStore", "%s", "flushing");

    //TODO: overhead of opening file over and over to flush buffer?
    //dump buffer
    FILE* file = fopen(logfile, "r+b");

    if (file == NULL) {
        //file does not exist, create
        cout << "file does not exist, creating..." << endl;
        file = fopen(logfile, "wb");
        cout << errno << endl;
    }

    //seek to log offset position
    fseek(file, l_offset, SEEK_SET);

    //write buffer
    fwrite(buffer, 1, pagesize*sizeof(char), file);

    //close file
    fclose(file);

    //reset buffer
//    memset(buffer, 0, pagesize*sizeof(char));
    b_offset = 0;

    //update log offset
    l_offset = (l_offset + pagesize) % logsize;

    return 0;
}

//write the value to the buffer
//written as: (topic)(value_size)(value)
//returns: offset: log_offset + buffer_offset
bool DS_write(int topic, const char * value) {

    size_t valueSize = strlen(value);
    size_t size_to_write = sizeof(int) + sizeof(size_t) + valueSize;

    if (size_to_write > pagesize) {
        cout << "too much data at once! Limited to buffer size" << endl;
        return -1;
    }

    bool flushed = false;

    if (size_to_write + b_offset >= pagesize) {
        cout << "overflow, flushing..." << endl;
        flushBuffer();
        flushed = true;
    }

    long offset = l_offset + b_offset;

    //write topic
    memcpy(buffer + b_offset, &topic, sizeof(int));
    b_offset += sizeof(int);

    //write value size
    memcpy(buffer + b_offset, &valueSize, sizeof(size_t));
    b_offset += sizeof(size_t);

    //write value
    memcpy(buffer + b_offset, value, valueSize);
    b_offset += valueSize;

    return flushed;
}

long DS_current_offset() {
    return l_offset + b_offset;
}

int DS_read_all() {
    cout << "reading in data" << endl;

    //open file
    FILE* file = fopen(logfile, "rb");

    char readBuf[pagesize];
    char valueBuf[pagesize];
    fread(readBuf, sizeof(char), pagesize, file);
    fclose(file);

    int topic;
    size_t valuesize;
    char * value;

    unsigned long offset = 0;
    int counter = 0;

    while (offset < pagesize && counter < 10) {
        memcpy(&topic, readBuf + offset , sizeof(int));
        memcpy(&valuesize, readBuf + offset + sizeof(int), sizeof(size_t));

        memset(valueBuf, 0, pagesize);
        value = readBuf + offset + sizeof(int) + sizeof(size_t);
        memcpy(valueBuf, value, valuesize);

        cout << topic << " " << valuesize << " " << valueBuf << endl;

        offset = offset + sizeof(int) + sizeof(size_t) + valuesize;
        counter ++;
    }

    return 0;
}
