package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.PostRequest;
import io.notfound.counsel_back.board.dto.PostResponse;
import io.notfound.counsel_back.board.dto.PostUpdateRequest;
import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.entity.PostView;
import io.notfound.counsel_back.board.repository.AttachmentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.board.repository.PostViewRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final PostViewRepository postViewRepository; // ✅ 유니크 뷰 저장소

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

        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (MultipartFile file : request.getAttachments()) {
                String fileUrl = s3Service.uploadFile(file); // 파일 S3 업로드
                Attachment attachment = Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileUrl(fileUrl)
                        .post(savedPost)
                        .build();
                attachmentRepository.save(attachment);
            }
        }
        return PostResponse.from(savedPost);
    }

    // ✅ 상세 조회: 조회수 증가 없음 (데이터만 반환)
    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(id)
                .orElseGet(() -> postRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")));

        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPostsSortedBy(String sortBy) {
        List<Post> posts;
        if ("views".equalsIgnoreCase(sortBy)) {
            posts = postRepository.findAllByOrderByViewsDesc();
        } else if ("comments".equalsIgnoreCase(sortBy)) {
            posts = postRepository.findAllByOrderByCommentCountDesc();
        } else { // 기본값 latest (최신순)
            posts = postRepository.findAllByOrderByCreatedAtDesc();
        }

        return posts.stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 권한 확인
        if (post.getAuthor() == null || post.getAuthor().getEmail() == null || !post.getAuthor().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글을 수정할 권한이 없습니다.");
        }

        post.update(request.getTitle(), request.getContent());

        // 1. 삭제 요청된 파일 처리
        if (request.getDeletedAttachmentUrls() != null) {
            List<Attachment> attachmentsToDelete = post.getAttachments().stream()
                    .filter(att -> request.getDeletedAttachmentUrls().contains(att.getFileUrl()))
                    .toList();

            for (Attachment attachment : attachmentsToDelete) {
                try {
                    s3Service.deleteFile(attachment.getFileUrl()); // S3에서 삭제
                } catch (Exception ex) {
                    System.err.println("[WARN] S3 파일 삭제 실패: " + attachment.getFileUrl() + " - " + ex.getMessage());
                }
                post.removeAttachment(attachment); // 연관관계 제거 (orphanRemoval=true로 DB에서 삭제)
            }
        }

        // 2. 새로 추가된 파일 처리
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

        // Post 엔티티는 dirty checking에 의해 자동 업데이트 됨
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 권한 확인 (author null 대비)
        if (post.getAuthor() == null || post.getAuthor().getEmail() == null || !email.equals(post.getAuthor().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글을 삭제할 권한이 없습니다.");
        }

        // ✅ 1) 유니크 뷰 기록 먼저 삭제 (FK 충돌 방지)
        try {
            postViewRepository.deleteByPostId(id);
        } catch (Exception ex) {
            // 만약 메서드가 없다면 PostView 엔티티에 cascade를 추가하는 방법 사용 가능
            System.err.println("[WARN] PostView 삭제 중 문제 발생(postId=" + id + "): " + ex.getMessage());
        }

        // ✅ 2) S3 파일 삭제: 예외는 로깅만 하고 계속 진행 (500 방지)
        if (post.getAttachments() != null) {
            for (Attachment attachment : post.getAttachments()) {
                String url = attachment.getFileUrl();
                if (url == null || url.isBlank()) continue;
                try {
                    s3Service.deleteFile(url);
                } catch (Exception ex) {
                    System.err.println("[WARN] S3 파일 삭제 실패: " + url + " - " + ex.getMessage());
                }
            }
        }

        // ✅ 3) 게시글 삭제 (attachments/comments는 cascade + orphanRemoval 로 정리)
        postRepository.delete(post);
    }

    // ✅ 댓글 수 증감 (댓글 생성/삭제에서 호출)
    @Transactional
    public void increaseCommentCount(Long postId) {
        int updated = postRepository.changeCommentCount(postId, +1);
        if (updated == 0) throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
    }

    @Transactional
    public void decreaseCommentCount(Long postId) {
        int updated = postRepository.changeCommentCount(postId, -1);
        if (updated == 0) throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다.");
    }

    // ✅ 유니크 조회수 기록: "로그인 사용자만" 카운트 (비로그인은 무시)
    @Transactional
    public void recordUniqueView(Long postId, String emailOrNull, HttpServletRequest httpRequest) {
        // 비로그인은 카운트하지 않음 (요구사항)
        if (emailOrNull == null || emailOrNull.isBlank()) {
            return; // 그냥 무시 (컨트롤러는 200 OK 반환)
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        User viewer = userRepository.findByEmail(emailOrNull)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + emailOrNull));

        // 이미 본 사용자면 증가하지 않음
        boolean seen = postViewRepository.existsByPostIdAndViewerUser_Id(postId, viewer.getId());
        if (seen) return;

        // 처음 보는 사용자 → 뷰 기록 저장 후 카운트 +1
        PostView pv = PostView.builder()
                .post(post)
                .viewerUser(viewer)
                .build();
        postViewRepository.save(pv);

        postRepository.incrementViews(postId);
    }
}
