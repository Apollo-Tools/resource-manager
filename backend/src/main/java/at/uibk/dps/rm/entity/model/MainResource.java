package at.uibk.dps.rm.entity.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the resource entity with type main.
 *
 * @author matthi-g
 */
@Entity
@DiscriminatorValue("main")
@Getter
@Setter
public class MainResource extends Resource {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "region_id")
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id")
    private Platform platform;


    @OneToMany(mappedBy = "mainResource")
    private List<SubResource> subResources = new ArrayList<>();
}
