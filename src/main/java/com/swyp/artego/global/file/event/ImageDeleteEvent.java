package com.swyp.artego.global.file.event;

import lombok.Getter;

import java.util.List;

@Getter
public class ImageDeleteEvent {

    private final List<String> previousFilenames;

    public ImageDeleteEvent(List<String> filenames) {
        this.previousFilenames = filenames;
    }

    public ImageDeleteEvent(String filename) {
        this.previousFilenames = List.of(filename);
    }
}
