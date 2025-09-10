package io.notfound.counsel_back.consultation.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PostRequestDto {
    private String title;
    private String content;
    private List<String> attachmentUrls;
}
