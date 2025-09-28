package io.notfound.counsel_back.board.service;

import io.notfound.counsel_back.board.dto.ReportRequestDto;
import io.notfound.counsel_back.board.entity.*;
import io.notfound.counsel_back.board.repository.CommentRepository;
import io.notfound.counsel_back.board.repository.PostRepository;
import io.notfound.counsel_back.board.repository.ReportRepository;
import io.notfound.counsel_back.user.entity.User;
import io.notfound.counsel_back.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Report saveReport(ReportRequestDto requestDto, String email) {

        User reporter = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + email
                        )
                );

        // 1. 동일한 유저가 동일한 대상을 이미 신고했는지 확인
        reportRepository.findByReporterAndTargetIdAndTargetType(
                reporter, requestDto.getTargetId(), requestDto.getTargetType()
        ).ifPresent(report -> {
            throw new IllegalStateException("이미 신고한 대상입니다.");
        });

        // 2. 신고 대상(게시글/댓글)이 DB에 존재하는지 확인
        validateTargetExists(requestDto.getTargetId(), requestDto.getTargetType());

        // 3. Report 엔티티 생성 및 저장
        Report report = Report.builder()
                .reporter(reporter)
                .targetId(requestDto.getTargetId())
                .targetType(requestDto.getTargetType())
                .reason(requestDto.getReason())
                .status(ReportStatus.PENDING) // 초기 상태는 '대기중'
                .build();

        return reportRepository.save(report);
    }

    private void validateTargetExists(Long targetId, ReportTargetType targetType) {
        if (targetType == ReportTargetType.POST) {
            postRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("신고 대상을 찾을 수 없습니다. (게시글 ID: " + targetId + ")"));
        } else if (targetType == ReportTargetType.COMMENT) {
            commentRepository.findById(targetId)
                    .orElseThrow(() -> new EntityNotFoundException("신고 대상을 찾을 수 없습니다. (댓글 ID: " + targetId + ")"));
        }
    }
}