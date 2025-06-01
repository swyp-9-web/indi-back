package com.swyp.artego.global.file.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class UploadRollbackEvent {

    private final List<String> savedFilenames;
}
