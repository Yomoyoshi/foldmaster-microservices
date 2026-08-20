package com.foldmaster.contactservice.repository;

import com.foldmaster.contactservice.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);
    List<ContactMessage> findByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(Long userId, LocalDateTime since);
}
