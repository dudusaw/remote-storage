package org.example.domain.service.impl;

import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;

public class MyObjectDecoder extends ObjectDecoder {

    public MyObjectDecoder() {
        super(ClassResolvers.weakCachingResolver(null));
    }
}
