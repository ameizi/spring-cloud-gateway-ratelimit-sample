package com.anchnet.gateway.utils;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

import java.time.Duration;

public class Bucket4jUtil {

    public static Bucket getBucket() {
        Bandwidth limit = Bandwidth.classic(1, Refill.of(1, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

}
