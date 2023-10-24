package com.filenet.filenet.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.filenet.filenet.component.util.UniversalResponse;

public class Controller {


    /**
     * 
     * @return json internal server error
     */
    protected static JsonNode getInternalServerErrorJson(){
        return UniversalResponse.getInstance().getErrorJson(500, "Internal server error", "Internal server error");
    }
    
    /**
     * 
     * @param code
     * @param msg
     * @param content
     * @return
     */
    protected static JsonNode getSuccessJson(int code, String msg, Object content){
        return UniversalResponse.getInstance().getSuccesJson(code, msg, content);
    }
}
