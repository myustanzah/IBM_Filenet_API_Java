package com.filenet.filenet.controller;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filenet.filenet.model.ReqCreateDoc;
import com.filenet.filenet.model.ReqUpdateNameDoc;
import com.filenet.filenet.service.P8Service;

@RestController
@RequestMapping("/filenet")
public class BaseController extends Controller {

    private final P8Service p8Service;
    
    public BaseController(P8Service p8Service){
        this.p8Service = p8Service;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> sayHello(){
        p8Service.doSomething();
        return ResponseEntity.ok("Hello");
    }

    @GetMapping("/test-connect")
    public ResponseEntity<String> testP8Connection(){
        try {

            String result = p8Service.testConnectionP8();

            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e);
            return ResponseEntity.ok("Error");
        }
    }

    @GetMapping("/list-document")
    public ResponseEntity<JsonNode> listDocument(){
        try {

            ArrayList<String> listDocument = p8Service.getDocumentList();
            System.out.println(listDocument);
            return ResponseEntity.ok(getSuccessJson(200, "Success", listDocument));
        } catch (Exception e) {
            // TODO: handle exception
            return ResponseEntity.ok(getInternalServerErrorJson());
        }
    }

    @DeleteMapping("/delete/{docId}")
    public ResponseEntity<JsonNode> deleteDocument(@PathVariable String docId){
        try {
            String resultDelete = p8Service.deleteDocument(docId);

            return ResponseEntity.ok(getSuccessJson(200, "Success Delete", resultDelete));
        } catch (Exception e) {
            // TODO: handle exception
            return ResponseEntity.ok(getInternalServerErrorJson());
        }
    }

    @PostMapping("/update/{docId}")
    public ResponseEntity<JsonNode> updateNameDocument(@PathVariable String docId, @RequestBody ReqUpdateNameDoc newName){
        try {
            String resultUpdate = p8Service.updateDocument(docId, newName.new_name);
            // return getSuccessJson(0, docId, docId);
            return ResponseEntity.ok(getSuccessJson(0, resultUpdate, newName));
        } catch (Exception e) {
            // TODO: handle exception
            return ResponseEntity.ok(getInternalServerErrorJson());
        }
    }

    @PostMapping("/create")
    public ResponseEntity<JsonNode> createDocument(@RequestBody ReqCreateDoc payload){
        try {
            // MultipartFile
            String resultCreate = p8Service.createDocument(payload);
            return ResponseEntity.ok(getSuccessJson(201, resultCreate, payload));
        } catch (Exception e) {
            // TODO: handle exception
            return ResponseEntity.ok(getInternalServerErrorJson());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<JsonNode> uploadDocument(@RequestParam("data") String data, @RequestParam("file") MultipartFile file){
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(data);
            return ResponseEntity.ok(getSuccessJson(200, "Success", jsonNode));
        } catch (Exception e) {
            // TODO: handle exception
            return ResponseEntity.ok(getInternalServerErrorJson());
        }
    }
    
}
