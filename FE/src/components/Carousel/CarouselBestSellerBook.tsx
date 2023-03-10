import React, { Suspense, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Slider from 'react-slick';

import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';
import './CarouselNearBook.styles.scss';
import { bestSellerAPI } from '@src/API/bookAPI';

const settings = {
  dots: false,
  speed: 500,
  slidesToShow: 4,
  slidesToScroll: 1,
  swipeToSlide: true,
};

export default function CarouselNearBook() {
  const booksData = bestSellerAPI();
  const navigate = useNavigate();

  return (
    <Suspense fallback={<span>Loading...</span>}>
      <Slider {...settings} className="my-slider-near-book">
        {booksData?.map((data: any, i: number) => (
          <div
            key={i}
            className="recommend-near-book-container"
            onClick={() => navigate(`/book/${data.isbn}`)}
          >
            <div>
              <img src={data.bookImage} alt="" />
              <h2>{data.bookTitle}</h2>
            </div>
          </div>
        ))}
      </Slider>
    </Suspense>
  );
}
