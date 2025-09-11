package io.notfound.counsel_back.board.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class PostRequestDto {
    private String title;
    private String content;

    // 첨부파일을 MultipartFile로 받아서 S3 업로드
    private List<MultipartFile> attachments;
}
