//
// Created by Joseph Noor on 10/31/16.
//

#include "DataStream.cpp"

int SS_setup(const char * datafilename, const char * indexfilename) {

//    IS_setup(indexfilename);
    DS_setup(datafilename);

    return 0;
}
long SS_write(int topic, const char * value) {
    long logoffset = DS_write(topic, value);
//    if (logoffset != -1L) {
//        IS_write(topic, logoffset);
//    }
    return logoffset;
}
int SS_close() {
//    IS_close();
    DS_close();
    return 0;
}
long SS_current_offset() {
    return DS_current_offset();
//    return 0;
}

int SS_read_all() {
    return DS_read_all();
//    return 0;
}
