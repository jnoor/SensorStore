//
//  SensorStore.hpp
//  SensorStore
//
//  Created by Joseph Noor on 10/17/16.
//  Copyright © 2016 Joseph Noor. All rights reserved.
//

#ifndef SensorStore_hpp
#define SensorStore_hpp

#include <stdio.h>

__BEGIN_DECLS
int SS_setup(char * logfilename, char * indexfilename, char * logoffsetfilename);
long SS_write(int topic, char * value);
int SS_close();
long SS_current_offset();
int SS_read_all();
__END_DECLS


#endif /* SensorStore_hpp */
