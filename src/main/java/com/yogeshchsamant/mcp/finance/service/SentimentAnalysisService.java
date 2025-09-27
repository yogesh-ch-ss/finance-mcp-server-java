package com.yogeshchsamant.mcp.finance.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yogeshchsamant.mcp.finance.dto.NewsAnalysisDto;
import com.yogeshchsamant.mcp.finance.model.NewsData;
import com.yogeshchsamant.mcp.finance.model.SentimentResult;

@Service
public class SentimentAnalysisService {
    @Autowired
    private AlphaVantageService alphaVantageService;

    public NewsAnalysisDto analyzeStockSentiment(String symbol) {
        List<NewsData> newsData = alphaVantageService.getNewsData(symbol);
        SentimentResult sentimentResult = calculateSentiment(newsData);

        NewsAnalysisDto analysis = new NewsAnalysisDto(symbol, newsData, sentimentResult);
        analysis.setNewsSummary(generateNewsSummary(newsData, symbol));
        analysis.setMarketImpact(generateMarketImpact(sentimentResult, newsData));

        return analysis;
    }

    private SentimentResult calculateSentiment(List<NewsData> newsData) {
        SentimentResult result = new SentimentResult();

        if (newsData.isEmpty()) {
            result.setOverallSentiment("NEUTRAL");
            result.setAverageScore(0.0);
            return result;
        }

        int positive = 0;
        int negative = 0;
        int neutral = 0;
        double totalScore = 0.0;
        int validScores = 0;

        for (NewsData news : newsData) {
            if (news.getSentiment() != null) {
                String sentiment = news.getSentiment().toUpperCase();
                if (sentiment.contains("BULLISH")) {
                    positive++;
                } else if (sentiment.contains("BEARISH")) {
                    negative++;
                } else {
                    neutral++;
                }
            }

            if (news.getSentimentScore() != null) {
                totalScore += news.getSentimentScore();
                validScores++;
            }
        }

        result.setPositiveCount(positive);
        result.setNegativeCount(negative);
        result.setNeutralCount(neutral);
        result.setTotalArticles(newsData.size());

        if (validScores > 0) {
            result.setAverageScore(totalScore / validScores);
        }

        if (positive > negative && positive > neutral) {
            result.setOverallSentiment("BULLISH");
        } else if (negative > positive && negative > neutral) {
            result.setOverallSentiment("BEARISH");
        } else {
            result.setOverallSentiment("NEUTRAL");
        }

        return result;
    }

    private String generateNewsSummary(List<NewsData> newsData, String symbol) {
        StringBuilder summary = new StringBuilder();

        summary.append("Recent News Summary for ").append(symbol).append(":\n\n");
        summary.append("Total Articles Analyzed: ").append(newsData.size()).append("\n\n");

        List<NewsData> bullishNews = new ArrayList<>();
        List<NewsData> bearishNews = new ArrayList<>();

        for (NewsData news : newsData) {
            if (news.getSentiment() != null) {
                String sentiment = news.getSentiment().toUpperCase();
                if (sentiment.contains("BULLISH")) {
                    bullishNews.add(news);
                } else if (sentiment.contains("BEARISH")) {
                    bearishNews.add(news);
                }
            }
        }

        if (!bullishNews.isEmpty()) {
            summary.append("POSITIVE NEWS HIGHLIGHTS:\n");
            int count = Math.min(3, bullishNews.size());
            for (int i = 0; i < count; i++) {
                NewsData news = bullishNews.get(i);
                summary.append("• ").append(news.getTitle()).append("\n");
                summary.append("  Source: ").append(news.getSource()).append("\n");

                String newsSummary = news.getSummary();
                if (newsSummary.length() > 100) {
                    summary.append("  Summary: ").append(newsSummary.substring(0, 100)).append("...\n");
                } else {
                    summary.append("  Summary: ").append(newsSummary).append("\n");
                }
                summary.append("\n");
            }
        }

        if (!bearishNews.isEmpty()) {
            summary.append("NEGATIVE NEWS HIGHLIGHTS:\n");
            int count = Math.min(3, bearishNews.size());
            for (int i = 0; i < count; i++) {
                NewsData news = bearishNews.get(i);
                summary.append("• ").append(news.getTitle()).append("\n");
                summary.append("  Source: ").append(news.getSource()).append("\n");

                String newsSummary = news.getSummary();
                if (newsSummary.length() > 100) {
                    summary.append("  Summary: ").append(newsSummary.substring(0, 100)).append("...\n");
                } else {
                    summary.append("  Summary: ").append(newsSummary).append("\n");
                }
                summary.append("\n");
            }
        }

        return summary.toString();
    }

    private String generateMarketImpact(SentimentResult sentiment, List<NewsData> newsData) {
        StringBuilder impact = new StringBuilder();

        impact.append("Market Impact Analysis:\n\n");
        impact.append("Overall Sentiment: ").append(sentiment.getOverallSentiment()).append("\n");
        impact.append("Average Sentiment Score: ").append(String.format("%.2f", sentiment.getAverageScore()))
                .append("\n");
        impact.append("Sentiment Distribution:\n");
        impact.append("  Positive: ").append(sentiment.getPositiveCount()).append(" articles\n");
        impact.append("  Negative: ").append(sentiment.getNegativeCount()).append(" articles\n");
        impact.append("  Neutral: ").append(sentiment.getNeutralCount()).append(" articles\n\n");

        String overallSentiment = sentiment.getOverallSentiment();

        if ("BULLISH".equals(overallSentiment)) {
            impact.append("MARKET IMPACT: POSITIVE\n");
            impact.append("The predominantly positive news sentiment suggests potential upward price pressure. ");
            impact.append("Investors may view this as a buying opportunity. Monitor for increased trading volume.");
        } else if ("BEARISH".equals(overallSentiment)) {
            impact.append("MARKET IMPACT: NEGATIVE\n");
            impact.append("The predominantly negative news sentiment indicates potential downward price pressure. ");
            impact.append("Investors may consider taking profits or avoiding new positions until sentiment improves.");
        } else {
            impact.append("MARKET IMPACT: NEUTRAL\n");
            impact.append("Mixed or neutral sentiment suggests the stock may trade sideways. ");
            impact.append("Look for catalysts or technical breakouts to determine direction.");
        }

        int newsCount = newsData.size();
        if (newsCount > 15) {
            impact.append("\n\nHigh news volume (").append(newsCount)
                    .append(" articles) indicates increased market attention and potential volatility.");
        } else if (newsCount < 5) {
            impact.append("\n\nLow news volume (").append(newsCount)
                    .append(" articles) suggests limited market attention.");
        }

        return impact.toString();
    }
}
