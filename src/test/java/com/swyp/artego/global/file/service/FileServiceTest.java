package com.swyp.artego.global.file.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.file.dto.response.FileResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
class FileServiceTest {

    @Autowired
    private FileService fileService;

    @MockitoBean
    private AmazonS3 amazonS3;

    private final String bucketName = "testBucket";

    private final String folderName = "testFolder";
    private List<MultipartFile> mockMultipartFiles;

    private List<FileResponse> uploadedFileResponses;

    /**
     * [테스트 실행 전] 올바른 형식의 mock MultipartFile 리스트를 초기화합니다.
     */
    @BeforeEach
    void setUp() {
        mockMultipartFiles = new ArrayList<>();
        mockMultipartFiles.add(new MockMultipartFile(
                "files", "test-image1.jpg", "image/jpeg", "Dummy Image Content 1".getBytes()));
        mockMultipartFiles.add(new MockMultipartFile(
                "files", "test-image2.png", "image/png", "Dummy Image Content 2".getBytes()));

        when(amazonS3.deleteObjects(
                any(DeleteObjectsRequest.class))
        ).thenReturn(createMockDeleteObjectsResult("test-image1.jpg", "test-image2.png"));

        when(amazonS3.doesObjectExist(
                anyString(),
                anyString())
        ).thenReturn(true);
    }

    /**
     * [테스트 실행 후] 남아있는 파일을 모두 삭제합니다.
     */
    @AfterEach
    void tearDown() {
        if (uploadedFileResponses == null) {
            System.out.println("[TEARDOWN] 남은 파일이 없습니다. tearDown() 자동 종료.");
            return;
        }

        for (FileResponse fileResponse : uploadedFileResponses) {
            String keyName = folderName + "/" + fileResponse.getUploadFileName();
            if (amazonS3.doesObjectExist(bucketName, keyName)) {
                amazonS3.deleteObject(bucketName, keyName);
                System.out.println("[TEARDOWN] 남은 파일 삭제: " + keyName);
            }
        }
    }

    /**
     * [테스트] 파일 업로드 - 아무 이상 없는 경우
     */
    @Test
    void uploadFiles() {
        // when
        uploadedFileResponses = fileService.uploadFiles(mockMultipartFiles, folderName);

        // then
        assertEquals(2, uploadedFileResponses.size(), "업로드된 파일 개수가 예상과 다릅니다.");

        verify(amazonS3, times(2))
                .putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    /**
     * [테스트] 파일 업로드 - 정확한 이미지 파일이 아닌 파일이 존재하는 경우
     */
    @Test
    void uploadFiles_validateFilesExtension_INVALID_FILE() {
        // given
        List<MockMultipartFile> invalidFiles = List.of(
                new MockMultipartFile("files", "test-doc1.doc", "application/msword", "Dummy MS Doc Content 1".getBytes()),
                new MockMultipartFile("files", null, "image/png", "Dummy Null Image Content".getBytes()),
                new MockMultipartFile("files", "no-extension", "image/jpeg", "Dummy No Extension Image Content".getBytes()));

        // when + then
        for (MockMultipartFile invalidFile : invalidFiles) {
            BusinessExceptionHandler exception = assertThrows(
                    BusinessExceptionHandler.class,
                    () -> fileService.uploadFiles(List.of(invalidFile), folderName)
            );

            assertEquals(ErrorCode.INVALID_FILE, exception.getErrorCode());
        }
    }

    /**
     * [테스트] 파일 업로드 도중 발생하는 IOException 을 검증합니다.
     * IOException 강제 발생
     *
     * 이 과정에서 업로드된 사진을 삭제(롤백)하는 테스트를 포함합니다.
     */
    @Test
    void uploadFiles_IOException() {
        // given
        mockMultipartFiles.add(new IOExceptionMultipartFile(
                "files", "test-IOFail-image.jpg", "image/jpeg", "Fail Content".getBytes()));

        // when + then
        BusinessExceptionHandler exception = assertThrows(
                BusinessExceptionHandler.class,
                () -> fileService.uploadFiles(mockMultipartFiles, folderName)
        );

        assertEquals(ErrorCode.IO_ERROR, exception.getErrorCode());
    }

    static class IOExceptionMultipartFile extends MockMultipartFile {

        public IOExceptionMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            super(name, originalFilename, contentType, content);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            throw new IOException("강제 발생된 테스트용 IOException");
        }
    }

    /**
     * [테스트] 파일 업로드 도중 발생하는 SdkClientException 을 검증합니다.
     * SdkClientException 강제 발생
     */
    @Test
    void uploadFiles_SdkClientException() {
        // given
        mockMultipartFiles.add(new MockMultipartFile(
                "files", "test-image-fail.jpg", "image/jpeg", "Dummy Content".getBytes()));

        when(amazonS3.putObject(
                anyString(),
                anyString(),
                any(InputStream.class),
                any(ObjectMetadata.class))
        ).thenThrow(new SdkClientException("강제 발생 테스트용 SdkClientException"));

        // when + then
        BusinessExceptionHandler exception = assertThrows(
                BusinessExceptionHandler.class,
                () -> fileService.uploadFiles(mockMultipartFiles, folderName)
        );

        assertEquals(ErrorCode.AMAZON_S3_API_ERROR, exception.getErrorCode());
    }

    @Test
    void deleteFile() {
    }

    @Test
    void getUuidFileName() {
    }

    private DeleteObjectsResult createMockDeleteObjectsResult(String... keys) {
        List<DeleteObjectsResult.DeletedObject> deletedObjects = Arrays.stream(keys)
                .map(key -> {
                    DeleteObjectsResult.DeletedObject obj = new DeleteObjectsResult.DeletedObject();
                    obj.setKey(key);
                    return obj;
                })
                .collect(Collectors.toList());
        return new DeleteObjectsResult(deletedObjects);
    }

}