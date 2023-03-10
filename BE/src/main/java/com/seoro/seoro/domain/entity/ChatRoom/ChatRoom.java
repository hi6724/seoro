package com.seoro.seoro.domain.entity.ChatRoom;

import javax.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ChatRoom implements Serializable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomId;

    @OneToMany(mappedBy = "chatRoom")
    @Builder.Default
    private List<ChatRoomContent> contents = new ArrayList<>();
    @OneToMany(mappedBy = "chatRoom")
    @Builder.Default
    private List<ChatRoomJoin> joins = new ArrayList<>();
}
