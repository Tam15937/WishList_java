package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "items")
public class ListItemModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private ListModel list;

    @NotBlank
    private String name;
    private String link;
    private boolean taken;
    private Long takenByUserId;

    public ListItemModel() {}

    // геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isTaken() { return taken; }
    public void setTaken(boolean taken) { this.taken = taken; }

    public Long getTakenByUserId() { return takenByUserId; }
    public void setTakenByUserId(Long takenByUserId) { this.takenByUserId = takenByUserId; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public ListModel getList() { return list; }
    public void setList(ListModel list) { this.list = list; }
}
