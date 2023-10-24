package at.uibk.dps.rm.entity.misc;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the retry count that is used to keep track of the amount of retries of database
 * transactions. Used at {@link at.uibk.dps.rm.service.database.util.SessionManagerProvider}.
 *
 * @author matthi-g
 */
@Getter
@NoArgsConstructor
public class RetryCount {
    int retryCount = 0;

    /**
     * Return the current value and increment it afterward.
     *
     * @return the current value before incrementing it
     */
    public int increment() {
        int oldValue = retryCount;
        this.retryCount += 1;
        return oldValue;
    }
}
