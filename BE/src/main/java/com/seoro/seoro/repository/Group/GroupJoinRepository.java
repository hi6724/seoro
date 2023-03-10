package com.seoro.seoro.repository.Group;

import java.util.List;

import com.seoro.seoro.domain.entity.Groups.GroupJoin;
import com.seoro.seoro.domain.entity.Groups.Groups;
import com.seoro.seoro.domain.entity.Member.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupJoinRepository extends JpaRepository<GroupJoin, Long> {

	List<GroupJoin> findByGroups(Groups group);

	boolean existsByMemberAndGroups(Member findMember, Groups group);

}
