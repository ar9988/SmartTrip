package com.travel.together.TravelTogether.tripwebsocket.controller;

import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
//    @GetMapping("/chat")
//    public String chat() {
//        return "chat.html";  // static 폴더의 chat.html을 가리킴
//    }
//
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    @Scheduled(fixedRate = 3000)  // 3초마다 실행
//    public void sendMessage() {
//        ChatMessage message = new ChatMessage();
//        message.setSender("Server");
//        message.setContent("Test message: " + new Date());
//
//        // /topic/messages로 메시지 전송
//        messagingTemplate.convertAndSend("/topic/messages", message);
//    }
//
//    @MessageMapping("/send")  // 클라이언트가 /app/chat으로 메시지를 보낼 때
//    @SendTo("/topic/messages")  // 구독자들에게 메시지 전달
//    public ChatMessage sendMessage(ChatMessage message) {
//        return message;
//    }
}