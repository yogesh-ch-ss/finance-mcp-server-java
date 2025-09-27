package com.yogeshchsamant.mcp.finance.dto;

import java.util.List;

import com.yogeshchsamant.mcp.finance.model.NewsData;
import com.yogeshchsamant.mcp.finance.model.SentimentResult;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NewsAnalysisDto {

    private String symbol;
    private List<NewsData> newsArticles;
    private SentimentResult sentimentAnalysis;
    private String newsSummary;
    private String marketImpact;

    public NewsAnalysisDto(String symbol, List<NewsData> newsArticles, SentimentResult sentimentAnalysis) {
        this.symbol = symbol;
        this.newsArticles = newsArticles;
        this.sentimentAnalysis = sentimentAnalysis;
    }

}
