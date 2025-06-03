package com.swyp.artego.global.file.event;

import lombok.Getter;

import java.util.List;

@Getter
public class UploadRollbackEvent {

    private final List<String> savedFilenames;

    public UploadRollbackEvent(List<String> filenames) {
        this.savedFilenames = filenames;
    }

    public UploadRollbackEvent(String filename) {
        this.savedFilenames = List.of(filename);
    }
}
