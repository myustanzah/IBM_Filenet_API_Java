package com.filenet.filenet.component.util;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UniversalResponse {
    private static UniversalResponse instance = null;
    private ObjectMapper objectMapper = new ObjectMapper();

    public static UniversalResponse getInstance(){
        if(instance == null){
            instance = new UniversalResponse();
        }
        return instance;
    }

    public JsonNode getErrorJson(int code, String message, String description){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
		map.put("content", objectMapper.createObjectNode());
		map.put("limit", 1);
		map.put("offset", 1);
		map.put("total", 1);
		map.put("message", message);
        return objectMapper.valueToTree(map);
    }

    public JsonNode getSuccesJson(int code, String message, Object content){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
		map.put("content", content);
		map.put("limit", 1);
		map.put("offset", 1);
		map.put("total", 1);
		map.put("message", message);
        return objectMapper.valueToTree(map);
    }
}
