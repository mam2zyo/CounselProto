package io.notfound.counsel_back.board.dto;

import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class PostResponse {
    private Long postId;
    private String title;
    private String content;
    private List<String> attachmentUrls;
    private String createdAt;

    public static PostResponse from(Post post) {
        PostResponse response = new PostResponse();
        response.postId = post.getId();
        response.title = post.getTitle();
        response.content = post.getContent();
        response.attachmentUrls = post.getAttachments().stream()
                .map(Attachment::getFileUrl)
                .toList();
        response.createdAt = post.getCreatedAt() != null ? post.getCreatedAt().toString() : null;
        return response;
    }
}



