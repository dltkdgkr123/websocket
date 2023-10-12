package com.example.demo.config;

import com.example.demo.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketChatHandler extends TextWebSocketHandler {

    private final ObjectMapper mapper;

    // 채팅방 세션 -> 해당 채팅방에 연결된 유저 세션의 집합
    private final Set<WebSocketSession> sessions = new HashSet<>();

    // 전체 채팅방 목록 -> 채팅방 세션의 집합
    private final Map<Long, Set<WebSocketSession>> chatRoomSessionMap = new HashMap<>();


    // 연결 성공 이후 동작
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        super.afterConnectionEstablished(session);
        log.info("{} Established", session.getId());
    }

    // 연결 해제 이후 동작
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//        super.afterConnectionClosed(session, status);
        log.info("{} Closed. Reason : {}", session.getId(), status.getReason());

        log.info("==============================");
        sessions.forEach(webSocketSession -> log.info(webSocketSession.toString()));
        log.info(session.toString());
        log.info("==============================");

        // 채팅방 세션에서 해당 유저 세션 해제
        if(sessions.remove(session)){
            log.info("{} session removed", session.getId());
        }
        else {
            log.info("{} session remove failed", session.getId());
        }
    }

    // 메세지 핸들링 동작
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        super.handleTextMessage(session, message);
        String payload = message.getPayload();
        log.info("payload : {}", payload);

        // ObjectMapper가 JSON을 직렬화하여 DTO로 변환한다. 이를 위한 String to JSON은 JAVA에서 자동으로 해주었다.
        ChatMessageDto chatMessageDto = mapper.readValue(payload, ChatMessageDto.class);

        Long chatRoomId = chatMessageDto.getChatRoomId();


        Set<WebSocketSession> chatRoomSession = chatRoomSessionMap.get(chatRoomId);

        // User가 ENTER한다면, 현재 채팅방 세션에 유저 세션을 추가한다.
        if(chatMessageDto.getMessageType().equals(ChatMessageDto.MessageType.ENTER)) {
            chatRoomSession.add(session);
            log.info("{} entered", chatMessageDto.getSenderId());
        }

        // User가 3명 이상이라면 removeClosedSession 호출
//        if (chatRoomSession.size()>=3) {
//            removeClosedSession(chatRoomSession);
//        }
        sendMessageToChatRoom(chatMessageDto, chatRoomSession);
    }

    // 자체 정의 세션 Close 메서드 -> 채팅방 세션을 순회하며 끊긴 유저 세션 삭제
//    private void removeClosedSession(Set<WebSocketSession> chatRoomSession) {
//        chatRoomSession.removeIf(sess -> !sessions.contains(sess));
//    }

    // 채팅방 세션을 순회하며 유저 세션마다 sendMessage 호출
    private void sendMessageToChatRoom(ChatMessageDto chatMessageDto, Set<WebSocketSession> chatRoomSession) {
        chatRoomSession.parallelStream().forEach(sess -> sendMessage(sess, chatMessageDto));
    }

    // T타입을 String으로 변환하여 해당 WebSocketSession에 TextMessage 전송
    public <T> void sendMessage(WebSocketSession session, T message) {
        try{
            log.info("sendMessage : {}", message.toString());
            // 해당 메서드는 내가 정의한 것이 아닌 WebSocketSession 클래스의 sendMessage임에 주의
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
