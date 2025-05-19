package com.swyp.artego.global.dummy.service;

import com.swyp.artego.domain.comment.entity.Comment;
import com.swyp.artego.domain.comment.repository.CommentRepository;
import com.swyp.artego.domain.item.entity.Item;
import com.swyp.artego.domain.item.repository.ItemRepository;
import com.swyp.artego.domain.user.entity.User;
import com.swyp.artego.domain.user.repository.UserRepository;
import com.swyp.artego.global.common.code.ErrorCode;
import com.swyp.artego.global.excpetion.BusinessExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DummyCommentService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void createDummyComments(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessExceptionHandler("작품이 존재하지 않습니다.", ErrorCode.NOT_FOUND_ERROR));

        User itemOwner = item.getUser();

        List<User> users = new java.util.ArrayList<>(userRepository.findAll()
                .stream()
                .filter(user -> !user.getId().equals(itemOwner.getId()))
                .toList());

        Collections.shuffle(users);
        User user1 = users.get(0);
        User user2 = users.get(1);
        User user3 = users.get(2);
        User user4 = users.get(3);


        List<CommentSet> shuffled = getShuffledCommentSets();
        Collections.shuffle(shuffled);

        CommentSet commentSet1 = shuffled.get(0);
        CommentSet deletedCommentSet = shuffled.get(1);
        CommentSet commentSet2 = shuffled.get(2);

        CommentSet secretCommentSet = getRandomSecretCommentSet();


        // 평범 댓글
        Comment comment1 = commentRepository.save(new Comment(user1, item, commentSet1.parentContent(), false, false, null));
        commentRepository.save(new Comment(itemOwner, item, commentSet1.replyContent2(), false, false, comment1));

        // 삭제된 댓글
        Comment comment2 = commentRepository.save(new Comment(user2, item, deletedCommentSet.parentContent(), false, false, null));
        commentRepository.save(new Comment(user2, item, "삭제된 댓글입니다.", false, true, comment2));
        commentRepository.save(new Comment(itemOwner, item, deletedCommentSet.replyContent2(), false, false, comment2));

        // 비밀 댓글
        Comment comment3 = commentRepository.save(new Comment(user3, item, secretCommentSet.parentContent, false, false, null));
        commentRepository.save(new Comment(itemOwner, item, secretCommentSet.replyContent1(), true, false, comment3));
        commentRepository.save(new Comment(user3, item, secretCommentSet.replyContent2(), true, false, comment3));

        // 평범 댓글
        Comment comment4 = commentRepository.save(new Comment(user4, item, commentSet2.parentContent(), false, false, null));
        commentRepository.save(new Comment(user4, item, commentSet2.replyContent1(), false, false, comment4));
        commentRepository.save(new Comment(itemOwner, item, commentSet2.replyContent2(), false, false, comment4));
    }

    private static CommentSet getRandomSecretCommentSet() {
        List<CommentSet> secretCommentSetList = List.of(
                new CommentSet(
                        "가격 조금만 더 조정 가능할까요? 27 생각하고 있어요.",
                        "정말 관심 있으시다면 26까지는 가능해요.",
                        "좋아요, 그럼 그렇게 진행할게요."
                ),
                new CommentSet(
                        "직거래 가능한가요? 위치가 어디신가요?",
                        "서울 강남 근처입니다. 직거래 가능해요.",
                        "그럼 이번 주 토요일에 뵐 수 있을까요?"
                ),
                new CommentSet(
                        "작가님 다른 작품도 보고 싶은데 공유 가능할까요?",
                        "포트폴리오 링크 보내드릴게요.",
                        "확인했습니다. 감사합니다!"
                ),
                new CommentSet(
                        "선물용으로 구매하고 싶은데 포장이 될까요?",
                        "간단한 포장은 가능합니다.",
                        "그럼 포장 부탁드릴게요. 감사합니다."
                )
        );
        return secretCommentSetList.get(new Random().nextInt(secretCommentSetList.size()));
    }

    private static List<CommentSet> getShuffledCommentSets() {
        List<CommentSet> commentSetList = List.of(
                new CommentSet("대단합니다.", "상당히 섬세한 작업이네요.", "알아봐주셔서 감사합니다."),
                new CommentSet("색감이 예술이에요", "디테일이 살아있네요.", "컬러 조합에 신경 썼어요!"),
                new CommentSet("이런 느낌 너무 좋아요.", "저도 비슷한 시도 해봤어요.", "공감해주셔서 기쁘네요."),
                new CommentSet("아이디어가 기발해요.", "이거 구매 가능한가요?", "제 SNS 링크로 연락 부탁드릴게요."),
                new CommentSet("강렬한 인상이네요.", "색 대비가 눈에 띄어요.", "감정적인 부분을 강조하고 싶었어요."),
                new CommentSet("분위기가 너무 좋아요.", "저랑 취향이 비슷하신 것 같아요.", "취향이 통한다니 반가워요!"),
                new CommentSet("이 작품 어디에서 볼 수 있나요?", "오프라인 전시도 하나요?", "다음 달 개인전에서 전시할 예정이에요."),
                new CommentSet("친구한테 보여줘야겠어요.", "정말 멋지네요.", "공유해주시면 감사하죠! 소문 많이 내주세요 :)")
        );
        return new ArrayList<>(commentSetList);
    }

    public record CommentSet(String parentContent, String replyContent1, String replyContent2) {
    }
}
