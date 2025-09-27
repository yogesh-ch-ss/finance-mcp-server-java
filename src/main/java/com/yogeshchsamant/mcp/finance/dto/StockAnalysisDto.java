package com.yogeshchsamant.mcp.finance.dto;

import java.util.List;

import com.yogeshchsamant.mcp.finance.model.StockData;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StockAnalysisDto {

    private StockData currentData;
    private List<StockData> historicalData;
    private String analysis;
    private String recommendation;
    private String technicalIndicators;

    public StockAnalysisDto(StockData currentData, List<StockData> historicalData) {
        this.currentData = currentData;
        this.historicalData = historicalData;
    }
    
}
