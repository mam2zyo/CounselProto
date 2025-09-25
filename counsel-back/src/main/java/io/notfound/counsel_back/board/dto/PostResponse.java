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
<<<<<<< HEAD
    private Long views; // ✅ 조회수 포함
=======
    private String createdAt;
>>>>>>> 389d3b6c42d0bdbd02ca8b7151847056590be3ab

    public static PostResponse from(Post post) {
        PostResponse response = new PostResponse();
        response.postId = post.getId();
        response.title = post.getTitle();
        response.content = post.getContent();
        response.attachmentUrls = post.getAttachments().stream()
                .map(Attachment::getFileUrl)
                .collect(Collectors.toList());
<<<<<<< HEAD
        response.views = post.getViews();  // ✅ 조회수 할당 추가
=======
        response.createdAt = post.getCreatedAt() != null ? post.getCreatedAt().toString() : null;
>>>>>>> 389d3b6c42d0bdbd02ca8b7151847056590be3ab
        return response;
    }
}



