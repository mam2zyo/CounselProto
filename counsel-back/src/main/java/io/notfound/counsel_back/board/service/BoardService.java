package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.PostRequest;
import io.notfound.counsel_back.board.dto.PostResponse;
import io.notfound.counsel_back.board.dto.PostUpdateRequest;
import io.notfound.counsel_back.board.entity.Attachment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FilteringService filteringService;

    /** 게시글 생성 */
    @Transactional
    public PostResponse createPost(PostRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + email));

        String filteredTitle = filteringService.filterText(request.getTitle());
        String filteredContent = filteringService.filterText(request.getContent());

        Post post = Post.builder()
                .title(filteredTitle)
                .content(filteredContent)
                .author(user)
                .build();

        postRepository.save(post);
        return PostResponse.from(post);
    }

    /** 게시글 조회 */
    @Transactional
    public PostResponse getPost(Long id) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        post.incrementViews(); // 조회수 증가
        return PostResponse.from(post);
    }

    /** 게시글 전체 조회 */
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(String keyword, String sort) {
        List<Post> posts;
        if (keyword != null && !keyword.trim().isEmpty()) {
            if ("views".equals(sort)) {
                posts = postRepository.findByTitleContainingOrContentContainingOrderByViewsDesc(keyword, keyword);
            } else {
                posts = postRepository.findByTitleContainingOrContentContaining(keyword, keyword);
            }
        } else {
            if ("views".equals(sort)) {
                posts = postRepository.findAllByOrderByViewsDesc();
            } else {
                posts = postRepository.findAll();
            }
        }
        return posts.stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    /** 게시글 수정 */
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

        return PostResponse.from(post);
    }

    /** 게시글 삭제 */
    @Transactional
    public void deletePost(Long id, String email) {
        Post post = postRepository.findByIdWithAuthorAndAttachments(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        if (!post.getAuthor().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "게시글을 삭제할 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}
