package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.ReportResponseDto;
import io.notfound.counsel_back.board.entity.Comment;
import io.notfound.counsel_back.board.entity.Post;
import io.notfound.counsel_back.board.entity.Report;
import io.notfound.counsel_back.board.entity.ReportStatus;
import io.notfound.counsel_back.board.repository.CommentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.board.repository.ReportRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 🔹 게시글 신고
    @Transactional
    public ReportResponseDto reportPost(Long postId, Long userId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Report report = Report.builder()
                .post(post)
                .reporter(user)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();

        post.addReport(report);
        reportRepository.save(report);

        return mapToDto(report);
    }

    // 🔹 댓글 신고
    @Transactional
    public ReportResponseDto reportComment(Long commentId, Long userId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Report report = Report.builder()
                .comment(comment)
                .reporter(user)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();

        comment.addReport(report);
        reportRepository.save(report);

        return mapToDto(report);
    }

    // 🔹 신고 상태 변경 (관리자용)
    @Transactional
    public ReportResponseDto updateReportStatus(Long reportId, ReportStatus status) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));
        report.updateStatus(status);
        return mapToDto(report);
    }

    // 🔹 단일 신고 조회
    @Transactional(readOnly = true)
    public ReportResponseDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고입니다."));
        return mapToDto(report);
    }

    // 🔹 Report → ReportResponseDto 변환
    private ReportResponseDto mapToDto(Report report) {
        return ReportResponseDto.builder()
                .id(report.getId())
                .postId(report.getPost() != null ? report.getPost().getId() : null)
                .reporterId(report.getReporter().getId())
                .reason(report.getReason())
                .status(report.getStatus())
                .build();
    }
}
