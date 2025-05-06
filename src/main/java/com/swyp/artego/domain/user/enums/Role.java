package com.swyp.artego.domain.user.enums;

public enum Role {
    USER, ARTIST;

    public boolean isArtist() {
        return this == ARTIST;
    }
}
