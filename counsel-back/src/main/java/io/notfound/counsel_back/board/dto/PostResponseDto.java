package io.notfound.counsel_back.board.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PostResponseDto {
	private Long postId;
    private String title;
    private String content;
    private List<String> attachmentUrls;
}
