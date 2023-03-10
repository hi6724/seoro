import React, { Suspense, useEffect, useState } from 'react';
import { useMyQuery } from '@src/hooks/useMyQuery';
import { useNavigate, useParams } from 'react-router-dom';
import Slider from 'react-slick';

import './HoldBook.styles.scss';
import { holdBooksAPI } from '@src/API/memberAPI';
import { holdBookDetailAPI } from '@src/API/bookAPI';

const settings = {
  dots: false,
  speed: 500,
  slidesToShow: 4,
  slidesToScroll: 1,
  swipeToSlide: true,
  infinite: false,
};

interface IHoldBookProps {
  isbn: any;
}

function HoldBook({ isbn }: IHoldBookProps) {
  const param = useParams();
  const booksData = holdBookDetailAPI(param?.isbn, param?.memberName);
  const navigate = useNavigate();

  return (
    <div className="hold-book-container">
      <h1>{param?.memberName}님의 또 다른 보유 도서</h1>
      <Suspense fallback={<span>Loading...</span>}>
        <Slider {...settings} className="user-slider-hold-book">
          {booksData?.ownBookList?.map((data: any, i: number) => (
            <div
              key={i}
              className="hold-book-container"
              onClick={() =>
                navigate(`/profile/${param?.memberName}/book/${data.isbn}`)
              }
            >
              <div>
                <img src={data.bookImage} alt="" />
                <h2>{data.bookTitle}</h2>
              </div>
            </div>
          ))}
        </Slider>
      </Suspense>
    </div>
  );
}

export default HoldBook;
