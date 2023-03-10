package com.seoro.seoro.service.Group;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.seoro.seoro.domain.dto.Group.*;
import com.seoro.seoro.domain.dto.Member.MemberDto;
import com.seoro.seoro.domain.entity.Groups.GroupApply;
import com.seoro.seoro.domain.dto.Member.RecommendMemberDto;
import com.seoro.seoro.domain.entity.Groups.GroupBook;
import com.seoro.seoro.domain.entity.Groups.GroupJoin;
import com.seoro.seoro.domain.entity.Groups.GroupSchedule;
import com.seoro.seoro.repository.ChatRoom.ChatRoomRepository;
import com.seoro.seoro.repository.Group.GroupApplyRepository;
import com.seoro.seoro.repository.Group.GroupBookRepository;
import com.seoro.seoro.repository.Group.GroupJoinRepository;
import com.seoro.seoro.repository.Group.GroupScheduleRepository;
import com.seoro.seoro.service.GroupPost.GroupPostService;

import org.springframework.stereotype.Service;

import com.seoro.seoro.domain.dto.ResultResponseDto;
import com.seoro.seoro.domain.entity.Groups.Groups;
import com.seoro.seoro.domain.entity.Member.Member;
import com.seoro.seoro.repository.Group.GroupRepository;
import com.seoro.seoro.repository.Member.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService{
	private final GroupApplyRepository groupApplyRepository;
	private final GroupRepository groupRepository;
	private final GroupJoinRepository groupJoinRepository;
	private final ChatRoomRepository chatRepository;
	private final MemberRepository memberRepository;
	private final GroupPostService groupPostService;
	private final GroupScheduleRepository groupScheduleRepository;
	private final GroupBookRepository groupBookRepository;


	@Override
	public GroupMainResponseDto groupMain(String userName) {
		GroupMainResponseDto groupMainResponseDto = new GroupMainResponseDto();
		System.out.println("userName = " + userName);
		//?????? ???????????? ?????????
		Member findMember = memberRepository.findByMemberName(userName).get();
		System.out.println("findUser = " + findMember);

		//??? ???????????? ????????? ?????????
		String myDongCode = findMember.getMemberDongCode();
		Long myGenre = findMember.getMemberGenre();

		//?????? ???????????? ?????? ???????????? ??????
		List<GroupJoin> findGroupJoin = findMember.getGroupJoins();
		System.out.println("findGroupJoin = " + findGroupJoin.size());
		// for (int i=0; i<findGroupJoin.size(); i++) {
		// 	System.out.println(findGroupJoin.get(i));
		// }
		List<GroupShowDto> myGroups = new ArrayList<>();
		for (GroupJoin groupJoin : findGroupJoin) {
			Groups groups = groupJoin.getGroups();
			myGroups.add(GroupShowDto.builder()
				.groupProfile(groups.getGroupProfile())
				.groupDescrib(groups.getGroupIntroduction())
				.groupName(groups.getGroupName())
				.groupId(groups.getGroupId())
				.build());
		}
		if(myGroups.size() > 0) {
			groupMainResponseDto.setMyGroups(myGroups);
		}

		//?????? ???????????? ?????? ???????????? ??????????????? ??????
		List<Groups> dongGroups = groupRepository.findAll();
		if(dongGroups.size() > 0) {
			Collections.sort(dongGroups, new Comparator<Groups>(){
				@Override
				public int compare(Groups o1, Groups o2) {
					Long o1_genre = o1.getGroupGenre();
					Long o2_genre = o2.getGroupGenre();
					if(Long.bitCount(o1_genre&myGenre) == Long.bitCount(o2_genre&myGenre)){
						return (int)(Long.bitCount(o1_genre)-Long.bitCount(o2_genre));
					}
					return (int)(Long.bitCount(o2_genre&myGenre)-Long.bitCount(o1_genre&myGenre));
				}
			});
			List<GroupShowDto> recommendGroups = new ArrayList<>();
			for(Groups group : dongGroups){
				if(groupJoinRepository.existsByMemberAndGroups(findMember,group)) continue;
				if((group.getGroupGenre()&myGenre)==0){
					break;
				}
				recommendGroups.add(GroupShowDto.builder()
						.groupName(group.getGroupName())
						.groupDescrib(group.getGroupIntroduction())
						.groupProfile(group.getGroupProfile())
						.groupId(group.getGroupId())
						.build());
			}
			groupMainResponseDto.setRecommendGroups(recommendGroups);
		}else{
			groupMainResponseDto.setRecommendGroups(new ArrayList<>());
		}


		//?????? ???????????? ?????? ???????????? ??????????????? ??????
		List<Member> dongMember = memberRepository.findAll();
		if(dongMember.size()>0){
			Collections.sort(dongMember, new Comparator<Member>(){
				@Override
				public int compare(Member o1, Member o2) {
					Long o1_genre = o1.getMemberGenre();
					Long o2_genre = o2.getMemberGenre();
					if(Long.bitCount(o1_genre&myGenre) == Long.bitCount(o2_genre&myGenre)){
						return (int)(Long.bitCount(o1_genre)-Long.bitCount(o2_genre));
					}
					return (int)(Long.bitCount(o2_genre&myGenre)-Long.bitCount(o1_genre&myGenre));
				}
			});
			List<RecommendMemberDto> recommendMembers = new ArrayList<>();
			for(Member member : dongMember){
				if(member.getMemberName().equals(userName)) continue;
				if((member.getMemberGenre()&myGenre)==0){
					break;
				}
				recommendMembers.add(RecommendMemberDto.builder()
					.memberProfile(member.getMemberProfile())
					.memberName(member.getMemberName())
					.memberId(member.getMemberId())
					.build());
			}
			groupMainResponseDto.setRecommendMembers(recommendMembers);
		}

		groupMainResponseDto.setResult(true);
		return groupMainResponseDto;
	}

	@Override
	public ResultResponseDto makeGroup(GroupSignupRequestDto requestDto) {
		ResultResponseDto resultResponseDto = new ResultResponseDto();
		Groups saveGroup = new Groups();
		GroupJoin saveGroupJoin = new GroupJoin();
		boolean online = false;
		if(requestDto.getGroupDongCode() == null) {
			online = true;
		}

		Member host = new Member();
		Optional<Member> tmpUser = memberRepository.findById(requestDto.getGroupHostId());
		if(tmpUser.isPresent()) {
			host = tmpUser.get();
		}else {
			host = tmpUser.orElse(null);
			resultResponseDto.setResult(false);
			return resultResponseDto;
		}

		//?????? ?????? ??????
		Long genre = 0L;
		if(requestDto.getGroupGenres().length > 0) {
			for(int i=0; i<requestDto.getGroupGenres().length; i++) {
				genre = genre | (1 << requestDto.getGroupGenres()[i]);
			}
			log.info("?????? = {}", genre);
		}

		saveGroup = Groups.builder()
			.groupName(requestDto.getGroupName())
			.host(host)
			.groupPassword(requestDto.getGroupPassword())
			.groupCapacity(requestDto.getGroupCapacity())
			.groupDongCode(requestDto.getGroupDongCode())
			.groupProfile(requestDto.getGroupProfile())
			.groupGenre(genre)
			.groupIntroduction(requestDto.getGroupIntroduction())
			.groupStartDate(requestDto.getGroupStartDate())
			.groupEndDate(requestDto.getGroupEndDate())
			.isOnline(online)
			.build();

		saveGroupJoin = GroupJoin.builder()
						.groups(saveGroup)
						.member(host)
						.build();

		groupRepository.save(saveGroup);
		groupJoinRepository.save(saveGroupJoin);
		resultResponseDto.setResult(true);
		return resultResponseDto;
	}

	@Override
	public GroupDetailResponseDto groupDetail(Long groupId) {
		GroupDetailResponseDto groupDetailResponseDto = new GroupDetailResponseDto();
		Groups group = new Groups();
		Optional<Groups> findGroup = groupRepository.findById(groupId);
		if(findGroup.isPresent()) {
			group = findGroup.get();
		} else {
			groupDetailResponseDto.setResult(false);
			return groupDetailResponseDto;
		}

		Integer[] genres = new Integer[Long.bitCount(group.getGroupGenre())];
		String p = Long.toBinaryString(group.getGroupGenre());
		for(int i=p.length()-1, j=0; i>=0; i--) {
			if(p.charAt(i)=='1') {
				genres[j++] = p.length() -i -1;
			}
		}
		Member host = group.getHost();
		GroupMemberDto hostDto = GroupMemberDto.builder()
			.userProfile(host.getMemberProfile())
			.userId(host.getMemberId())
			.userName(host.getMemberName())
			.build();


		groupDetailResponseDto = GroupDetailResponseDto.builder()
				.result(true)
				.groupName(group.getGroupName())
			    .groupProfile(group.getGroupProfile())
			    .groupPassword(group.getGroupPassword())
				.groupStartDate(group.getGroupStartDate())
				.groupEndDate(group.getGroupEndDate())
				.groupDongCode(group.getGroupDongCode())
				.groupCapacity(group.getGroupCapacity())
				.groupDescrib(group.getGroupIntroduction())
			    .groupGenre(genres)
				.bookCount(group.getBooks().size())
				.postCount(group.getPosts().size())
				.meetingCount(group.getMeetings().size())
				.host(hostDto)
				.build();

		return groupDetailResponseDto;
	}

	@Override
	public ResultResponseDto deleteGroup(Long groupId, Long userId) {
		ResultResponseDto responseDto = new ResultResponseDto();
		Member member = new Member();
		Optional<Member> tmpMember = memberRepository.findById(userId);
		if(tmpMember.isPresent()) {
			member = tmpMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		Optional<Groups> tmpGroup = groupRepository.findById(groupId);
		Groups group = new Groups();
		if(tmpGroup.isPresent()) {
			group = tmpGroup.get();
		}
		else {
			responseDto.setResult(false);
			return responseDto;
		}

		if(group.getHost().equals(member)) { //?????? ???????????? ??????????????? ?????? ???????????????
			groupRepository.deleteById(groupId);
		}

		responseDto.setResult(true);
		return responseDto;
	}

	@Override
	public ResultResponseDto applyGroup(Long groupId, Long userId) {
		ResultResponseDto responseDto = new ResultResponseDto();
		Member member = new Member();
		Optional<Member> tmpMember = memberRepository.findById(userId);
		if (tmpMember.isPresent()) {
			member = tmpMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		Optional<Groups> tmpGroup = groupRepository.findById(groupId);
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		GroupApply groupApply = GroupApply.builder()
			.groups(group)
			.member(member)
			.isDelete(false)
			.build();
		groupApplyRepository.save(groupApply);
		responseDto.setResult(true);
		return responseDto;
	}

	@Override
	public GroupApplyReadResponseDto readGroupApplies(Long groupId, Long userId) {
		GroupApplyReadResponseDto responseDto = new GroupApplyReadResponseDto();
		Member member = new Member();
		Optional<Member> tmpMember = memberRepository.findById(userId);
		if (tmpMember.isPresent()) {
			member = tmpMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		Optional<Groups> tmpGroup = groupRepository.findById(groupId);
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		if(group.getHost().equals(member)) { //?????? ???????????? ??????????????? ?????? ???????????????
			List<GroupApply> groupApplyList = groupApplyRepository.findByGroups(group);
			List<GroupApplyUserDto> applyUsers = new ArrayList<>();
			for(GroupApply apply : groupApplyList) {
				if(!apply.getIsDelete()) { //??????, ?????? ?????? ???????????? ??????
					GroupApplyUserDto userDto = GroupApplyUserDto.builder()
						.groupApplyId(apply.getGroupAplyId())
						.userId(apply.getMember().getMemberId())
						.userName(apply.getMember().getMemberName())
						.build();
					applyUsers.add(userDto);	
				}
			}
			responseDto.setResult(true);
			responseDto.setGroupApplies(applyUsers);
			return responseDto;
		}
		else {
			responseDto.setResult(false);
			return responseDto;
		}
	}

	@Override
	public ResultResponseDto approveGroupApply(GroupApproveRequestDto requestDto) {
		ResultResponseDto responseDto = new ResultResponseDto();
		
		//Host ?????? ????????????
		Member member = new Member();
		Optional<Member> tmpMember = memberRepository.findById(requestDto.getUserId());
		if (tmpMember.isPresent()) {
			member = tmpMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}
		//????????? ?????? ????????????
		Member applyMember = new Member();
		Optional<Member> tmpApplyMember = memberRepository
			.findById(requestDto.getApplyUserId());
		if (tmpApplyMember.isPresent()) {
			applyMember = tmpApplyMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}
		//?????? ?????? ?????? ????????????
		Optional<Groups> tmpGroup = groupRepository.findById(requestDto.getGroupId());
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}
		//?????? ?????? ?????? ?????? ?????? ????????????
		Optional<GroupApply> tmpGroupApply = groupApplyRepository.findById(requestDto.getGroupApplyId());
		GroupApply groupApply = new GroupApply();
		if (tmpGroupApply.isPresent()) {
			groupApply = tmpGroupApply.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		if(group.getHost().equals(member)) { //????????? ?????????
			if(requestDto.getIsApprove()) { //??????
				GroupJoin join = GroupJoin.builder()
					.groups(group)
					.member(applyMember)
					.build();
				groupJoinRepository.save(join);
			}
			GroupApply done = GroupApply.builder()
				.groupAplyId(groupApply.getGroupAplyId())
				.groups(group)
				.isDelete(true)
				.member(applyMember)
				.build();
			groupApplyRepository.save(done); //?????? ??????
		}
		responseDto.setResult(true);
		return responseDto;
	}

	@Override
	public GroupMemberReadResponseDto readGroupMembers(Long groupId) {
		GroupMemberReadResponseDto responseDto = new GroupMemberReadResponseDto();
		//?????? ?????? ?????? ????????????
		Optional<Groups> tmpGroup = groupRepository.findById(groupId);
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		List<GroupJoin> findGroupJoins = groupJoinRepository.findByGroups(group);
		List<GroupMemberDto> members = new ArrayList<>();
		//?????? ??? ????????? ??????
		GroupMemberDto hostDto = GroupMemberDto.builder()
			.userId(group.getHost().getMemberId())
			.userProfile(group.getHost().getMemberProfile())
			.userName(group.getHost().getMemberName())
			.build();
		members.add(hostDto);
		for(GroupJoin join : findGroupJoins) {
			if(!join.getMember().equals(group.getHost())) {
				GroupMemberDto memberDto = GroupMemberDto.builder()
					.userId(join.getMember().getMemberId())
					.userProfile(join.getMember().getMemberProfile())
					.userName(join.getMember().getMemberName())
					.build();
				members.add(memberDto);
			}
		}
		responseDto.setResult(true);
		responseDto.setGroupMembers(members);
		return responseDto;
	}

	@Override
	public ResultResponseDto enterGroup(Long groupId, GroupEnterRequestDto requestDto) {
		ResultResponseDto responseDto = new ResultResponseDto();
		//????????? ?????? ????????????
		Member applyMember = new Member();
		Optional<Member> tmpApplyMember = memberRepository
				.findById(requestDto.getUserId());
		if (tmpApplyMember.isPresent()) {
			applyMember = tmpApplyMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}
		//?????? ?????? ?????? ????????????
		Optional<Groups> tmpGroup = groupRepository.findById(requestDto.getGroupId());
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		if(group.getGroupPassword().equals(null)) { //???????????? ?????????
			GroupJoin join = GroupJoin.builder()
				.groups(group)
				.member(applyMember)
				.build();
			groupJoinRepository.save(join);
			responseDto.setResult(true);
		} else {
			if(group.getGroupPassword().equals(requestDto.getWritePassword())) {
				//???????????? ??????
				GroupJoin join = GroupJoin.builder()
					.groups(group)
					.member(applyMember)
					.build();
				groupJoinRepository.save(join);
				responseDto.setResult(true);
			}
			else { //???????????? ??????
				responseDto.setResult(false);
			}
		}

		return responseDto;
	}

	@Override
	public ResultResponseDto createGroupSchedule(GroupScheduleCreateRequestDto requestDto) {
		ResultResponseDto responseDto = new ResultResponseDto();
		System.out.println(requestDto.toString());
		//Host ?????? ????????????
		Member member = new Member();
		Optional<Member> tmpMember = memberRepository.findById(requestDto.getWriterId());
		if (tmpMember.isPresent()) {
			member = tmpMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}
		//?????? ?????? ?????? ????????????
		Optional<Groups> tmpGroup = groupRepository.findById(requestDto.getGroupId());
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		//?????? ?????????
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
				.appendPattern("[yyyy-MM-dd]")
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
				.toFormatter();
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		//????????? -> Date
		LocalDateTime date = LocalDateTime.parse(requestDto.getDate(), formatter);
//		LocalDateTime date = LocalDateTime.parse(requestDto.getDate(),)

		// if(member.equals(group.getHost())) { //????????? ?????? ??????
		// 	GroupSchedule schedule = GroupSchedule.builder()
		// 			.groups(group)
		// 			.date(date)
		// 			.scheduleTitle(requestDto.getGroupScheduleTitle())
		// 			.scheduleContent(requestDto.getGroupScheduleContent())
		// 		.build();
		// 	groupScheduleRepository.save(schedule);
		// }
		// else {
		// 	responseDto.setResult(false);
		// 	return responseDto;
		// }
		GroupSchedule schedule = GroupSchedule.builder()
				.groups(group)
				.date(date)
				.scheduleTitle(requestDto.getGroupScheduleTitle())
				.scheduleContent(requestDto.getGroupScheduleContent())
			.build();
		groupScheduleRepository.save(schedule);
		responseDto.setResult(true);
		return responseDto;
	}

	@Override
	public GroupScheduleUpdateResponseDto updateGroupSchedule(GroupScheduleUpdateRequestDto requestDto) {
		GroupScheduleUpdateResponseDto responseDto = new GroupScheduleUpdateResponseDto();
		//Host ?????? ????????????
		Member member = new Member();
		Optional<Member> tmpMember = memberRepository.findById(requestDto.getWriterId());
		if (tmpMember.isPresent()) {
			member = tmpMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		//?????? ?????? ?????? ????????????
		Optional<Groups> tmpGroup = groupRepository.findById(requestDto.getGroupId());
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		//?????? ?????? ?????? ?????? ????????????
		Optional<GroupSchedule> tmpGroupSchedule = groupScheduleRepository.findById(requestDto.getGroupScheduleId());
		GroupSchedule schedule = new GroupSchedule();
		if (tmpGroupSchedule.isPresent()) {
			schedule = tmpGroupSchedule.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}
		GroupSchedule s = new GroupSchedule();
		if(member.equals(group.getHost())) { //????????? ?????? ??????
			s = GroupSchedule.builder()
					.groupScheduleId(schedule.getGroupScheduleId())
					.groups(group)
					.scheduleTitle(requestDto.getGroupScheduleTitle())
					.scheduleContent(requestDto.getGroupScheduleContent())
					.date(schedule.getDate())
					.build();
			groupScheduleRepository.save(s);
		}
		else {
			responseDto.setResult(false);
			return responseDto;
		}

		responseDto = GroupScheduleUpdateResponseDto.builder()
				.result(true)
				.groupId(group.getGroupId())
				.writerId(member.getMemberId())
				.groupScheduleTitle(s.getScheduleTitle())
				.groupScheduleContent(s.getScheduleContent())
				.build();
		return responseDto;
	}

	@Override
	public GroupScheduleDetailResponseDto readGroupSchedule(Long scheduleId) {
		GroupScheduleDetailResponseDto responseDto = new GroupScheduleDetailResponseDto();
		//?????? ?????? ?????? ?????? ????????????
		Optional<GroupSchedule> tmpGroupSchedule = groupScheduleRepository.findById(scheduleId);
		GroupSchedule schedule = new GroupSchedule();
		if (tmpGroupSchedule.isPresent()) {
			schedule = tmpGroupSchedule.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		responseDto = GroupScheduleDetailResponseDto.builder()
				.result(true)
				.groupId(schedule.getGroups().getGroupId())
				.groupScheduleId(schedule.getGroupScheduleId())
				.groupScheduleTitle(schedule.getScheduleTitle())
				.groupScheduleTime(schedule.getDate())
				.groupScheduleContent(schedule.getScheduleContent())
				.build();
		return responseDto;
	}

	@Override
	public GroupScheduleListResponseDto readGroupScheduleList(Long groupId) {
		GroupScheduleListResponseDto responseDto = new GroupScheduleListResponseDto();
		//?????? ?????? ?????? ????????????
		Optional<Groups> tmpGroup = groupRepository.findById(groupId);
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		List<GroupScheduleDto> schedules = new ArrayList<>();
		List<GroupSchedule> findGroupSchedules = groupScheduleRepository.findByGroupsOrderByDateDesc(group);
		for(GroupSchedule s : findGroupSchedules) {
			GroupScheduleDto sDto = GroupScheduleDto.builder()
					.groupScheduleId(s.getGroupScheduleId())
					.groupScheduleTitle(s.getScheduleTitle())
					.groupScheduleTime(s.getDate())
					.groupScheduleContent(s.getScheduleContent())
					.build();
			schedules.add(sDto);
		}
		responseDto.setResult(true);
		responseDto.setSchedules(schedules);
		return responseDto;
	}

	@Override
	public ResultResponseDto delGroupSchedule(Long scheduleId, Long userId) {
		ResultResponseDto responseDto = new ResultResponseDto();
		//?????? ?????? ?????? ?????? ????????????
		Optional<GroupSchedule> tmpGroupSchedule = groupScheduleRepository.findById(scheduleId);
		GroupSchedule schedule = new GroupSchedule();
		if (tmpGroupSchedule.isPresent()) {
			schedule = tmpGroupSchedule.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		//?????? ?????? ?????? ????????????
		Optional<Groups> tmpGroup = groupRepository.findById(schedule.getGroups().getGroupId());
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		//Host ?????? ????????????
		Member member = new Member();
		Optional<Member> tmpMember = memberRepository.findById(userId);
		if (tmpMember.isPresent()) {
			member = tmpMember.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		if(member.equals(group.getHost())) { //????????? ?????? ??????
			groupScheduleRepository.deleteById(schedule.getGroupScheduleId());
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		responseDto.setResult(true);
		return responseDto;
	}

	@Override
	public GroupBookReadResponseDto readGroupBook(Long groupId) {
		GroupBookReadResponseDto responseDto = new GroupBookReadResponseDto();
		//?????? ?????? ?????? ????????????
		Optional<Groups> tmpGroup = groupRepository.findById(groupId);
		Groups group = new Groups();
		if (tmpGroup.isPresent()) {
			group = tmpGroup.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		//????????? ????????????
		List<GroupJoin> joins = group.getJoins();
		// for(GroupJoin j : )

		return responseDto;
	}

	@Override
	public ResultResponseDto deleteGroupBook(Long groupBookId) {
		ResultResponseDto responseDto = new ResultResponseDto();
		Optional<GroupBook> findBook = groupBookRepository.findById(groupBookId);
		GroupBook book = new GroupBook();
		if(findBook.isPresent()) {
			book = findBook.get();
		} else {
			responseDto.setResult(false);
			return responseDto;
		}

		groupBookRepository.deleteById(groupBookId);
		responseDto.setResult(true);
		return responseDto;
	}

	// @Override
	// public ResultResponseDto delGroupBook(GroupBookCreateRequestDto groupBookDto) {
	// 	ResultResponseDto responseDto = new ResultResponseDto();
	//
	// 	Optional<GroupBook> groupBook = groupBookRepository.findByGroups(groupBookDto.getGroups());
	// 	if(groupBook.isPresent()) {
	// 		groupBookRepository.delete(groupBook.get());
	// 		responseDto.setResult(true);
	// 	}
	//
	// 	return responseDto;
	// }
}
