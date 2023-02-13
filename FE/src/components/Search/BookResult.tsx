import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useMyQuery } from '@src/hooks/useMyQuery';

import './BookResult.styles.scss';
function BookResult() {
  const data = useMyQuery('/books.json');
  const navigate = useNavigate();

  return (
    <div className="book-result-container">
      {data?.map((book: any, i: number) => (
        <div
          key={i}
          className="book-result-item"
          onClick={() => navigate(`/book/1`)}
        >
          <img src={book.image_url} alt="" />

          <div className="book-detail-container">
            <div className="book-detail-item">
              <h1>{book.title}</h1>
              <h2>{book.author}</h2>
            </div>

            <p>{book.description}</p>
          </div>
        </div>
      ))}
    </div>
  );
}

export default BookResult;