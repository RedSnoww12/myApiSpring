package com.etna.myapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
@Entity
@Table(name = "video_format")
public class VideoFormat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    @NonNull
    private String code;

    @Column(nullable = false)
    @NonNull
    private String uri;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Video video;

}
