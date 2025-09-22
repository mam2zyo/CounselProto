package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.CommentRequest;
import io.notfound.counsel_back.board.dto.CommentResponse;
import io.notfound.counsel_back.board.entity.Comment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.repository.CommentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final FilteringService filteringService;

    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found with id: " + postId));

        String filteredContent = filteringService.filterText(request.getContent());

        Comment comment = Comment.builder()
                .content(filteredContent)
                .build();

        post.addComment(comment);
        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.from(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        List<Comment> comments = commentRepository.findAllByPostId(postId);
        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with id: " + commentId));

        String filteredContent = filteringService.filterText(request.getContent());

        comment.update(filteredContent);

        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NoSuchElementException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }
}