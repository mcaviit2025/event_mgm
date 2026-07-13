package com.example.event_mgm.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "registrations")
public class TeamMembers extends  BaseEntity{

    @Column(nullable = false,length = 50)
    private String firstName;

    @Column(nullable = false,length = 50)
    private String middleName;

    @Column(nullable = false,length = 50)
    private String lastName;

    @Column(nullable = false,length = 50)
    private String email;

    @ManyToOne
    @JoinColumn(name = "registration_id")
    private  Registration registration;

}
