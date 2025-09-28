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

@Service
@RequiredArgsConstructor
public class BoardService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AttachmentRepository attachmentRepository;
    private final S3Service s3Service; // S3Service는 이미 구현되어 있다고 가정합니다.

    @Transactional
    public PostResponse createPost(PostRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + email));

        Post post = Post.builder()
                .title(request.getTitle())
                .author(user)
                .content(request.getContent())
                .build();

        final Post savedPost = postRepository.save(post);

        if (request.getAttachments() != null) {
            for (MultipartFile file : request.getAttachments()) {
                String fileUrl = s3Service.uploadFile(file);
                Attachment attachment = Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileUrl(fileUrl)
                        .post(savedPost)
                        .build();
                attachmentRepository.save(attachment);
                savedPost.getAttachments().add(attachment); // 양방향 매핑 처리
            }
        }
        return PostResponse.from(savedPost);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        return PostResponse.from(post);
    }

    /**
     * 제목 또는 내용으로 게시글을 검색합니다.
     * 검색어가 없거나 비어있으면 모든 게시글을 반환합니다.
     * @param search 검색 키워드
     * @return 검색 결과 (PostResponse 리스트)
     */
    @Transactional(readOnly = true)
    public List<PostResponse> searchPosts(String search) {
        List<Post> posts;

        if (search == null || search.trim().isEmpty()) {
            // 🌟 수정: 새로운 Fetch Join 메서드 사용
            posts = postRepository.findAllWithAuthorAndAttachments();
        } else {
            // 🌟 수정: 새로운 Fetch Join 검색 메서드 사용
            posts = postRepository.searchByTitleOrContentWithAuthorAndAttachments(search.trim());
        }

        // 이 시점에 이미 Attachment와 User 정보가 로드되어 있어 Lazy Loading 오류가 발생하지 않습니다.
        return posts.stream()
                .map(PostResponse::from)
                .toList();
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getAuthor().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글을 수정할 권한이 없습니다.");
        }

        post.update(request.getTitle(), request.getContent());

        // 기존 첨부파일 삭제
        if (request.getDeletedAttachmentUrls() != null) {
            List<Attachment> attachmentsToDelete = post.getAttachments().stream()
                    .filter(att -> request.getDeletedAttachmentUrls().contains(att.getFileUrl()))
                    .toList();
            for (Attachment attachment : attachmentsToDelete) {
                s3Service.deleteFile(attachment.getFileUrl());
                post.removeAttachment(attachment); // Post 엔티티에서 제거 (CascadeType.ALL로 삭제)
            }
        }

        // 새 첨부파일 추가
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
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getAuthor().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글을 삭제할 권한이 없습니다.");
        }

        // S3 파일 삭제
        for (Attachment attachment : post.getAttachments()) {
            s3Service.deleteFile(attachment.getFileUrl());
        }

        postRepository.delete(post);
    }
}