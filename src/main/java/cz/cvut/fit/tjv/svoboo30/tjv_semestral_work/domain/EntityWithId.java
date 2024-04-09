package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.domain;

import java.io.Serializable;

public interface EntityWithId<ID> extends Serializable {
    ID getId();
}

