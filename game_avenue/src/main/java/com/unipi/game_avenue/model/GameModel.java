package com.unipi.game_avenue.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Table(name = "games")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuppressWarnings("JpaDataSourceORMInspection")
public class GameModel {

    @Id
    private Long id;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 100, nullable = false)
    private String thumbnail;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(nullable = false)
    private String short_description;

    @Column(length = 100, nullable = false)
    private String game_url;

    @Column(length = 30, nullable = false)
    private String genre;

    @Column(length = 30, nullable = false)
    private String platform;

    @Column(length = 100, nullable = false)
    private String publisher;

    @Column(length = 100, nullable = false)
    private String developer;

    @Column(length = 20, nullable = false)
    private String release_date;
}
