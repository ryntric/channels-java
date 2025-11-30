package io.github.ryntric;

import sun.misc.Unsafe;

interface Constants {
    int CACHE_LINE_SIZE = 64;

    int OBJECT_ARRAY_PADDING = CACHE_LINE_SIZE / Unsafe.ARRAY_OBJECT_INDEX_SCALE;

    int BYTE_BUFFER_PADDING =  64;
}
