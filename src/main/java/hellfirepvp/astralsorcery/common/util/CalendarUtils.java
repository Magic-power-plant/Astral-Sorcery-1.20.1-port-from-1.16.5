package hellfirepvp.astralsorcery.common.util;

import java.time.LocalDateTime;
import java.time.Month;

public class CalendarUtils {

    public static boolean isAprilFirst() {
        LocalDateTime date = LocalDateTime.now();
        return date.getMonth() == Month.APRIL && date.getDayOfMonth() == 1;
    }
}
