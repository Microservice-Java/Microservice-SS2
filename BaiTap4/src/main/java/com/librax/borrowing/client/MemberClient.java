package com.librax.borrowing.client;

import com.librax.borrowing.dto.MemberResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class MemberClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public MemberResponse getMemberById(Long memberId) {
        String url = "http://localhost:8082/api/members/" + memberId;
        try {
            log.info("Calling member-service REST API for memberId: {}", memberId);
            return restTemplate.getForObject(url, MemberResponse.class);
        } catch (Exception e) {
            log.warn("Failed to fetch member from member-service (memberId: {}). Fallback applied. Error: {}", memberId, e.getMessage());
            return MemberResponse.builder()
                    .id(memberId)
                    .fullName("[Tạm thời không khả dụng - Fallback Name]")
                    .email("unknown@librax.com")
                    .build();
        }
    }
}
