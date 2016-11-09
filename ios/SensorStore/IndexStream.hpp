//
//  IndexStream.hpp
//  SensorStore
//
//  Created by Joseph Noor on 9/29/16.
//  Copyright © 2016 Joseph Noor. All rights reserved.
//

#ifndef IndexStream_hpp
#define IndexStream_hpp

#include <stdio.h>

__BEGIN_DECLS
int IS_setup(const char * filename);
long IS_write(int topic, long offset);
int IS_close();
__END_DECLS


#endif /* IndexStream_hpp */
