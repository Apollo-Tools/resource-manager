package at.uibk.dps.rm.util.misc;

import lombok.experimental.UtilityClass;

import java.util.Calendar;
import java.util.Date;

/**
 * This class is a utility class for dates.
 *
 * @author matthi-g
 */
@UtilityClass
public class DateHelper {

    /**
     * Get the current date-timestamp with an offset in minutes.
     *
     * @param offsetMinutes the offset in minutes
     * @return the current date
     */
    public static Date getDate(int offsetMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, offsetMinutes);
        return cal.getTime();
    }
}
