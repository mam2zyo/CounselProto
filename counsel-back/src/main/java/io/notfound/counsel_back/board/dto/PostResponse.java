package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class PostResponse {

    private Long postId;         // 게시글 ID
    private Long authorId;       // 게시글 작성자 ID
    private String title;        // 게시글 제목
    private String content;      // 게시글 내용
    private List<String> attachmentUrls;  // 첨부파일 URL 리스트
    private Integer viewCount;   // 조회수
    private Integer commentCount;// 댓글 수
    private String createdAt;    // 생성일시 (yyyy-MM-dd HH:mm 형식)

    private Integer likeCount;   // 좋아요 수
    private Boolean liked;       // 로그인 유저의 좋아요 여부

    // 기본적으로 liked는 false로 설정
    public static PostResponse from(Post post) {
        return from(post, false);
    }

    // 로그인한 사용자의 좋아요 여부도 포함
    public static PostResponse from(Post post, boolean liked) {
        return PostResponse.builder()
                .postId(post.getId())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId() : null)
                .title(post.getTitle())
                .content(post.getContent())
                .attachmentUrls(post.getAttachments() != null
                        ? post.getAttachments().stream().map(Attachment::getFileUrl).toList()
                        : List.of())
                .viewCount(post.getViews())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt() != null
                        ? post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        : null)
                .likeCount(post.getLikeCount()) // 좋아요 수
                .liked(liked)  // 로그인 유저가 좋아요를 눌렀는지 여부
                .build();
    }
}
