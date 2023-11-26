package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Represents the aws_price entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class AwsPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long awsPriceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;


    @Column(columnDefinition = "text")
    private String instanceType;

    @Column(precision = 8, scale = 4)
    @Type(type = "big_decimal")
    private BigDecimal price;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AwsPrice awsPrice = (AwsPrice) obj;
        return awsPriceId.equals(awsPrice.awsPriceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return awsPriceId.hashCode();
    }
}
