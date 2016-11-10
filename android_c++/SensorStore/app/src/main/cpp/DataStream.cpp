//
// Created by Joseph Noor on 10/31/16.
//

#include <iostream>
#include <stdio.h>
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


void loadOffset(const char * offsetfilename) {
    offsetfile = offsetfilename;

    FILE* file = fopen(offsetfile, "r+b");
    if (file == NULL) {
        //file does not exist, no log offset
        l_offset = 0L;
    } else {
        //read offset into buffer
        char readBuf[sizeof(long)];
        fread(readBuf, sizeof(char), sizeof(long), file);
        fclose(file);

        //read offset from buffer
        memcpy(&l_offset, readBuf , sizeof(long));
    }

    cout << "Log offset at " << l_offset << endl;
}

int writeOffset() {
    FILE* file = fopen(offsetfile, "wb");
    if (file == NULL) {
        //ERROR! Probably OS access issues
        return -1;
    }

    //write offset to buffer
    char buff[sizeof(long)];
    memcpy(buff, &l_offset, sizeof(long));

    //write buffer to file
    fwrite(buff, sizeof(char), sizeof(long), file);

    fclose(file);

    return 0;
}

//SETUP: Call this when you fist initialize the application to point to the proper Log file
//TODO: We should have a static parameter that will point to it
//NOTE: OS is restricting file access if we don't pass them in as a parameter
int DS_setup(const char * logfilename, const char * offsetfilename) {
    logfile = logfilename;
    cout << "logfile: " << logfilename << endl;

    //reset buffer
    memset(buffer, 0, pagesize*sizeof(char));
    b_offset = 0;

    //load offsetfile variable
    loadOffset(offsetfilename);

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
//    fseek(file, l_offset, SEEK_SET);

    //write buffer
    fwrite(buffer, 1, pagesize*sizeof(char), file);

    //close file
    fclose(file);

    //reset buffer
    memset(buffer, 0, pagesize*sizeof(char));
    b_offset = 0;

    //update log offset
    l_offset = (l_offset + pagesize) % logsize;

    writeOffset();

    return 0;
}

//write the value to the buffer
//written as: (topic)(value_size)(value)
//returns: offset: log_offset + buffer_offset
long DS_write(int topic, char * value) {

    size_t valueSize = strlen(value);
    size_t size_to_write = sizeof(long) + sizeof(int) + sizeof(size_t) + valueSize;

    if (size_to_write > pagesize) {
        cout << "too much data at once! Limited to buffer size" << endl;
        return -1;
    }

    if (size_to_write + b_offset >= pagesize) {
        cout << "overflow, flushing..." << endl;
        flushBuffer();
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

    return offset;
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
