package com.swyp.artego.domain.user.enums;

public enum Role {
    USER,ARTIST,ADMIN;

    public boolean isArtist() {
        return this == ARTIST;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }


}
