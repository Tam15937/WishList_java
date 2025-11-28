package org.example.data.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "items")
public class ListItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    @JsonBackReference(value = "list")
    private ListModel list;

    @NotBlank
    private String name;
    private String link;
    private boolean taken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "takenByUser_id", nullable = true)//taken_by_user
    @JsonBackReference
    private UserModel takenByUser;
}// ENTITY
