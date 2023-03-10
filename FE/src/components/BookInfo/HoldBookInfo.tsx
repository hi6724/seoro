import React from 'react';
import { useLocation, useParams } from 'react-router-dom';

import ExchangeAvailable from '@components/NearBooks/ExchangeAvailable';

import './BookInfo.styles.scss';
import { holdBookDetailAPI } from '@src/API/bookAPI';
import { useUser } from '@src/hooks/useUser';

interface IBookInfo {
  isbn: any;
}
function BookInfo({ isbn }: IBookInfo) {
  const location = useLocation();
  const param = useParams();
  const isUser = location.pathname.includes('profile');
  const user = useUser();

  const data = holdBookDetailAPI(param?.isbn, param?.memberName);
  // console.log(booksData);

  return (
    <div className="book-info-container">
      {isUser ? (
        <div>
          <div className="book-cover">
            <img src={data?.bookImage} alt="" />
          </div>
        </div>
      ) : (
        <div>
          <div className="book-cover">
            <img src={data?.bookImage} alt="" />
          </div>
        </div>
      )}

      <div className="book-primary-info">
        <h1>{data?.bookTitle}</h1>
        <p>
          {data?.bookAuthor} 지음 · {data?.bookPublisher} · {data?.bookPubDate}{' '}
          출간
        </p>
      </div>

      <h1>책 소개</h1>
      <div className="book-description">
        <p>{data?.bookDescrib}</p>
      </div>
      <div></div>
    </div>
  );
}
export default BookInfo;
