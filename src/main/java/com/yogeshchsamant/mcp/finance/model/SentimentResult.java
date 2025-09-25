package com.yogeshchsamant.mcp.finance.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SentimentResult {

    private String overallSentiment;
    private double averageScore;
    private int positiveCount;
    private int negativeCount;
    private int neutralCount;
    private int totalArticles;

    public SentimentResult(String overallSentiment, double averageScore) {
        this.overallSentiment = overallSentiment;
        this.averageScore = averageScore;
    }

}
