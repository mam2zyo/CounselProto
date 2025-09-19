package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.PostRequest;
import io.notfound.counsel_back.board.dto.PostResponse;
import io.notfound.counsel_back.board.dto.PostUpdateRequest;
import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.repository.AttachmentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;
    private final S3Service s3Service;
    private final FilteringService filteringService;

    @Transactional
    public PostResponse createPost(PostRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + email));

        String filteredTitle = filteringService.filterText(request.getTitle());
        String filteredContent = filteringService.filterText(request.getContent());

        Post post = Post.builder()
                .title(filteredTitle)
                .author(user)
                .content(filteredContent)
                .build();

        if (request.getAttachments() != null) {
            for (MultipartFile file : request.getAttachments()) {
                String fileUrl = s3Service.uploadFile(file);
                Attachment attachment = Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileUrl(fileUrl)
                        .post(post)
                        .build();
                post.addAttachment(attachment);
            }
        }

        postRepository.save(post);
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(String keyword) {
        List<Post> posts;
        if (keyword != null && !keyword.trim().isEmpty()) {
            posts = postRepository.findByTitleContainingOrContentContaining(keyword, keyword);
        } else {
            posts = postRepository.findAll();
        }
        return posts.stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String email) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getAuthor().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글을 수정할 권한이 없습니다.");
        }

        String filteredTitle = filteringService.filterText(request.getTitle());
        String filteredContent = filteringService.filterText(request.getContent());

        post.update(filteredTitle, filteredContent);

        if (request.getDeletedAttachmentUrls() != null) {
            List<Attachment> attachmentsToDelete = post.getAttachments().stream()
                    .filter(att -> request.getDeletedAttachmentUrls().contains(att.getFileUrl()))
                    .toList();

            for (Attachment attachment : attachmentsToDelete) {
                s3Service.deleteFile(attachment.getFileUrl());
                post.removeAttachment(attachment);
            }
        }

        if (request.getNewAttachments() != null) {
            for (MultipartFile file : request.getNewAttachments()) {
                String fileUrl = s3Service.uploadFile(file);
                Attachment attachment = Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileUrl(fileUrl)
                        .post(post)
                        .build();
                post.addAttachment(attachment);
            }
        }

        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long id, String email) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getAuthor().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글을 삭제할 권한이 없습니다.");
        }

        for (Attachment attachment : post.getAttachments()) {
            s3Service.deleteFile(attachment.getFileUrl());
        }

        postRepository.delete(post);
    }
}