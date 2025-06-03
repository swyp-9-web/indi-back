package com.swyp.artego.domain.user.service;

import com.swyp.artego.domain.user.dto.request.ArtistUpdateRequest;
import com.swyp.artego.domain.user.dto.response.ArtistDetailInfoResponse;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import com.swyp.artego.global.file.event.ImageDeleteEvent;
import com.swyp.artego.global.file.event.UploadRollbackEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPersistenceService {

    private final UserRepository userRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${ncp.storage.filename.default-profile}")
    private String defaultProfileFileName;

    /**
     * 유저 조회 및 유효성 검사
     * <p>
     * 1. 존재하는 userId 인지
     * 2. 해당 User가 작가 권한을 가지고 있는지
     */
    @Transactional(readOnly = true)
    public User loadAndValidateArtist(String oauthId) {
        User artist = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new BusinessExceptionHandler("해당 작가가 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        if (!artist.getRole().isArtist()) {
            throw new BusinessExceptionHandler("아티스트만 프로필을 수정할 수 있습니다.", ErrorCode.FORBIDDEN_ERROR);
        }

        return artist;
    }

    /**
     * 닉네임 검사
     * <p>
     * true 반환 조건: 기존 닉네임과 달라 변경이 필요한 닉네임이고, 중복이 아닌 경우
     * false 반환 조건: 기존 닉네임과 동일하여 변경할 필요가 없거나 null인 경우
     * 예외: 중복된 닉네임일 경우
     */
    @Transactional(readOnly = true)
    public boolean isNicknameChangedAndAvailable(String currentNickname, String newNickname) {
        if (newNickname != null && !newNickname.equals(currentNickname)) {
            if (userRepository.existsByNickname(newNickname)) {
                throw new BusinessExceptionHandler("이미 사용 중인 닉네임입니다.", ErrorCode.DUPLICATE_RESOURCE);
            }
            return true;
        }
        return false;
    }

    @Transactional
    public ArtistDetailInfoResponse updateArtistDetailWithTransaction(User artist, ArtistUpdateRequest artistRequest, String newImgUrl, boolean isNicknameChanged) {

        // 프로필 이미지 처리
        if (newImgUrl != null) {

            // 롤백 이벤트 등록. DB 롤백 시 S3에 새로 업로드했던 프로필 이미지를 삭제한다.
            applicationEventPublisher.publishEvent(new UploadRollbackEvent(newImgUrl));

            // 커밋 이벤트 등록. DB 커밋 시 기존 프로필 이미지를 삭제한다.
            String previousImgUrl = artist.getImgUrl();
            if (!previousImgUrl.contains(defaultProfileFileName)) {
                log.debug("[updateArtistDetailWithTransaction] 기존 프로필 이미지가 기본 프로필 이미지가 아니므로 기존 이미지를 삭제합니다.");
                applicationEventPublisher.publishEvent(new ImageDeleteEvent(previousImgUrl));
            }

            artist.changeImgUrl(newImgUrl);
        }

        // 닉네임
        if (isNicknameChanged) {
            artist.changeNickname(artistRequest.getNickname());
        }

        // 이미지를 제외한 필드는 무조건 업데이트 (값이 null일 수 없다는 전제)
        artist.changeAboutMe(artistRequest.getAboutMe());
        artist.changeHomeLink(artistRequest.getHomeLink());
        artist.changeSnsLinks(artistRequest.getSnsLinks());

        return ArtistDetailInfoResponse.from(artist, false);
    }
}
