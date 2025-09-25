package com.yogeshchsamant.mcp.finance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class McpRequest {

    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private Object params;

    public McpRequest(String id, String method, Object params) {
        this.id = id;
        this.method = method;
        this.params = params;
    }
    
}
