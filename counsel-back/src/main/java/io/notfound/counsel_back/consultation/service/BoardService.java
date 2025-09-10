
package io.notfound.counsel_back.consultation.service;

import io.notfound.counsel_back.consultation.dto.PostRequestDto;
import io.notfound.counsel_back.consultation.dto.PostResponseDto;
import io.notfound.counsel_back.consultation.entity.Attachment;
import io.notfound.counsel_back.consultation.entity.Post;
import io.notfound.counsel_back.consultation.repository.AttachmentRepository;
import io.notfound.counsel_back.consultation.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public PostResponseDto createPost(PostRequestDto request) {
        // Post 생성 및 저장
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        final Post savedPost = postRepository.save(post);

        // 첨부파일 저장
        if (request.getAttachmentUrls() != null) {
            for (String url : request.getAttachmentUrls()) {
                Attachment attachment = Attachment.builder()
                        .fileName(url)   // 파일 이름은 예제용
                        .fileUrl(url)
                        .post(savedPost)
                        .build();

                attachmentRepository.save(attachment);
                savedPost.getAttachments().add(attachment); // 양방향 연관관계면 필요
            }
        }

        // 응답 DTO 변환
        PostResponseDto response = new PostResponseDto();
        response.setPostId(savedPost.getId());
        response.setTitle(savedPost.getTitle());
        response.setContent(savedPost.getContent());
        response.setAttachmentUrls(
                savedPost.getAttachments().stream()
                        .map(Attachment::getFileUrl)
                        .collect(Collectors.toList())
        );

        return response;
    }
}