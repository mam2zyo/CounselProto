package io.notfound.counsel_back.consultation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.notfound.counsel_back.consultation.entity.Consultation;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
}