package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.PostRequest;
import io.notfound.counsel_back.board.dto.PostResponse;
import io.notfound.counsel_back.board.dto.PostUpdateRequest;
import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.entity.PostView;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.board.repository.PostViewRepository;
import io.notfound.counsel_back.common.exception.PostNotFoundException;
import io.notfound.counsel_back.common.exception.UnauthorizedActionException;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final S3Service s3Service;
    private final PostViewRepository postViewRepository;

    /** 게시글 생성 */
    @Transactional
    public PostResponse createPost(PostRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new PostNotFoundException("사용자를 찾을 수 없습니다: " + email));

        Post post = Post.builder()
                .title(request.getTitle())
                .author(user)
                .content(request.getContent())
                .build();

        final Post savedPost = postRepository.save(post);

        // 첨부파일 업로드 및 연관 추가
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            for (MultipartFile file : request.getAttachments()) {
                String fileUrl = s3Service.uploadFile(file);
                Attachment attachment = Attachment.builder()
                        .fileName(file.getOriginalFilename())
                        .fileUrl(fileUrl)
                        .post(savedPost)
                        .build();
                // 연관 편의 메서드 사용 (cascade = PERSIST 가정)
                savedPost.addAttachment(attachment);
            }
        }
        return PostResponse.from(savedPost);
    }

    /** 상세 조회 (조회수 증가 없음) */
    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(id)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));
        return PostResponse.from(post);
    }

    /**
     * 목록 조회: 검색 + 페이지네이션 (정렬은 컨트롤러에서 Pageable 주입)
     * - search 가 null/blank 이면 전체 조회
     * - 아니면 제목/내용에서 대소문자 구분 없이 부분일치 검색
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(String search, Pageable pageable) {
        Page<Post> posts;
        if (search == null || search.trim().isEmpty()) {
            posts = postRepository.findAllWithAuthorAndAttachments(pageable);
        } else {
            posts = postRepository.findByKeywordContainingIgnoreCase(search.trim(), pageable);
        }
        return posts.map(PostResponse::from);
    }

    /** 댓글순 정렬 전용: commentCount 기준 + 정렬 방향(direction) 반영 */
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPostsOrderByCommentCount(String search, Pageable pageable, String direction) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(dir, "commentCount")
        );

        Page<Post> posts;
        if (search == null || search.trim().isEmpty()) {
            posts = postRepository.findAllWithAuthorAndAttachments(sorted);
        } else {
            String q = search.trim();
            posts = postRepository.findByKeywordContainingIgnoreCase(search.trim(), sorted);
        }
        return posts.map(PostResponse::from);
    }

    /** 게시글 수정 */
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request, String email) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        // 권한 확인
        if (post.getAuthor() == null || post.getAuthor().getEmail() == null
                || !post.getAuthor().getEmail().equals(email)) {
            throw new UnauthorizedActionException("게시글을 수정할 권한이 없습니다.");
        }

        // 본문/제목 수정
        post.update(request.getTitle(), request.getContent());

        // 삭제 요청된 파일 처리
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
                post.removeAttachment(attachment); // 연관관계 제거 (orphanRemoval=true 가정)
            }
        }

        // 새로 추가된 파일 처리
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
        // dirty checking 으로 업데이트 반영
        return PostResponse.from(post);
    }

    /** 게시글 삭제 */
    @Transactional
    public void deletePost(Long id, String email) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        // 권한 확인
        if (post.getAuthor() == null || post.getAuthor().getEmail() == null
                || !email.equals(post.getAuthor().getEmail())) {
            throw new UnauthorizedActionException("게시글을 삭제할 권한이 없습니다.");
        }

        // 유니크 뷰 기록 먼저 삭제 (FK 충돌 방지)
        try {
            postViewRepository.deleteByPostId(id);
        } catch (Exception ex) {
            System.err.println("[WARN] PostView 삭제 중 문제 발생(postId=" + id + "): " + ex.getMessage());
        }

        // S3 파일 삭제 (실패해도 계속 진행)
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
        // 게시글 삭제 (attachments/comments 는 cascade + orphanRemoval 가정)
        postRepository.delete(post);
    }

    // ====== 댓글 수 증감 (댓글 생성/삭제에서 호출) ======

    @Transactional
    public void increaseCommentCount(Long postId) {
        int updated = postRepository.changeCommentCount(postId, +1);
        if (updated == 0) throw new PostNotFoundException("해당 게시글이 존재하지 않습니다.");
    }

    @Transactional
    public void decreaseCommentCount(Long postId) {
        int updated = postRepository.changeCommentCount(postId, -1);
        if (updated == 0) throw new PostNotFoundException("해당 게시글이 존재하지 않습니다.");
    }

    // ====== 유니크 조회수 기록 ======
    // 로그인 사용자만 카운트 (비로그인은 무시)
    @Transactional
    public void recordUniqueView(Long postId, String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일 값이 비어있습니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("해당 게시글이 존재하지 않습니다."));

        User viewer = userRepository.findByEmail(email)
                .orElseThrow(() -> new PostNotFoundException("사용자를 찾을 수 없습니다: " + email));

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
