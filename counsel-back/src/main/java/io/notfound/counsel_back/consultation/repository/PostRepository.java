package io.notfound.counsel_back.consultation.repository;

import io.notfound.counsel_back.consultation.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> { }
