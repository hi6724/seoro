package com.seoro.seoro.domain.entity.Place;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
public class PlaceReviewPhoto implements Serializable {
    private Long placeReviewId;
    private String photo;
}