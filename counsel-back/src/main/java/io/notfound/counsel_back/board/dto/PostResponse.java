package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
public class PostResponse {

    private Long postId;
    private Long authorId;
    private String title;
    private String content;             // 상세 조회 시 포함
    private List<String> attachmentUrls;
    private Integer viewCount;          // 프론트 호환
    private Integer commentCount;       // 프론트 호환
    private String createdAt;           // 문자열로 내려줌

    public static PostResponse from(Post post) {

        return PostResponse.builder()
                .postId(post.getId())
                .authorId(post.getAuthor().getId())
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
                .build();
    }
}
