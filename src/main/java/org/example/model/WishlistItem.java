package org.example.model;

public class WishlistItem {

    private String name;
    private boolean taken;
    private Long takenByUserId; // может быть null
    private String link;

    public WishlistItem() {
    }

    public WishlistItem(String name, boolean taken, Long takenByUserId, String link) {
        this.name = name;
        this.taken = taken;
        this.takenByUserId = takenByUserId;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public Long getTakenByUserId() {
        return takenByUserId;
    }

    public void setTakenByUserId(Long takenByUserId) {
        this.takenByUserId = takenByUserId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
