package at.uibk.dps.rm.entity.misc;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RetryCount {
    int retryCount = 0;

    public int increment() {
        int oldValue = retryCount;
        this.retryCount += 1;
        return oldValue;
    }
}
