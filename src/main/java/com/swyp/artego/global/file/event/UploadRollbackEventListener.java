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
public class UploadRollbackEventListener {

    private final FileService fileService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void rollbackUploadImage(UploadRollbackEvent event) {
        try {
            log.info("[UploadRollbackEventListener] DB 트랜잭션 롤백 감지. 업로드된 이미지 삭제를 시작합니다. imgUrl: {}", event.getSavedFilenames());

            List<String> deletedKeys = event.getSavedFilenames().stream()
                    .map(fileService::extractKeyFromImgUrl)
                    .toList();
            fileService.deleteFiles(deletedKeys);
        } catch (Exception e) {
            log.error("[UploadRollbackEventListener] 롤백 시 이미지 삭제 실패", e);
        }
    }
}
