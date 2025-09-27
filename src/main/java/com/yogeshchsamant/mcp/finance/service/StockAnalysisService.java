package com.yogeshchsamant.mcp.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yogeshchsamant.mcp.finance.dto.StockAnalysisDto;
import com.yogeshchsamant.mcp.finance.model.StockData;

@Service
public class StockAnalysisService {
    @Autowired
    private AlphaVantageService alphaVantageService;

    public StockAnalysisDto analyzeStock(String symbol) {
        StockData currentData = alphaVantageService.getStockQuote(symbol);
        List<StockData> historicalData = alphaVantageService.getHistoricalData(symbol);

        StockAnalysisDto analysis = new StockAnalysisDto(currentData, historicalData);
        analysis.setAnalysis(generateAnalysis(currentData, historicalData));
        analysis.setRecommendation(generateRecommendation(currentData, historicalData));
        analysis.setTechnicalIndicators(generateTechnicalIndicators(historicalData));

        return analysis;
    }

    private String generateAnalysis(StockData current, List<StockData> historical) {
        StringBuilder analysis = new StringBuilder();

        analysis.append("Stock Analysis for ").append(current.getSymbol()).append(":\n");
        analysis.append("Current Price: $").append(current.getPrice()).append("\n");
        analysis.append("Daily Change: ").append(current.getChange()).append(" (")
                .append(current.getChangePercent()).append("%)\n");
        analysis.append("Volume: ").append(current.getVolume()).append("\n");
        analysis.append("Day Range: $").append(current.getLow()).append(" - $").append(current.getHigh()).append("\n");

        if (historical.size() >= 20) {
            BigDecimal ma20 = calculateMovingAverage(historical, 20);
            analysis.append("20-day Moving Average: $").append(ma20).append("\n");

            if (current.getPrice().compareTo(ma20) > 0) {
                analysis.append("Price is above 20-day MA, indicating bullish momentum.\n");
            } else {
                analysis.append("Price is below 20-day MA, indicating bearish momentum.\n");
            }
        }

        BigDecimal volatility = calculateVolatility(historical);
        analysis.append("Historical Volatility (20-day): ").append(volatility).append("%\n");

        return analysis.toString();
    }

    private String generateRecommendation(StockData current, List<StockData> historical) {
        StringBuilder recommendation = new StringBuilder();

        int bullishSignals = 0;
        int bearishSignals = 0;

        if (historical.size() >= 20) {
            BigDecimal ma20 = calculateMovingAverage(historical, 20);
            if (current.getPrice().compareTo(ma20) > 0) {
                bullishSignals++;
            } else {
                bearishSignals++;
            }
        }

        if (current.getChangePercent().compareTo(BigDecimal.ZERO) > 0) {
            bullishSignals++;
        } else {
            bearishSignals++;
        }

        if (historical.size() >= 10) {
            BigDecimal avgVolume = calculateAverageVolume(historical, 10);
            if (current.getVolume().compareTo(avgVolume) > 0) {
                bullishSignals++;
            }
        }

        if (bullishSignals > bearishSignals) {
            recommendation.append("BUY - Bullish signals outweigh bearish indicators. ");
            recommendation.append("Stock shows positive momentum with ").append(bullishSignals)
                    .append(" bullish vs ").append(bearishSignals).append(" bearish signals.");
        } else if (bearishSignals > bullishSignals) {
            recommendation.append("SELL - Bearish signals dominate. ");
            recommendation.append("Stock shows negative momentum with ").append(bearishSignals)
                    .append(" bearish vs ").append(bullishSignals).append(" bullish signals.");
        } else {
            recommendation.append("HOLD - Mixed signals present. ");
            recommendation.append("Equal bullish and bearish indicators suggest sideways movement.");
        }

        return recommendation.toString();
    }

    private String generateTechnicalIndicators(List<StockData> historical) {
        StringBuilder indicators = new StringBuilder();

        if (historical.size() >= 14) {
            BigDecimal rsi = calculateRSI(historical, 14);
            indicators.append("RSI (14): ").append(rsi).append("\n");

            if (rsi.compareTo(new BigDecimal("70")) > 0) {
                indicators.append("RSI indicates overbought conditions.\n");
            } else if (rsi.compareTo(new BigDecimal("30")) < 0) {
                indicators.append("RSI indicates oversold conditions.\n");
            } else {
                indicators.append("RSI indicates neutral momentum.\n");
            }
        }

        if (historical.size() >= 20) {
            BigDecimal ma20 = calculateMovingAverage(historical, 20);
            indicators.append("MA20: $").append(ma20).append("\n");
        }

        if (historical.size() >= 50) {
            BigDecimal ma50 = calculateMovingAverage(historical, 50);
            indicators.append("MA50: $").append(ma50).append("\n");
        }

        return indicators.toString();
    }

    private BigDecimal calculateMovingAverage(List<StockData> historical, int periods) {
        if (historical.size() < periods) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < periods; i++) {
            sum = sum.add(historical.get(i).getPrice());
        }
        return sum.divide(new BigDecimal(periods), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVolatility(List<StockData> historical) {
        if (historical.size() < 20) {
            return BigDecimal.ZERO;
        }

        BigDecimal mean = calculateMovingAverage(historical, 20);
        BigDecimal sumSquaredDifferences = BigDecimal.ZERO;

        for (int i = 0; i < 20; i++) {
            BigDecimal difference = historical.get(i).getPrice().subtract(mean);
            sumSquaredDifferences = sumSquaredDifferences.add(difference.multiply(difference));
        }

        BigDecimal variance = sumSquaredDifferences.divide(new BigDecimal(20), 4, RoundingMode.HALF_UP);
        double volatility = Math.sqrt(variance.doubleValue()) * 100;
        return new BigDecimal(volatility).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageVolume(List<StockData> historical, int periods) {
        if (historical.size() < periods) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < periods; i++) {
            sum = sum.add(historical.get(i).getVolume());
        }
        return sum.divide(new BigDecimal(periods), 0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRSI(List<StockData> historical, int periods) {
        if (historical.size() < periods + 1) {
            return new BigDecimal("50");
        }

        BigDecimal gains = BigDecimal.ZERO;
        BigDecimal losses = BigDecimal.ZERO;

        for (int i = 0; i < periods; i++) {
            BigDecimal change = historical.get(i).getPrice().subtract(historical.get(i + 1).getPrice());
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gains = gains.add(change);
            } else {
                losses = losses.add(change.abs());
            }
        }

        BigDecimal avgGain = gains.divide(new BigDecimal(periods), 4, RoundingMode.HALF_UP);
        BigDecimal avgLoss = losses.divide(new BigDecimal(periods), 4, RoundingMode.HALF_UP);

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("100");
        }

        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        BigDecimal rsi = new BigDecimal("100").subtract(
                new BigDecimal("100").divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));

        return rsi;
    }
}
