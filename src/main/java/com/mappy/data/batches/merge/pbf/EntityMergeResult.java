package com.mappy.data.batches.merge.pbf;

import com.google.common.base.Preconditions;

import lombok.*;
import lombok.experimental.Wither;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.*;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class EntityMergeResult {

    private EntityContainer ec1;
    private EntityContainer ec2;
    private EntityContainer result;
    private boolean merged;

    public EntityMergeResult merge() {
        if (ec1 == null) {
            return ec2();
        }
        if (ec2 == null) {
            return ec1();
        }
        if (ec2.getEntity().getType().ordinal() > ec1.getEntity().getType().ordinal()) {
            return ec1();
        }
        if (ec1.getEntity().getType().ordinal() > ec2.getEntity().getType().ordinal()) {
            return ec2();
        }

        EntityType type = ec1.getEntity().getType();
        Preconditions.checkState(ec2.getEntity().getType() == type);

        if (type == EntityType.Bound) {
            return mergeTags();
        }
        if (ec2.getEntity().getId() > ec1.getEntity().getId()) {
            return ec1();
        }
        if (ec1.getEntity().getId() > ec2.getEntity().getId()) {
            return ec2();
        }
        return mergeTags();
    }

    private EntityMergeResult mergeTags() {
        Entity entity1 = ec1.getEntity();
        Entity entity2 = ec2.getEntity();
        entity1.getTags().addAll(entity2.getTags());
        return new EntityMergeResult(null, null, ec1, true);
    }

    private EntityMergeResult ec2() {
        return new EntityMergeResult(ec1, null, ec2, false);
    }

    private EntityMergeResult ec1() {
        return new EntityMergeResult(null, ec2, ec1, false);
    }
}
