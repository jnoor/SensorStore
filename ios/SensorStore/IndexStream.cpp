//
//  IndexStream.cpp
//  SensorStore
//
//  Created by Joseph Noor on 9/29/16.
//  Copyright © 2016 Joseph Noor. All rights reserved.
//

#include "IndexStream.hpp"

#include <iostream>
#include <stdio.h>
#include <sys/time.h>

using namespace std;

//Buffer Page Size
const unsigned long pagesize = 512L * 1024L;

//Index Buffer
char ibuffer[pagesize];

//Current Buffer Offset
unsigned long ib_offset = 0L;

//Total Size of Index
const unsigned long indexsize = 2L * 1024L * 1024L * 1024L;

//Current Index Log Offset
unsigned long il_offset = 0L;

//Filename to Log File
const char * index_filename;

//SETUP: Call this when you fist initialize the application to point to the proper Log file
//TODO: We should have a static parameter that will point to it.
int IS_setup(const char * file) {
    index_filename = file;
    cout << "filename: " << index_filename << endl;
    memset(ibuffer, 0, pagesize*sizeof(char));
    ib_offset = 0;
    return 0;
}
int flushIndexBuffer();

//CLOSE: Call this when you are finished to guarantee the buffer is flushed to disk.
int IS_close() {
    cout << "closing SS stream" << endl;
    flushIndexBuffer();
    return 0;
}

//Flush the in-memory buffer to disk
int flushIndexBuffer() {
    cout << "flushing buffer" << endl;
    return 0;
    
    //TODO: overhead of opening file over and over to flush buffer?
    //dump buffer
    FILE* file = fopen(index_filename, "r+b");
    
    if (file == NULL) {
        //file does not exist, create
        file = fopen(index_filename, "wb");
    }
    
    //seek to log offset position
    //    fseek(file, l_offset, SEEK_SET);
    
    //write buffer
    fwrite(ibuffer, 1, pagesize*sizeof(char), file);
    
    //close file
    fclose(file);
    
    //reset buffer
    memset(ibuffer, 0, pagesize*sizeof(char));
    ib_offset = 0;
    
    //update log offset
    il_offset = (il_offset + pagesize) % indexsize;
    
    return 0;
}

//write the value to the buffer
//written as: (timestamp)(topic)(value_size)(value)
//returns: offset: log_offset + buffer_offset
long IS_write(int topic, long logoffset) {
    size_t size_to_write = sizeof(long) + sizeof(int);
    
    if (size_to_write + ib_offset >= pagesize) {
        cout << "overflow, flushing..." << endl;
        flushIndexBuffer();
    }
    
    long offset = il_offset + ib_offset;
    
    //write topic
    memcpy(ibuffer + ib_offset, &topic, sizeof(int));
    ib_offset += sizeof(int);
    
    //write log offset
    memcpy(ibuffer + ib_offset, &logoffset, sizeof(long));
    ib_offset += sizeof(long);
    
    return offset;
}
