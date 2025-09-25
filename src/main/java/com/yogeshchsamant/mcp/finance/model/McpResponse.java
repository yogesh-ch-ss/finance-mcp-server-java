package com.yogeshchsamant.mcp.finance.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class McpResponse {

    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private Object error;

    public McpResponse(String id, Object result) {
        this.id = id;
        this.result = result;
    }

}
