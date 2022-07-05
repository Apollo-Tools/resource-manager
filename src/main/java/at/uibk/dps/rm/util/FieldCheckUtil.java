package at.uibk.dps.rm.util;

import io.vertx.core.http.HttpMethod;

public class FieldCheckUtil {

    private final long postLimit;
    private final long patchMinimum;

    public FieldCheckUtil(long postLimit, long patchMinimum) {
        this.postLimit = postLimit;
        this.patchMinimum = patchMinimum;
    }

    public boolean checkAcceptedFields(HttpMethod httpMethod, long acceptedFields,
        long requestFields) {
        if (HttpMethod.POST.equals(httpMethod)) {
            return acceptedFields != postLimit || acceptedFields != requestFields;
        } else if (HttpMethod.PATCH.equals(httpMethod)) {
            return acceptedFields <= patchMinimum || acceptedFields != requestFields;
        }
        return false;
    }
}
