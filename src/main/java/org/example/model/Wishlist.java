package org.example.model;

import java.util.List;

public class Wishlist {

    private long id;
    private String name;
    private List<WishlistItem> wishlist;
    private String username;
    private long userId;

    public Wishlist() {
    }

    public Wishlist(long id, String name, List<WishlistItem> wishlist, String username, long userId) {
        this.id = id;
        this.name = name;
        this.wishlist = wishlist;
        this.username = username;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WishlistItem> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<WishlistItem> wishlist) {
        this.wishlist = wishlist;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
