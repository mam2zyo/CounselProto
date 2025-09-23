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
    private Long views; // ✅ 조회수 포함

    public static PostResponse from(Post post) {
        PostResponse response = new PostResponse();
        response.postId = post.getId();
        response.title = post.getTitle();
        response.content = post.getContent();
        response.attachmentUrls = post.getAttachments().stream()
                .map(Attachment::getFileUrl)
                .collect(Collectors.toList());
        response.views = post.getViews();  // ✅ 조회수 할당 추가
        return response;
    }
}



