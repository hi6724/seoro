package com.seoro.seoro.service.Book;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.seoro.seoro.domain.dto.Book.BookDto;
import com.seoro.seoro.domain.dto.Book.ReviewDto;
// import com.seoro.seoro.domain.entity.User.User;
// import com.seoro.seoro.repository.Book.OwnBookRepository;
import com.seoro.seoro.domain.dto.ResultResponseDto;
import com.seoro.seoro.domain.entity.Book.Review;
import com.seoro.seoro.domain.entity.Member.Member;
import com.seoro.seoro.repository.Book.ReadBookRepository;
import com.seoro.seoro.repository.Book.ReviewRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

	// final BookRepository bookRepository;
	final ReviewRepository reviewRepository;
	final ReadBookRepository readBookRepository;
	// final OwnBookRepository ownBookRepository;

	@Override
	public ResultResponseDto makeReview(String isbn, ReviewDto requestDto) {
		ResultResponseDto resultResponseDto = new ResultResponseDto();

		Long memberId = 1L;
		Member writer = new Member();

		Review review = Review.builder()
			.member(writer)
			.reviewContent(requestDto.getReviewContent())
			.readBook(readBookRepository.findByIsbn(requestDto.getIsbn()).get())
			.build();
		reviewRepository.save(review);
		resultResponseDto.setResult(true);

		return resultResponseDto;
	}

	@Override
	public ReviewDto findReviewByIsbnAndMemberId(String isbn) {
		Long member_id=1L;
		Review review= reviewRepository.findByReadBook_IsbnAndMember_MemberId(isbn,member_id);
		ReviewDto dtoOutput = ReviewDto.builder()
			.userName(review.getMember().getMemberName())
			.isbn(review.getReadBook().getIsbn())
			.reviewContent(review.getReviewContent())
			.build();

		return dtoOutput;
	}

	// public List<OwnBookDto> findOwnBookByIsbn(String isbn) {
	// 	List<OwnBook> list = ownBookRepository.findByBook_Isbn(isbn);
	// 	List<OwnBookDto> dtoList = new ArrayList<>();
	// 	for(OwnBook ownBook: list){
	// 		dtoList.add(OwnBookDto.builder()
	// 			.isbn(ownBook.getBook().getIsbn())
	// 			.userId(ownBook.getUser().getUserId())
	// 			.ownComment(ownBook.getOwnComment())
	// 			.build());
	// 	}
	// 	return dtoList;

	// }
	// @Override
	// public List<BookDto> findAllBooks() {
	// 	List<Book> list = bookRepository.findAll();
	// 	List<BookDto> dtoList = new ArrayList<>();
	// 	for(Book book: list){
	// 		dtoList.add(BookDto.builder()
	// 				.isbn(book.getIsbn())
	// 				.bookTitle(book.getBookTitle())
	// 				.bookAuthor(book.getBookAuthor())
	// 				.bookPublisher(book.getBookPublisher())
	// 				.bookImage(book.getBookImage())
	// 				.bookDescrib(book.getBookDescrib())
	// 				.bookPubDate(book.getBookPubDate())
	// 				.build());
	// 	}
	// 	return dtoList;

	// }

	@Override
	public BookDto findByIsbn(String isbn) throws IOException, ParseException {
		BookDto output;
		URL url =new URL("http://data4library.kr/api/srchDtlList?authKey=5131ae002fe7c43930587697cae1f2fe3b9495c7df43cc23b8ee69e3ccb017f7&isbn13="+isbn+"&format=json");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
		String result = br.readLine();
		System.out.println(result);
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject)jsonParser.parse(result);
		JSONObject responseResult = (JSONObject)jsonObject.get("response");
		ArrayList info = new ArrayList((Collection)responseResult.get("detail"));
		JSONObject jsonlist = (JSONObject)info.get(0);
		Map outputlist = (Map)jsonlist.get("book");

		long count_review = reviewRepository.countByReadBook_Isbn(isbn);
		long count_readpeople = readBookRepository.countByIsbn(isbn);
		//연도 형식 파악하고 추가하기
		output = BookDto.builder()
			.bookImage(outputlist.get("bookImageURL").toString())
			.bookTitle(outputlist.get("bookname").toString())
			.isbn(outputlist.get("isbn13").toString())
			.bookAuthor(outputlist.get("authors").toString())
			.bookDescrib(outputlist.get("description").toString())
			.result(true)
			.countReview(count_review)
			.countReader(count_readpeople)
			.build();
		return output;
	}
	//내 주변 보유사용자, 한줄평 출력 추가 필요

	@Override
	public List<BookDto> findBook(String input) throws IOException, ParseException {
		List<BookDto> output = new ArrayList<>();
		URL url =new URL("http://data4library.kr/api/srchBooks?authKey=5131ae002fe7c43930587697cae1f2fe3b9495c7df43cc23b8ee69e3ccb017f7&keyword="+ URLEncoder.encode(input,"utf-8")+"&pageSize=100&format=json");
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
		String result = br.readLine();
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject)jsonParser.parse(result);
		JSONObject responseResult = (JSONObject)jsonObject.get("response");
		ArrayList docs = new ArrayList((Collection)responseResult.get("docs"));
		for(Object list: docs){
			JSONObject jsonlist = (JSONObject)list;
			Map outputlist = (Map)jsonlist.get("doc");
			System.out.println(jsonlist);
			System.out.println("!!");
			BookDto bookDto = new BookDto();
			output.add(BookDto.builder()
				.bookImage(outputlist.get("bookImageURL").toString())
				.bookTitle(outputlist.get("bookname").toString())
				.isbn(outputlist.get("isbn13").toString())
				.result(true)
				.build());
		}
		return output;
	}

	@Override
	public List findBestSeller() throws IOException {
		List<BookDto> output = new ArrayList<>();
		try {
			Calendar today = new GregorianCalendar();
			today.add(Calendar.DATE,-7);
			SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
			String now = SDF.format(today.getTime());
			URL url = new URL("http://data4library.kr/api/loanItemSrch?authKey=5131ae002fe7c43930587697cae1f2fe3b9495c7df43cc23b8ee69e3ccb017f7&startDt="+now+"&pageSize=10&format=json");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

			String result = br.readLine();
			JSONParser jsonParser = new JSONParser();

			JSONObject jsonObject = (JSONObject)jsonParser.parse(result);
			JSONObject responseResult = (JSONObject)jsonObject.get("response");
			ArrayList docs = new ArrayList((Collection)responseResult.get("docs"));
			for(Object list: docs){
				JSONObject jsonlist = (JSONObject)list;
				Map outputlist = (Map)jsonlist.get("doc");
				BookDto bookDto = new BookDto();
				output.add(BookDto.builder()
						.bookImage(outputlist.get("bookImageURL").toString())
						.bookTitle(outputlist.get("bookname").toString())
						.isbn(outputlist.get("isbn13").toString())
						.result(true)
					.build());
			}
		}catch (Exception e) {

		}
		return output;
	}
}
