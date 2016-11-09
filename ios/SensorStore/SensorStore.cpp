//
//  SensorStore.cpp
//  SensorStore
//
//  Created by Joseph Noor on 10/17/16.
//  Copyright © 2016 Joseph Noor. All rights reserved.
//

#include "SensorStore.hpp"
#include "DataStream.hpp"
#include "IndexStream.hpp"
#include <cstring>
#include <string>

using namespace std;

int SS_setup(char * datafilename, char * indexfilename, char * logoffsetfilename) {
    
//    IS_setup(indexfilename);
    DS_setup(datafilename, logoffsetfilename);
    
    return 0;
}
long SS_write(int topic, char * value) {
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
}

int SS_read_all() {
    return DS_read_all();
}
