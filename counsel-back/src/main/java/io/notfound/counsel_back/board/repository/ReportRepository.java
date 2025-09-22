package io.notfound.counsel_back.board.repository;

import io.notfound.counsel_back.board.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
