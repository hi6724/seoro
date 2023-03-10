import React, { useState } from 'react';
import InfiniteScroll from 'react-infinite-scroll-component';
import { useNavigate } from 'react-router-dom';
import { useQuery } from 'react-query';
import { MdLocalCafe } from 'react-icons/md';

import './PlaceRecommend.styles.scss';
import { placeRecomendAPI } from '@src/API/placeAPI';
import { useUser } from '@src/hooks/useUser';

const PlaceRecommend = () => {
  const [placesData, setPlacesData] = useState<any>();
  const getPlacesData = async () => await (await fetch('/places.json')).json();
  const { data } = useQuery('place-recommend', getPlacesData);

  const user = useUser();
  const memberId = user?.memberId;
  const placeData = placeRecomendAPI(memberId);
  console.log(placeData);

  const navigate = useNavigate();

  const fetchData = () => {
    setTimeout(() => {
      setPlacesData(placesData.concat(Array.from({ length: 6 })));
    }, 1500);
  };

  return (
    <InfiniteScroll
      className="place-recommend-container"
      dataLength={8}
      next={fetchData}
      hasMore={true}
      loader=""
    >
      <div>
        {placeData?.map((placeRecommend: any, id: number) => (
          <div
            key={id}
            className="place-container"
            onClick={() => navigate(`/places/${placeRecommend.placeId}`)}
          >
            <h2>
              <MdLocalCafe />
              &nbsp;
              {placeRecommend.placeName}
            </h2>
            {/* <h6>
              {placeRecommend.title}&nbsp;
              {placeRecommend.title}&nbsp;
              {placeRecommend.title}
            </h6> */}
            <img src={placeRecommend.placePhotoList[0]} alt="" />
            <div className="line" />
          </div>
        ))}
      </div>
    </InfiniteScroll>
  );
};

export default PlaceRecommend;
