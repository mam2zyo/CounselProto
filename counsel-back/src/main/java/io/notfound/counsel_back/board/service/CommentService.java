package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.CommentRequest;
import io.notfound.counsel_back.board.dto.CommentResponse;
import io.notfound.counsel_back.board.entity.Comment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.repository.CommentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository; // 게시글 존재 여부 확인용

    // 댓글 생성
    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("Post not found with id: " + postId));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .writer(request.getWriter())
                .post(post)
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentResponse.builder()
                .id(savedComment.getId())
                .content(savedComment.getContent())
                .author(savedComment.getAuthor())
                .createdAt(savedComment.getCreatedAt())
                .postId(savedComment.getPost().getId())
                .build();
    }

    // 특정 게시글의 모든 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // 특정 게시글이 존재하는지 먼저 확인
        if (!postRepository.existsById(postId)) {
            throw new NoSuchElementException("Post not found with id: " + postId);
        }

        List<Comment> comments = commentRepository.findAllByPostId(postId);
        return comments.stream()
                .map(comment -> CommentResponse.builder()
                        .id(comment.getId())
                        .content(comment.getContent())
                        .author(comment.getAuthor())
                        .createdAt(comment.getCreatedAt())
                        .postId(comment.getPost().getId())
                        .build())
                .collect(Collectors.toList());
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("Comment not found with id: " + commentId));

        comment.setContent(request.getContent());
        
        Comment updatedComment = commentRepository.save(comment);

        return CommentResponse.builder()
                .id(updatedComment.getId())
                .content(updatedComment.getContent())
                .author(updatedComment.getAuthor())
                .createdAt(updatedComment.getCreatedAt())
                .postId(updatedComment.getPost().getId())
                .build();
    }
    
    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NoSuchElementException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }
}