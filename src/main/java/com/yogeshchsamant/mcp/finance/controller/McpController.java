package com.yogeshchsamant.mcp.finance.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yogeshchsamant.mcp.finance.dto.NewsAnalysisDto;
import com.yogeshchsamant.mcp.finance.dto.StockAnalysisDto;
import com.yogeshchsamant.mcp.finance.model.McpRequest;
import com.yogeshchsamant.mcp.finance.model.McpResponse;
import com.yogeshchsamant.mcp.finance.service.SentimentAnalysisService;
import com.yogeshchsamant.mcp.finance.service.StockAnalysisService;

@RestController
@RequestMapping("/mcp")
@CrossOrigin(origins = "*")
public class McpController {

    @Autowired
    private StockAnalysisService stockAnalysisService;

    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    @PostMapping("/tools/call")
    public ResponseEntity<McpResponse> handleToolCall(@RequestBody McpRequest request) {
        String method = request.getMethod();

        if ("tools/call".equals(method)) {
            Map<String, Object> params = (Map<String, Object>) request.getParams();
            String toolName = (String) params.get("name");
            Map<String, Object> arguments = (Map<String, Object>) params.get("arguments");

            if ("analyze_stock".equals(toolName)) {
                return handleStockAnalysis(request.getId(), arguments);
            } else if ("analyze_stock_sentiment".equals(toolName)) {
                return handleSentimentAnalysis(request.getId(), arguments);
            } else {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "Unknown tool: " + toolName);
                return ResponseEntity.ok(new McpResponse(request.getId(), errorResult));
            }
        }

        Map<String, Object> errorResult = new HashMap<>();
        errorResult.put("error", "Unknown method: " + method);
        return ResponseEntity.ok(new McpResponse(request.getId(), errorResult));
    }

    @GetMapping("/tools/list")
    public ResponseEntity<McpResponse> listTools() {
        Map<String, Object> tool1 = new HashMap<>();
        tool1.put("name", "analyze_stock");
        tool1.put("description", "Analyze stock performance with technical indicators and recommendations");

        Map<String, Object> symbolProperty = new HashMap<>();
        symbolProperty.put("type", "string");
        symbolProperty.put("description", "Stock symbol (e.g., AAPL, GOOGL)");

        Map<String, Object> properties = new HashMap<>();
        properties.put("symbol", symbolProperty);

        Map<String, Object> inputSchema1 = new HashMap<>();
        inputSchema1.put("type", "object");
        inputSchema1.put("properties", properties);
        inputSchema1.put("required", new String[] { "symbol" });

        tool1.put("inputSchema", inputSchema1);

        Map<String, Object> tool2 = new HashMap<>();
        tool2.put("name", "analyze_stock_sentiment");
        tool2.put("description", "Analyze recent news sentiment for a stock");
        tool2.put("inputSchema", inputSchema1);

        Map<String, Object> result = new HashMap<>();
        result.put("tools", new Object[] { tool1, tool2 });

        return ResponseEntity.ok(new McpResponse("list-tools", result));
    }

    private ResponseEntity<McpResponse> handleStockAnalysis(String requestId, Map<String, Object> arguments) {
        try {
            String symbol = (String) arguments.get("symbol");

            if (symbol == null || symbol.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "Symbol parameter is required");
                return ResponseEntity.ok(new McpResponse(requestId, errorResult));
            }

            StockAnalysisDto analysis = stockAnalysisService.analyzeStock(symbol.toUpperCase());

            Map<String, Object> result = new HashMap<>();
            result.put("content", formatStockAnalysis(analysis));
            result.put("isError", false);

            return ResponseEntity.ok(new McpResponse(requestId, result));
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to analyze stock: " + e.getMessage());
            errorResult.put("isError", true);

            return ResponseEntity.ok(new McpResponse(requestId, errorResult));
        }
    }

    private ResponseEntity<McpResponse> handleSentimentAnalysis(String requestId, Map<String, Object> arguments) {
        try {
            String symbol = (String) arguments.get("symbol");

            if (symbol == null || symbol.trim().isEmpty()) {
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("error", "Symbol parameter is required");
                return ResponseEntity.ok(new McpResponse(requestId, errorResult));
            }

            NewsAnalysisDto analysis = sentimentAnalysisService.analyzeStockSentiment(symbol.toUpperCase());

            Map<String, Object> result = new HashMap<>();
            result.put("content", formatSentimentAnalysis(analysis));
            result.put("isError", false);

            return ResponseEntity.ok(new McpResponse(requestId, result));
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Failed to analyze sentiment: " + e.getMessage());
            errorResult.put("isError", true);

            return ResponseEntity.ok(new McpResponse(requestId, errorResult));
        }
    }

    private String formatStockAnalysis(StockAnalysisDto analysis) {
        StringBuilder result = new StringBuilder();

        result.append("STOCK ANALYSIS REPORT\n");
        result.append("====================\n\n");

        result.append(analysis.getAnalysis()).append("\n");
        result.append("TECHNICAL INDICATORS:\n");
        result.append(analysis.getTechnicalIndicators()).append("\n");
        result.append("RECOMMENDATION:\n");
        result.append(analysis.getRecommendation()).append("\n");

        return result.toString();
    }

    private String formatSentimentAnalysis(NewsAnalysisDto analysis) {
        StringBuilder result = new StringBuilder();

        result.append("SENTIMENT ANALYSIS REPORT\n");
        result.append("========================\n\n");

        result.append(analysis.getNewsSummary()).append("\n");
        result.append(analysis.getMarketImpact()).append("\n");

        return result.toString();
    }
}
