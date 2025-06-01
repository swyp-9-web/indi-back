package com.swyp.artego.global.file.event;


import com.swyp.artego.global.file.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class) // 로그 캡처용
class UploadRollbackEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Mock
    private FileService fileService;

    @Test
    @DisplayName("트랜잭션 롤백 시 UploadRollbackEventListener가 이미지 삭제 로그를 출력해야 함")
    void rollbackEventTriggersDelete(CapturedOutput output) {
        // given
        List<String> imgUrls = List.of("https://img-url-of-test-image1.jpg", "https://img-url-of-test-image2.jpg");
        UploadRollbackEvent event = new UploadRollbackEvent(imgUrls);

        // when
        // 트랜잭션 시작 후 강제 예외로 롤백 유도
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        assertThrows(RuntimeException.class, () -> {
            txTemplate.executeWithoutResult(status -> {
                eventPublisher.publishEvent(event);
                throw new RuntimeException("강제 롤백");
            });
        });

        // then
        // 로그 출력으로 리스너 실행 확인
        assertThat(output).contains("DB 트랜잭션 롤백 감지", "삭제 완료");
    }
}