package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.util.SLORequestDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the body of the listResourcesBySLOs operation.
 *
 * @author matthi-g
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(using = SLORequestDeserializer.class)
public class ListResourcesBySLOsRequest extends SLORequest {}

