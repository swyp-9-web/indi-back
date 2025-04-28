package com.swyp.artego.global.file.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.file.dto.response.FileResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final AmazonS3 amazonS3;

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/png", "image/jpeg");

    @Value("${ncp.storage.endpoint}")
    private String endPoint;

    @Value("${ncp.storage.bucket.name}")
    private String bucketName;

    /**
     * 여러 파일 업로드
     *
     * @param multipartFiles
     * @param folderName 해당 파일을 업로드할 폴더명
     * @return List<FileResponse>
     */
    public List<FileResponse> uploadFiles(@NotNull List<MultipartFile> multipartFiles, String folderName) {
        validateFilesExtension(multipartFiles);

        List<FileResponse> s3files = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();
        
        for (MultipartFile multipartFile : multipartFiles) {

            String originalFileName = multipartFile.getOriginalFilename();

            String uploadFileName = getUuidFileName(originalFileName);

            String keyName = folderName + "/" + uploadFileName;

            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(multipartFile.getSize());
            objectMetadata.setContentType(multipartFile.getContentType());

            try (InputStream inputStream = multipartFile.getInputStream()) {
                amazonS3.putObject(bucketName, keyName, inputStream, objectMetadata);
                uploadedKeys.add(keyName);
            } catch (IOException | SdkClientException e) {
                log.error("[FileService] 파일 업로드 도중 오류 발생: {}", e.toString());
                log.info("[FileService] #### 업로드된 파일 롤백 시작 ####");
                rollbackUploadedFiles(uploadedKeys, folderName);

                if (e instanceof IOException) {
                    throw new BusinessExceptionHandler("파일 업로드 도중 오류가 발생했습니다.", ErrorCode.IO_ERROR);
                } else {
                    throw new BusinessExceptionHandler("파일 업로드 도중 오류가 발생했습니다.", ErrorCode.AMAZON_S3_API_ERROR);
                }
            }

            String uploadFileUrl = endPoint + "/" + bucketName + "/" + keyName;
            s3files.add(
                    FileResponse.builder()
                            .originalFileName(originalFileName)
                            .uploadFileName(uploadFileName)
                            .uploadFilePath(folderName)
                            .uploadFileUrl(uploadFileUrl)
                            .build());
        }
        return s3files;
    }

    /**
     * 한 파일 삭제
     *
     * @param folderName 파일이 존재하는 폴더명
     * @param fileName
     */
    public void deleteFile(String folderName, String fileName) {
        String keyName = folderName + "/" + fileName;
        try {
            amazonS3.deleteObject(bucketName, keyName);
        } catch (SdkClientException e) {
            log.error("[FileService] 파일 삭제 도중 오류 발생: {}", e.toString());
            throw new BusinessExceptionHandler("파일 삭제 도중 오류가 발생했습니다.", ErrorCode.AMAZON_S3_API_ERROR);
        }
    }

    /**
     * 파일이 올바른 형식인지 확인한다.
     * 현재 허용하는 이미지 형식(Content-Type: image/png, image/jpeg)이 아닌 경우, 상황 별 예외를 던진다.
     *
     * TODO: DEVELOP: 파일 내용(Magic Number) 검사?
     *
     * @param multipartFiles
     */
    private static void validateFilesExtension(List<MultipartFile> multipartFiles) {
        List<MultipartFile> validFiles = multipartFiles.stream()
                .filter(file -> !file.isEmpty())
                .toList();
        if (validFiles.isEmpty()) {
            throw new BusinessExceptionHandler("파일이 없습니다. 파일을 선택하여 제출해주세요.", ErrorCode.INVALID_FILE);
        }

        for (MultipartFile multipartFile : multipartFiles) {
            Optional.ofNullable(multipartFile.getOriginalFilename())
                    .filter(name -> name.contains("."))
                    .orElseThrow(() -> new BusinessExceptionHandler(
                            "올바르지 않은 파일입니다. 파일명 혹은 파일 확장자를 확인해주세요.", ErrorCode.INVALID_FILE
                    ));

//            String ext = originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase();

            String contentType = multipartFile.getContentType();
            if (!IMAGE_CONTENT_TYPES.contains(contentType)) {
                throw new BusinessExceptionHandler(
                        "지원하지 않는 파일 타입입니다. 현재 지원하는 타입: " + String.join(", ", IMAGE_CONTENT_TYPES),
                        ErrorCode.INVALID_FILE
                );
            }
        }
    }

    /**
     * 스토리지에 이미지를 저장할 때 기존 파일명을 UUID로 변환한다.
     * 파일명이 동일한 경우 스토리지에 저장할 수 없는 상황을 방지한다.
     *
     * @param fileName
     * @return String(UUID)
     */
    public String getUuidFileName(String fileName) {
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return UUID.randomUUID().toString() + "." + ext;
    }

    /**
     * 스토리지에 여러 파일을 올리는 도중 에러가 발생하는 경우, 이전까지 저장했던 파일을 삭제(=롤백)한다.
     *
     * @param uploadedKeys 업로드 완료한 파일명이 담긴 리스트
     * @param folderName
     */
    void rollbackUploadedFiles(List<String> uploadedKeys, String folderName) {
        if (uploadedKeys.isEmpty()) {
            log.info("[FileService] 롤백 완료 - 업로드된 파일이 없습니다.");
            return;
        }

        List<DeleteObjectsRequest.KeyVersion> keys = uploadedKeys.stream()
                .map(key -> new DeleteObjectsRequest.KeyVersion(folderName + "/" + key))
                .toList();

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucketName).withKeys(keys);

        try {
            DeleteObjectsResult result = amazonS3.deleteObjects(deleteObjectsRequest);
            List<DeleteObjectsResult.DeletedObject> deletedObjects = result.getDeletedObjects();
            for(DeleteObjectsResult.DeletedObject deletedObject : deletedObjects) {
                log.info("[FileService] 롤백 완료 - 삭제된 파일 key: {}", deletedObject.getKey());
            }
        } catch (MultiObjectDeleteException e){
            List<MultiObjectDeleteException.DeleteError> errors = e.getErrors();
            for(MultiObjectDeleteException.DeleteError error : errors) {
                log.error("[FileService] 롤백 일부 실패 - 삭제 실패 파일 key: {}, 코드: {}, 메시지: {}",
                        error.getKey(), error.getCode(), error.getMessage());
            }
        } catch (SdkClientException e) {
            log.error("[FileService] 롤백 전체 실패 - 네트워크 오류로 전체 삭제 실패", e);
            throw new BusinessExceptionHandler("파일 삭제(롤백) 도중 오류가 발생했습니다.", ErrorCode.AMAZON_S3_API_ERROR);
        }
    }
}
