package com.yogeshchsamant.mcp.finance.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yogeshchsamant.mcp.finance.config.AlphaVantageConfig;
import com.yogeshchsamant.mcp.finance.model.NewsData;
import com.yogeshchsamant.mcp.finance.model.StockData;

@Service
public class AlphaVantageService {
    @Autowired
    private AlphaVantageConfig config;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public StockData getStockQuote(String symbol) {
        try {
            String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    config.getBaseUrl(), symbol, config.getApiKey());

            String response = restTemplate.getForObject(url, String.class);
            return parseStockQuote(response);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching stock quote: " + e.getMessage());
        }
    }

    public List<StockData> getHistoricalData(String symbol) {
        try {
            String url = String.format("%s?function=TIME_SERIES_DAILY&symbol=%s&apikey=%s&outputsize=compact",
                    config.getBaseUrl(), symbol, config.getApiKey());

            String response = restTemplate.getForObject(url, String.class);
            return parseHistoricalData(response, symbol);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching historical data: " + e.getMessage());
        }
    }

    public List<NewsData> getNewsData(String symbol) {
        try {
            String url = String.format("%s?function=NEWS_SENTIMENT&tickers=%s&apikey=%s&limit=20",
                    config.getBaseUrl(), symbol, config.getApiKey());

            String response = restTemplate.getForObject(url, String.class);
            return parseNewsData(response);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching news data: " + e.getMessage());
        }
    }

    private StockData parseStockQuote(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode quote = root.get("Global Quote");

            if (quote == null) {
                throw new RuntimeException("Invalid API response");
            }

            StockData stockData = new StockData();
            stockData.setSymbol(quote.get("01. symbol").asText());
            stockData.setOpen(new BigDecimal(quote.get("02. open").asText()));
            stockData.setHigh(new BigDecimal(quote.get("03. high").asText()));
            stockData.setLow(new BigDecimal(quote.get("04. low").asText()));
            stockData.setPrice(new BigDecimal(quote.get("05. price").asText()));
            stockData.setVolume(new BigDecimal(quote.get("06. volume").asText()));
            stockData.setDate(LocalDate.parse(quote.get("07. latest trading day").asText()));
            stockData.setPreviousClose(new BigDecimal(quote.get("08. previous close").asText()));
            stockData.setChange(new BigDecimal(quote.get("09. change").asText()));

            String changePercent = quote.get("10. change percent").asText().replace("%", "");
            stockData.setChangePercent(new BigDecimal(changePercent));

            return stockData;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing stock quote: " + e.getMessage());
        }
    }

    private List<StockData> parseHistoricalData(String response, String symbol) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode timeSeries = root.get("Time Series (Daily)");

            if (timeSeries == null) {
                throw new RuntimeException("Invalid historical data response");
            }

            List<StockData> historicalData = new ArrayList<>();

            timeSeries.fieldNames().forEachRemaining(date -> {
                JsonNode dayData = timeSeries.get(date);
                StockData stockData = new StockData();
                stockData.setSymbol(symbol);
                stockData.setDate(LocalDate.parse(date));
                stockData.setOpen(new BigDecimal(dayData.get("1. open").asText()));
                stockData.setHigh(new BigDecimal(dayData.get("2. high").asText()));
                stockData.setLow(new BigDecimal(dayData.get("3. low").asText()));
                stockData.setPrice(new BigDecimal(dayData.get("4. close").asText()));
                stockData.setVolume(new BigDecimal(dayData.get("5. volume").asText()));
                historicalData.add(stockData);
            });

            return historicalData;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing historical data: " + e.getMessage());
        }
    }

    private List<NewsData> parseNewsData(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode feed = root.get("feed");

            if (feed == null || !feed.isArray()) {
                throw new RuntimeException("Invalid news data response");
            }

            List<NewsData> newsDataList = new ArrayList<>();

            for (JsonNode article : feed) {
                NewsData newsData = new NewsData();
                newsData.setTitle(article.get("title").asText());
                newsData.setSummary(article.get("summary").asText());
                newsData.setUrl(article.get("url").asText());
                newsData.setSource(article.get("source").asText());

                String timePublished = article.get("time_published").asText();
                newsData.setPublishedAt(parseDateTime(timePublished));

                JsonNode tickerSentiment = article.get("ticker_sentiment");
                if (tickerSentiment != null && tickerSentiment.isArray() && tickerSentiment.size() > 0) {
                    JsonNode firstTicker = tickerSentiment.get(0);
                    newsData.setSentiment(firstTicker.get("ticker_sentiment_label").asText());
                    newsData.setSentimentScore(firstTicker.get("ticker_sentiment_score").asDouble());
                }

                newsDataList.add(newsData);
            }

            return newsDataList;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing news data: " + e.getMessage());
        }
    }

    private LocalDateTime parseDateTime(String timePublished) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            return LocalDateTime.parse(timePublished, formatter);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
