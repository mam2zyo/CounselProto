package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.ReportRequestDto;
import io.notfound.counsel_back.board.entity.*;
import io.notfound.counsel_back.board.repository.ReportRepository;
import io.notfound.counsel_back.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    public Report saveReport(ReportRequestDto requestDto, User reporter) {
        // 중복 신고 확인
        reportRepository.findByReporterAndTargetIdAndTargetType(
                reporter,
                requestDto.getTargetId(),
                requestDto.getTargetType()
        ).ifPresent(r -> {
            throw new IllegalArgumentException("이미 신고한 대상입니다.");
        });

        // 저장
        Report report = Report.builder()
                .reporter(reporter)
                .targetId(requestDto.getTargetId())
                .targetType(requestDto.getTargetType())
                .reason(requestDto.getReason())
                .status(ReportStatus.PENDING)
                .build();

        return reportRepository.save(report);
    }
}
