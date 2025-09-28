package io.notfound.counsel_back.board.controller;

import io.notfound.counsel_back.board.dto.ReportRequestDto;
import io.notfound.counsel_back.board.entity.Report;
import io.notfound.counsel_back.board.service.ReportService;
import io.notfound.counsel_back.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Report> createReport(
            @RequestBody ReportRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        Report saved = reportService.saveReport(requestDto, email);
        return ResponseEntity.ok(saved);
    }
}
