package com.resumatchpro.repository;

import com.resumatchpro.model.RecruiterTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecruiterTeamRepository extends JpaRepository<RecruiterTeam, Long> {

    List<RecruiterTeam> findByCompanyOwnerId(Long ownerId);

    Optional<RecruiterTeam> findByCompanyOwnerIdAndMemberUserId(Long ownerId, Long memberId);

    boolean existsByCompanyOwnerIdAndMemberUserId(Long ownerId, Long memberId);
}
