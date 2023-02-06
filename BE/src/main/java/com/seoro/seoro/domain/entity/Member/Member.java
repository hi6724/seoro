package com.seoro.seoro.domain.entity.Member;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.seoro.seoro.domain.entity.Book.OwnBook;
import com.seoro.seoro.domain.entity.Book.ReadBook;
import com.seoro.seoro.domain.entity.Book.Review;
import com.seoro.seoro.domain.entity.ChatRoom.ChatRoomJoin;
import com.seoro.seoro.domain.entity.Groups.GroupJoin;
import com.seoro.seoro.domain.entity.Groups.Groups;
import com.seoro.seoro.domain.entity.Place.Place;
@Entity
@Getter
public class Member implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    @NotNull
    @Column(unique = true)
    private String memberEmail;
    @NotNull
    @Column(unique = true)
    private String memberName;
    @NotNull
    private String memberPassword;
    private String memberProfile;
    private String memberDongCode;
    @Enumerated(EnumType.STRING)
    private LoginType loginType;
    @Temporal(TemporalType.DATE)
    private Date withdrawalDate;
    private Integer memberScore;
    private Long memberGenre;
    @OneToMany(mappedBy = "friend")
    private List<Friend> friends = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<Review> reviews = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<ChatRoomJoin> chatRooms = new ArrayList<>();
    @OneToMany(mappedBy = "host")
    private List<Groups> hostGroups = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<GroupJoin> groupJoins = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<Place> places = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<OwnBook> ownBooks = new ArrayList<>();
    @OneToMany(mappedBy = "member")
    private List<ReadBook> readBooks = new ArrayList<>();
}