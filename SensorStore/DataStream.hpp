//
//  DataStream.hpp
//  SensorStore
//
//  Created by Joseph Noor on 9/29/16.
//  Copyright © 2016 Joseph Noor. All rights reserved.
//

#ifndef DataStream_hpp
#define DataStream_hpp

#include <stdio.h>

__BEGIN_DECLS
int DS_setup(const char * logfilename, const char * logoffsetfilename);
long DS_write(int topic, char * value);
int DS_close();
long DS_current_offset();
int DS_read_all();
__END_DECLS

#endif /* DataStream_hpp */
