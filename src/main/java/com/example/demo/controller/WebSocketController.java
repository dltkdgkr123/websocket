package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws")
public class WebSocketController {

    @GetMapping
    public ResponseEntity<String> makeChatRoom() {

        return ResponseEntity.ok("hi");
    }

}
