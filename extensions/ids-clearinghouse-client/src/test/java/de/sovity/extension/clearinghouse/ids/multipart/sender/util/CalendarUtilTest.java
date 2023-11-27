package de.sovity.extension.clearinghouse.ids.multipart.sender.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalendarUtilTest {

    private CalendarUtil calendarUtil;
    @Test
    void gregorianNow() {
       var now =  CalendarUtil.gregorianNow();
       assertNotNull(now);
    }
}
