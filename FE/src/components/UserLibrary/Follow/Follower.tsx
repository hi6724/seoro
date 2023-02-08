import React, { useEffect, useState } from 'react';
import { useMyQuery } from '@src/hooks/useMyQuery';

import './Follow.styles.scss';

function Follower() {
  const data = useMyQuery('/userFollower.json');

  return (
    <div className="user-follow-container">
      {data?.map((user: any, i: number) => (
        <div key={i} className="user-follow-item">
          <img src={user.avatar} alt="" />
          <p>{user.nickname}</p>
        </div>
      ))}
    </div>
  );
}

export default Follower;
