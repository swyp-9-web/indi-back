package com.swyp.artego.global.file.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.file.dto.response.FileUploadResponseExample;
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
    private List<String> fileUrls;

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
        ).thenReturn(createMockDeleteObjectsResult(folderName + "/test-image1.jpg", folderName + "/test-image2.png"));

        when(amazonS3.doesObjectExist(
                anyString(),
                anyString())
        ).thenReturn(true);

        when(amazonS3.putObject(
                anyString(),
                anyString(),
                any(InputStream.class),
                any(ObjectMetadata.class))
        ).thenReturn(new PutObjectResult());
    }

    /**
     * [테스트 실행 후] 남아있는 파일을 모두 삭제합니다.
     */
    @AfterEach
    void tearDown() {
        if (fileUrls == null) {
            System.out.println("[TEARDOWN] 남은 파일이 없습니다. tearDown() 자동 종료.");
            return;
        }

        for (String fileUrl : fileUrls) {
            String key = fileService.extractKeyFromImgUrl(fileUrl);
            if (amazonS3.doesObjectExist(bucketName, key)) {
                amazonS3.deleteObject(bucketName, key);
                System.out.println("[TEARDOWN] 남은 파일 삭제: " + key);
            }
        }
    }

    /**
     * [테스트] uploadFile 메서드 후 deleteFile 메서드 - 정상적인 단일 파일 업로드와 삭제 성공 검증
     *
     * 유효한 이미지 파일을 업로드 후 반환된 URL로 삭제 요청 시,
     * S3 putObject와 deleteObject가 각각 1회 호출되어야 한다.
     */
    @Test
    void uploadAndDeleteFile_shouldWorkSuccessfully_whenValidFileProvided() {
        // given
        MockMultipartFile mockMultipartFile =
                new MockMultipartFile("files", "test-image1.jpg", "image/jpeg", "Dummy Image Content".getBytes());

        // when
        FileUploadResponseExample response = fileService.uploadFile(mockMultipartFile, folderName);
        String imgUrl = response.getUploadFileUrl();

        fileService.deleteFile(imgUrl);

        // then
        String key = response.getUploadFilePath() + "/" + response.getUploadFileName();

        verify(amazonS3, times(1))
                .putObject(anyString(), eq(key), any(InputStream.class), any(ObjectMetadata.class));

        verify(amazonS3, times(1))
                .deleteObject(anyString(), eq(key));
    }

    /**
     * [테스트] uploadFiles 메서드 - 정상적인 파일 리스트가 주어질 때 전체 파일 업로드 성공 검증
     *
     * 유효한 이미지 파일 리스트를 업로드하는 경우, S3 putObject가 파일 수만큼 호출되고
     * 각 파일에 대해 FileResponse가 정상적으로 반환되어야 한다.
     */
    @Test
    void uploadFiles_shouldUploadAllFilesSuccessfully_whenValidFilesProvided() {
        // when
        fileUrls = fileService.uploadFiles(mockMultipartFiles, folderName);

        // then
        assertEquals(2, fileUrls.size(), "업로드된 파일 개수가 예상과 다릅니다.");

        verify(amazonS3, times(2))
                .putObject(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
    }

    /**
     * [테스트] uploadFiles 메서드 - 유효하지 않은 파일 확장자가 포함된 경우 예외 발생 검증
     *
     * 주어진 파일 리스트에 하나라도 유효하지 않은 확장자(.doc, 확장자 없음 등)가 존재하면
     * BusinessExceptionHandler가 발생하고, ErrorCode.INVALID_FILE을 반환해야 한다.
     */
    @Test
    void uploadFiles_shouldThrowBusinessException_whenInvalidFileExtensionExists() {
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
     * [테스트] uploadFiles 메서드 - 파일 업로드 도중 IOException 발생 시 롤백 및 예외 처리 검증
     *
     * 파일 업로드 과정에서 IOException이 발생하는 경우,
     * 업로드된 파일들을 롤백(delete)한 후 BusinessExceptionHandler(IO_ERROR)를 발생시켜야 한다.
     */
    @Test
    void uploadFiles_shouldRollbackAndThrowIOException_whenFileUploadFails() {
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
     * [테스트] uploadFiles 메서드 - S3 업로드 도중 SdkClientException 발생 시 롤백 및 예외 처리 검증
     *
     * S3에 파일 업로드 중 SdkClientException이 발생하는 경우,
     * 업로드된 파일들을 롤백(delete)한 후 BusinessExceptionHandler(AMAZON_S3_API_ERROR)를 발생시켜야 한다.
     */
    @Test
    void uploadFiles_shouldRollbackAndThrowSdkClientException_whenS3UploadFails() {
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

    /**
     * [테스트 유틸] S3 DeleteObjectsResult Mock 객체 생성
     *
     * 주어진 key 목록으로 S3 삭제 응답(DeleteObjectsResult)을 생성합니다.
     * 테스트 시 S3 삭제 성공 응답을 시뮬레이션할 때 사용합니다.
     *
     * @param keys 삭제된 객체의 key 리스트
     * @return DeleteObjectsResult - 삭제된 객체 정보가 포함된 응답 객체
     */
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