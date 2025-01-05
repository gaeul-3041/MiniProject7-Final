package com.aivle.mini7.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(unique = true, nullable = false)
    private String id; // PRIMARY KEY로 사용

    @Column(nullable = false)
    private String pw; // SQL의 "pw" 필드에 매핑

    @Column(nullable = false)
    private String name; // SQL의 "name" 필드에 매핑

    @Column(unique = true, nullable = false)
    private String phone; // SQL의 "phone" 필드에 매핑

    @Column(name = "id_type", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private int idType; // SQL의 "id_type" 필드에 매핑
}