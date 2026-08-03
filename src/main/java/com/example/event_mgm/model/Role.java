package com.example.event_mgm.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;  // ADMIN, JUDGE, etc.

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "role")
    private List<User> users = new ArrayList<>();
}