package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.PostRequestDto;
import io.notfound.counsel_back.board.dto.PostResponseDto;
import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.repository.AttachmentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;

    @Transactional
    public PostResponseDto createPost(PostRequestDto request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        final Post savedPost = postRepository.save(post);

        if (request.getAttachmentUrls() != null) {
            for (String url : request.getAttachmentUrls()) {
                Attachment attachment = Attachment.builder()
                        .fileName(url)
                        .fileUrl(url)
                        .post(savedPost)
                        .build();

                attachmentRepository.save(attachment);
                savedPost.getAttachments().add(attachment);
            }
        }

        return new PostResponseDto(savedPost);
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return new PostResponseDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponseDto updatePost(Long id, PostRequestDto request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 한 번에 업데이트하는 메서드 호출
        post.update(request.getTitle(), request.getContent());

        // 기존 첨부파일 제거 (orphanRemoval 적용)
        post.getAttachments().clear();

        // 새로운 첨부파일 추가
        if (request.getAttachmentUrls() != null) {
            for (String url : request.getAttachmentUrls()) {
                Attachment attachment = Attachment.builder()
                        .fileName(url)
                        .fileUrl(url)
                        .post(post)
                        .build();
                post.addAttachment(attachment);
            }
        }

        return new PostResponseDto(post);
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        postRepository.delete(post);
    }
}
