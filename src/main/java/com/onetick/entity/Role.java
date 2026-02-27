package com.onetick.entity;

import com.onetick.entity.enums.RoleName;
import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private RoleName name;

    public RoleName getName() {
        return name;
    }

    public void setName(RoleName name) {
        this.name = name;
    }
}
