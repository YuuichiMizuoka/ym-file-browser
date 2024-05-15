package net.vamoscantar.utils;

import java.time.ZonedDateTime;

public class TimeUtils {

    public static long toEpoch(ZonedDateTime time) {
        return time.toInstant().toEpochMilli();
    }

}
