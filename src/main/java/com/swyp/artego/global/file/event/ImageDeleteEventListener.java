package com.swyp.artego.global.file.event;


import com.swyp.artego.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageDeleteEventListener {

    private final FileService fileService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deletePreviousImage(ImageDeleteEvent event) {
        try {
            log.info("[ImageDeleteEventListener] DB 트랜잭션 커밋 감지. 기존 이미지 삭제를 시작합니다. imgUrl: {}", event.getPreviousFilenames());

            List<String> deletedKeys = event.getPreviousFilenames().stream()
                    .map(fileService::extractKeyFromImgUrl)
                    .toList();
            fileService.deleteFiles(deletedKeys);
        } catch (Exception e) {
            log.error("[ImageDeleteEventListener] 커밋 시 이미지 삭제 실패", e);
        }
    }
}
