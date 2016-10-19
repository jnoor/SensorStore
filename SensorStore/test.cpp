//
//  test.cpp
//  SensorStore
//
//  Created by Joseph Noor on 9/26/16.
//  Copyright © 2016 Joseph Noor. All rights reserved.
//

#include "test.hpp"
#include <iostream>
#include <stdio.h>

using namespace std;

/*
const unsigned int pagesize = 512 * 1024;
char buffer[pagesize];
unsigned int offset = 0;

char * filename;

int SS_setup(char * file) {
    filename = file;
    memset(buffer, 0, pagesize*sizeof(char));
    offset = 0;
    return 0;
}

int flushBuffer() {
    cout << "flushing buffer" << endl;
    
    //dump buffer
    //TODO: overhead of opening file over and over to flush buffer?
    FILE* pFile;
    pFile = fopen(filename, "wb");
    fwrite(buffer, 1, pagesize*sizeof(char), pFile);
    fclose(pFile);
    
    //reset buffer
    memset(buffer, 0, pagesize*sizeof(char));
    offset = 0;
    return 0;
}

int SS_close() {
    cout << "closing SS stream" << endl;
    flushBuffer();
    return 0;
}

int SS_read_all() {
    cout << "reading in data" << endl;
    
    //open file
    FILE* pFile;
    pFile = fopen(filename, "rb");
    
    char readBuf[pagesize];
    fread(readBuf, sizeof(char), pagesize, pFile);
    fclose(pFile);
    
    long timestamp;
    int topic;
    char * value;
    
    memcpy(&timestamp, readBuf, sizeof(long));
    memcpy(&topic, readBuf + sizeof(long), sizeof(int));
    value = readBuf + sizeof(long) + sizeof(int);
    
    
    cout << "read data in..." << endl;
    cout << timestamp << " " << topic << " " << value << endl;
    cout << "---" << endl;
    
    return 0;
}

//write the value to the buffer
//written as: (timestamp)(topic)(value)\n
int SS_send(int topic, char * value) {
    long timestamp = time(NULL);
    
    size_t valueSize = strlen(value);
    size_t size_to_write = sizeof(long) + sizeof(int) + valueSize + 1;
    
    if (size_to_write > pagesize) {
        cout << "too much data at once! limit to 512kB (buffer size)" << endl;
        return -1;
    }
    
    if (size_to_write + offset >= pagesize) {
        cout << "overflow, flushing..." << endl;
        flushBuffer();
    }
    
    //write timestamp
    memcpy(buffer + offset, &timestamp, sizeof(long));
    offset += sizeof(long);
    
    //write topic
    memcpy(buffer + offset, &topic, sizeof(int));
    offset += sizeof(int);
    
    //write value
    memcpy(buffer + offset, value, valueSize);
    offset += valueSize;
    
    //end with newline
    buffer[offset++] = 0;
    offset += 1;
    
    return 0;
}

//--------------------------------------------------------------------------------
//For reference:
int SS_writeQuickly() {
    
    cout << "starting write test" << endl;
    
    FILE* pFile;
    pFile = fopen(filename, "wb");
    for (int j = 0; j < 16; ++j) {
        //fill buffer
        cout << "writing 512kB" << endl;
        fwrite(buffer, 1, pagesize*sizeof(char), pFile);
    }
    fclose(pFile);
    
    cout << "finished write test" << endl;
    
    return 0;
}
*/
