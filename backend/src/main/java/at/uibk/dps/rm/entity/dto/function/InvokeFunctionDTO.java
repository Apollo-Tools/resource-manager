package at.uibk.dps.rm.entity.dto.function;

import lombok.Getter;
import lombok.Setter;

/**
 * A DTO that represents the response data consisting of the status code and response body from a
 * function invocation.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class InvokeFunctionDTO {

    private int statusCode;

    private String body;
}
