package com.yogeshchsamant.mcp.finance.model;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewsData {

    private String title;
    private String summary;
    private String url;
    private String source;
    private LocalDateTime publishedAt;
    private String sentiment;
    private Double sentimentScore;

    public NewsData(String title, String summary, String source) {
        this.title = title;
        this.summary = summary;
        this.source = source;
    }

}
