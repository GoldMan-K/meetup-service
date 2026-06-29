package com.meetup.member.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MemberNicknameService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String nicknamePath;
    private final String internalApiKey;

    public MemberNicknameService(RestClient.Builder restClientBuilder,
                                 ObjectMapper objectMapper,
                                 @Value("${member-service.base-url:http://localhost:8081}") String memberServiceBaseUrl,
                                 @Value("${member-service.nickname-path:/internal/members/nicknames}") String nicknamePath,
                                 @Value("${internal.api-key:}") String internalApiKey) {
        this.restClient = restClientBuilder.baseUrl(memberServiceBaseUrl).build();
        this.objectMapper = objectMapper;
        this.nicknamePath = nicknamePath;
        this.internalApiKey = internalApiKey;
    }

    public Map<Long, String> resolveNicknames(Collection<Long> memberIds) {
        Set<Long> requestIds = memberIds == null
                ? Collections.emptySet()
                : memberIds.stream().filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));

        if (requestIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            Object[] idArray = requestIds.stream().map(String::valueOf).toArray();
            String body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(nicknamePath)
                            .queryParam("memberIds", idArray)
                            .queryParam("ids", idArray)
                            .build())
                    .header("X-Internal-Api-Key", internalApiKey)
                    .retrieve()
                    .body(String.class);

            return parseNicknameMap(body, requestIds);
        } catch (Exception ex) {
            log.warn("[MemberNicknameService] nickname 조회 실패. memberIds={}, reason={}", requestIds, ex.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<Long, String> parseNicknameMap(String body, Set<Long> requestIds) throws Exception {
        if (body == null || body.isBlank()) {
            return Collections.emptyMap();
        }

        JsonNode root = objectMapper.readTree(body);
        if (root.isArray()) {
            return parseArray(root, requestIds);
        }

        JsonNode members = root.path("members");
        if (members.isArray()) {
            return parseArray(members, requestIds);
        }

        JsonNode data = root.path("data");
        if (data.isArray()) {
            return parseArray(data, requestIds);
        }

        return parseSingle(root, requestIds);
    }

    private Map<Long, String> parseArray(JsonNode arrayNode, Set<Long> requestIds) {
        return toStream(arrayNode)
                .map(this::extractNicknameEntry)
                .filter(Objects::nonNull)
                .filter(e -> requestIds.contains(e.memberId()))
                .collect(Collectors.toMap(NicknameEntry::memberId, NicknameEntry::nickname, (a, b) -> a));
    }

    private Map<Long, String> parseSingle(JsonNode node, Set<Long> requestIds) {
        NicknameEntry entry = extractNicknameEntry(node);
        if (entry == null || !requestIds.contains(entry.memberId())) {
            return Collections.emptyMap();
        }
        return Map.of(entry.memberId(), entry.nickname());
    }

    private java.util.stream.Stream<JsonNode> toStream(JsonNode arrayNode) {
        return arrayNode == null ? java.util.stream.Stream.empty() :
                java.util.stream.StreamSupport.stream(arrayNode.spliterator(), false);
    }

    private NicknameEntry extractNicknameEntry(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }

        JsonNode memberIdNode = node.path("memberId");
        if (memberIdNode.isMissingNode() || memberIdNode.isNull()) {
            memberIdNode = node.path("id");
        }
        if (!memberIdNode.canConvertToLong()) {
            return null;
        }

        JsonNode nicknameNode = node.path("nickname");
        if (nicknameNode.isMissingNode() || nicknameNode.isNull()) {
            nicknameNode = node.path("nickName");
        }
        if (!nicknameNode.isTextual()) {
            return null;
        }

        return new NicknameEntry(memberIdNode.asLong(), nicknameNode.asText());
    }

    private record NicknameEntry(Long memberId, String nickname) {}
}

