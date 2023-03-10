package com.seoro.seoro.service.Library;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.seoro.seoro.domain.dto.Book.BookReportDto;
import com.seoro.seoro.domain.dto.Book.OwnBookDto;
import com.seoro.seoro.domain.dto.Book.OwnCommentDto;
import com.seoro.seoro.domain.dto.Book.ReadBookDto;
import com.seoro.seoro.domain.dto.Book.ReviewDto;
import com.seoro.seoro.domain.dto.Group.GroupShowDto;
import com.seoro.seoro.domain.dto.Library.LibraryDto;
import com.seoro.seoro.domain.dto.Member.FriendDto;
import com.seoro.seoro.domain.dto.Member.MemberDto;
import com.seoro.seoro.domain.dto.ResultResponseDto;
import com.seoro.seoro.domain.entity.Book.BookReport;
import com.seoro.seoro.domain.entity.Book.BookReportPhoto;
import com.seoro.seoro.domain.entity.Book.OwnBook;
import com.seoro.seoro.domain.entity.Book.ReadBook;
import com.seoro.seoro.domain.entity.Book.Review;
import com.seoro.seoro.domain.entity.Groups.GroupJoin;
import com.seoro.seoro.domain.entity.Groups.Groups;
import com.seoro.seoro.domain.entity.Member.Friend;
import com.seoro.seoro.domain.entity.Member.Member;
import com.seoro.seoro.repository.Book.BookReportPhotoRepository;
import com.seoro.seoro.repository.Book.BookReportRepository;
import com.seoro.seoro.repository.Book.OwnBookRepository;
import com.seoro.seoro.repository.Book.ReadBookRepository;
import com.seoro.seoro.repository.Member.FriendRepository;
import com.seoro.seoro.repository.Member.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryServiceImpl implements LibraryService {
	private final BookReportRepository bookReportRepository;
	private final BookReportPhotoRepository bookReportPhotoRepository;
	private final ReadBookRepository readBookRepository;
	private final OwnBookRepository ownBookRepository;
	private final MemberRepository memberRepository;
	private final FriendRepository friendRepository;
	@Override
	public LibraryDto libraryMain(Long memberId, Long meId) {
		log.info("memberId: " + memberId + " meId: " + meId);

		LibraryDto responseDto = new LibraryDto();
		Member member = memberRepository.findById(memberId).orElse(null);
		Member me = memberRepository.findById(meId).orElse(null);

		if(member == null || me == null) {
			responseDto.setMessege("?????? ????????? ????????????.");
			return responseDto;
		}

		// ?????? ????????? ??????
		boolean isOwn;
		if(memberId.equals(meId)) {
			isOwn = true;
		} else {
			isOwn = false;
		}
		responseDto.setOwn(isOwn);

		// ?????????
		responseDto.setMemberInfo(new MemberDto(member));

		// ?????? ??????
		List<GroupShowDto> groupShowDto = getMyGroups(memberId);
		responseDto.setMyGroups(groupShowDto);

		// ?????? ??????
		List<OwnBookDto> ownBookDtoList = getOwnBookList(member);
		responseDto.setMyOwnBooks(ownBookDtoList);

		// ?????? ??????
		List<ReadBookDto> readBookDtoList = getReadBookList(member);
		responseDto.setMyReadBooks(readBookDtoList);

		// ????????? ?????????
		Long countOwnComment = ownBookRepository.countByMember(member);
		responseDto.setMyOwnComment(countOwnComment);

		// ?????? ?????????
		Long countReview = (long)member.getReviews().size();
		responseDto.setMyReview(countReview);

		// ?????? ?????? // ?????? api ??????

		// ????????? ??????
		Long countFollower = friendRepository.countByFollowing(member.getMemberId());
		responseDto.setMyFollowers(countFollower);

		// ????????? ??????
		Long countFollowing = friendRepository.countByFollower(member);
		responseDto.setMyFollowings(countFollowing);

		// ????????? ??????
		boolean isFollowing;
		if(!isOwn) {
			// ???????????? ?????? ????????? ???????????? ??? ???????????? ???????????? ??????
			Friend friend = friendRepository.findByFollowerAndFollowing(me, memberId).orElse(null);
			if(friend != null) {
				isFollowing = true;
			} else {
				isFollowing = false;
			}
		} else {
			isFollowing = false;
		}
		responseDto.setFollowing(isFollowing);

		return responseDto;
	}

	@Override
	public List<OwnBookDto> viewMyOwnBook(Long memberId) {
		Member member = memberRepository.findByMemberId(memberId);
		if(member == null) {
			return new ArrayList<>();
		}

		List<OwnBookDto> ownBookDtoList = getOwnBookList(member);
		return ownBookDtoList;
	}

	@Override
	public List<ReadBookDto> viewMyReadBook(Long memberId) {
		Member member = memberRepository.findByMemberId(memberId);
		if(member == null) {
			return new ArrayList<>();
		}

		List<ReadBookDto> readBookDtoList = getReadBookList(member);
		return readBookDtoList;
	}

	@Override
	public List<GroupShowDto> viewMyGroup(Long memberId) {
		return getMyGroups(memberId);
	}

	@Override
	public ResultResponseDto makeOwnBook(Long memberId, OwnBookDto requestDto) {
		// ??? ????????? BookController??? api??? ?????? ???????????? ????????? ????????? LibraryController api ??????
		Member member = memberRepository.findById(memberId).orElse(null);
		ResultResponseDto responseDto = new ResultResponseDto();

		if(member == null) {
			responseDto.setResult(false);
			responseDto.setMessege("?????? ???????????????.");
			return responseDto;
		}

		OwnBook checkBook = ownBookRepository.findByMemberAndIsbn(member, requestDto.getIsbn()).orElse(null);
		if(checkBook != null) {
			responseDto.setResult(false);
			responseDto.setMessege("?????? ????????? ???????????????.");
			return responseDto;
		}

		OwnBook ownBook = OwnBook.builder()
			.member(member)
			.isbn(requestDto.getIsbn())
			.bookTitle(requestDto.getBookTitle())
			.bookImage(requestDto.getBookImage())
			.ownComment(requestDto.getOwnComment())
			.author(requestDto.getAuthor())
			.bookdescrib(requestDto.getBookDescrib())
			.isOwn(true)
			.build();

		ownBookRepository.save(ownBook);
		responseDto.setResult(true);

		return responseDto;
	}

	@Override
	public ResultResponseDto removeOwnBook(Long memberId, String isbn) {
		ResultResponseDto responseDto = new ResultResponseDto();

		Member member = memberRepository.findByMemberId(memberId);
		if(member == null) {
			responseDto.setMessege("?????? ???????????????.");
			responseDto.setResult(false);
			return responseDto;
		}

		OwnBook ownBook = ownBookRepository.findByMemberAndIsbn(member, isbn).orElse(null);
		if(ownBook == null) {
			responseDto.setMessege("???????????? ?????? ???????????????.");
			responseDto.setResult(false);
			return responseDto;
		}

		ownBookRepository.delete(ownBook);
		responseDto.setResult(true);

		return responseDto;
	}

	@Override
	public ResultResponseDto removeReadBook(Long memberId, String isbn) {
		log.info("memberId: " + memberId);
		log.info("isbn: " + isbn);

		ResultResponseDto responseDto = new ResultResponseDto();

		Member member = memberRepository.findByMemberId(memberId);
		if(member == null) {
			responseDto.setMessege("?????? ???????????????.");
			responseDto.setResult(false);
			return responseDto;
		}

		ReadBook readBook = readBookRepository.findByIsbnAndMember(isbn, member).orElse(null);
		if(readBook == null) {
			responseDto.setMessege("???????????? ?????? ???????????????.");
			responseDto.setResult(false);
			return responseDto;
		}

		// ?????? ?????? ????????? ????????? ??????????????? ?????? ?????? ???????????? ?????? ??????

		readBookRepository.delete(readBook);
		responseDto.setResult(true);

		return responseDto;
	}

	@Override
	public List<OwnCommentDto> viewMyComment(Long memberId) {
		Member member = memberRepository.findById(memberId).orElse(null);

		if(member == null) {
			return new ArrayList<>();
		}

		List<OwnBook> ownBooks = member.getOwnBooks();
		List<OwnCommentDto> commentDtoList = new ArrayList<>();
		for(OwnBook ownBook : ownBooks) {
			commentDtoList.add(new OwnCommentDto(ownBook));
		}

		return commentDtoList;
	}

	@Override
	public List<ReviewDto> viewMyReview(Long memberId) {
		Member member = memberRepository.findById(memberId).orElse(null);

		if(member == null) {
			return new ArrayList<>();
		}

		List<Review> reviews = member.getReviews();
		List<ReviewDto> reviewDtoList = new ArrayList<>();
		for(Review review : reviews) {
			reviewDtoList.add(new ReviewDto(review));
		}

		return reviewDtoList;
	}

	@Override
	public List<BookReportDto> viewBookReportList(Long memberId) {
		Member member = memberRepository.findById(memberId).orElse(null);

		if(member == null) {
			return new ArrayList<>();
		}

		List<BookReportDto> bookReportDtoList = new ArrayList<>();
		List<BookReport> bookReports = member.getBookReports();
		for(BookReport bookReport : bookReports) {
			bookReportDtoList.add(new BookReportDto(bookReport));
		}

		return bookReportDtoList;
	}

	@Override
	public ResultResponseDto makeBookReport(BookReportDto requestDto, Long memberId) {
		ResultResponseDto responseDto = new ResultResponseDto();

		ReadBook readBook = readBookRepository.findByReadBookId(requestDto.getReadBookId()).orElse(null);
		if(readBook == null) {
			responseDto.setMessege("?????? ???????????? ????????????.");
			return responseDto;
		}

		Member member = memberRepository.findById(memberId).orElse(null);
		if(member == null) {
			responseDto.setMessege("????????? ???????????? ????????????.");
			return responseDto;
		}

		BookReport bookReport = BookReport.builder()
			.readBook(readBook)
			.member(member)
			.bookReportTitle(requestDto.getBookReportTitle())
			.bookReportContent(requestDto.getBookReportContent())
			.build();

		bookReportRepository.save(bookReport);

		// BookReport saveBookReport = bookReportRepository.findByReadBookAndMember(readBook, member).orElse(null);
		// if(saveBookReport == null) {
		// 	responseDto.setMessege("????????? ?????? ??????");
		// 	return responseDto;
		// }

		// ????????? ??????
		if(requestDto.getPhoto() != null) {
			BookReportPhoto bookReportPhoto = BookReportPhoto.builder()
				.bookReport(bookReport)
				.photo(requestDto.getPhoto())
				.build();

			bookReportPhotoRepository.save(bookReportPhoto);
		}

		responseDto.setResult(true);

		return responseDto;
	}


	@Override
	public BookReportDto viewBookReport(Long bookReportId) {
		BookReport bookReport = bookReportRepository.findById(bookReportId).orElse(null);

		if(bookReport == null) {
			BookReportDto responsetDto = new BookReportDto();
			responsetDto.setResult(false);
			return responsetDto;
		}

		BookReportDto responsetDto = new BookReportDto(bookReport);

		// ????????? ??????
		BookReportPhoto bookReportPhoto = bookReportPhotoRepository.findByBookReport(bookReport).orElse(null);
		if(bookReportPhoto != null) {
			responsetDto.setPhoto(bookReportPhoto.getPhoto());
		}

		return responsetDto;
	}

	@Override
	public ResultResponseDto modifyBookReport(BookReportDto requestDto) {
		ResultResponseDto responseDto = new ResultResponseDto();

		BookReport bookReport = bookReportRepository.findById(requestDto.getBookReportId()).orElse(null);
		if(bookReport == null) {
			responseDto.setMessege("?????? ????????? ????????????.");
		}

		ReadBook readBook = readBookRepository.findByReadBookId(requestDto.getReadBookId()).orElse(null);
		if(readBook == null) {
			responseDto.setMessege("?????? ????????? ????????????.");
		}

		// ????????? ?????? // ?????? ??????
		if(requestDto.getPhoto() != null) {
			BookReportPhoto chackBookReportPhoto = bookReportPhotoRepository.findByBookReport(bookReport).orElse(null);
			BookReportPhoto bookReportPhoto;

			if(chackBookReportPhoto == null) {
				bookReportPhoto = BookReportPhoto.builder()
					.bookReport(bookReport)
					.photo(requestDto.getPhoto())
					.build();
			} else {
				bookReportPhoto = BookReportPhoto.builder()
					.bookReportPhotoId(chackBookReportPhoto.getBookReportPhotoId())
					.bookReport(bookReport)
					.photo(requestDto.getPhoto())
					.build();
			}

			bookReportPhotoRepository.save(bookReportPhoto);
		}

		BookReport newBookReport = BookReport.builder()
			.bookReportId(requestDto.getBookReportId())
			.readBook(readBook)
			.member(bookReport.getMember())
			.bookReportTitle(requestDto.getBookReportTitle())
			.bookReportContent(requestDto.getBookReportContent())
			.build();

		bookReportRepository.save(newBookReport);
		responseDto.setResult(true);

		return responseDto;
	}

	@Override
	public ResultResponseDto removeBookReport(Long bookReportId) {
		ResultResponseDto responseDto = new ResultResponseDto();

		BookReport bookReport = bookReportRepository.findById(bookReportId).orElse(null);
		if(bookReport == null) {
			responseDto.setMessege("?????? ????????? ?????? ??? ????????????.");
			return responseDto;
		}

		// ????????? ??????
		BookReportPhoto bookReportPhoto = bookReportPhotoRepository.findByBookReport(bookReport).orElse(null);
		if(bookReportPhoto != null) {
			bookReportPhotoRepository.delete(bookReportPhoto);
		}
		bookReportRepository.delete(bookReport);

		responseDto.setResult(true);

		return responseDto;
	}

	@Override
	public ResultResponseDto makeFriend(Long memberId, Long meId) {
		ResultResponseDto responseDto = new ResultResponseDto();

		Member member = memberRepository.findByMemberId(memberId);
		Member me = memberRepository.findByMemberId(meId);
		if(member == null || member == null) {
			responseDto.setResult(false);
			responseDto.setMessege("?????? ????????? ????????????.");
			return responseDto;
		}

		Friend checkFriend = friendRepository.findByFollowerAndFollowing(me, memberId).orElse(null);
		if(checkFriend != null) {
			responseDto.setResult(false);
			responseDto.setMessege("?????? ????????? ???????????????.");
			return responseDto;
		}

		Friend friend = Friend.builder()
			.follower(me)
			.following(memberId)
			.build();
		friendRepository.save(friend);
		responseDto.setResult(true);

		return responseDto;
	}

	@Override
	public ResultResponseDto removeFriend(Long memberId, Long meId) {
		ResultResponseDto responseDto = new ResultResponseDto();

		Member member = memberRepository.findByMemberId(memberId);
		Member me = memberRepository.findByMemberId(meId);
		if(member == null || member == null) {
			responseDto.setResult(false);
			responseDto.setMessege("?????? ????????? ????????????.");
			return responseDto;
		}

		Friend friend = friendRepository.findByFollowerAndFollowing(me, memberId).orElse(null);
		if(friend == null) {
			responseDto.setResult(false);
			responseDto.setMessege("????????? ????????????.");
			return responseDto;
		}

		friendRepository.delete(friend);
		responseDto.setResult(true);

		return responseDto;
	}

	@Override
	public List<FriendDto> viewFollowingList(Long memberId) {
		Member member = memberRepository.findById(memberId).orElse(null);
		if(member == null) {
			return new ArrayList<>();
		}

		List<FriendDto> friendDtoList = new ArrayList<>();
		List<Friend> friends = member.getFriends();
		for(Friend friend : friends) {
			friendDtoList.add(new FriendDto(friend));
		}

		return friendDtoList;
	}

	@Override
	public List<FriendDto> viewFollowerList(Long memberId) {
		Member member = memberRepository.findById(memberId).orElse(null);
		if(member==null){
			return new ArrayList<>();
		}
		List<FriendDto> followerDtoList = new ArrayList<>();
		List<Friend> friends = friendRepository.findByFollowing(member.getMemberId());
		for(Friend friend : friends){
			followerDtoList.add(new FriendDto(friend));
		}
		return followerDtoList;
	}

	private List<OwnBookDto> getOwnBookList(Member member) {
		List<OwnBook> ownBooks = member.getOwnBooks();
		List<OwnBookDto> ownBookDtoList = new ArrayList<>();
		for(OwnBook ownBook : ownBooks) {
			ownBookDtoList.add(new OwnBookDto(ownBook));
		}
		return ownBookDtoList;
	}

	private List<ReadBookDto> getReadBookList(Member member) {
		List<ReadBook> readBooks = member.getReadBooks();

		List<ReadBookDto> readBookDtoList = new ArrayList<>();
		for(ReadBook readBook : readBooks) {
			readBookDtoList.add(new ReadBookDto(readBook));
		}

		return readBookDtoList;
	}

	private List<GroupShowDto> getMyGroups(Long memberId) {
		Member member = memberRepository.findById(memberId).orElse(null);

		if(member == null) {
			return new ArrayList<>();
		}

		List<GroupShowDto> groupShowDto = new ArrayList<>();
		List<GroupJoin> findGroupJoin = member.getGroupJoins();
		for(GroupJoin groupJoin : findGroupJoin) {
			Groups groups = groupJoin.getGroups();
			groupShowDto.add(GroupShowDto.builder()
				.groupName(groups.getGroupName())
				.groupDescrib(groups.getGroupIntroduction())
				.groupProfile(groups.getGroupProfile())
				.groupId(groups.getGroupId())
				.build());
		}

		return groupShowDto;
	}
}
