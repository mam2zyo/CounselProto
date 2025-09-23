package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.ReportResponseDto;
import io.notfound.counsel_back.board.entity.ReportStatus;
import io.notfound.counsel_back.board.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 🔹 게시글 신고
    @PostMapping("/post/{postId}")
    public ReportResponseDto reportPost(@PathVariable Long postId,
                                        @RequestParam Long userId,
                                        @RequestParam String reason) {
        return reportService.reportPost(postId, userId, reason);
    }

    // 🔹 댓글 신고
    @PostMapping("/comment/{commentId}")
    public ReportResponseDto reportComment(@PathVariable Long commentId,
                                           @RequestParam Long userId,
                                           @RequestParam String reason) {
        return reportService.reportComment(commentId, userId, reason);
    }

    // 🔹 신고 상태 변경 (관리자용)
    @PatchMapping("/{reportId}/status")
    public ReportResponseDto updateStatus(@PathVariable Long reportId,
                                          @RequestParam ReportStatus status) {
        return reportService.updateReportStatus(reportId, status);
    }

    // 🔹 단일 신고 조회
    @GetMapping("/{reportId}")
    public ReportResponseDto getReport(@PathVariable Long reportId) {
        return reportService.getReport(reportId);
    }
}
