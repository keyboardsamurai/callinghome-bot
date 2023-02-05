package com.keyboardsamurais.apps.db.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ClassifiedItem {
    private Integer id;
    private String title;
    private String content;
    private String url;
    private String imageUrl;
    private Instant created;

}
