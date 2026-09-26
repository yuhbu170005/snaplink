package com.snaplink.service;

import com.snaplink.config.security.UserPrincipal;
import com.snaplink.dto.request.CreateUrlRequest;
import com.snaplink.dto.response.UrlResponse;
import com.snaplink.entity.Url;
import com.snaplink.entity.User;
import com.snaplink.exception.BadRequestException;
import com.snaplink.exception.ConflictException;
import com.snaplink.repository.UrlRepository;
import com.snaplink.repository.UserRepository;
import com.snaplink.util.Base62;
import com.snaplink.util.UrlValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Transactional
    public UrlResponse createShortUrl(CreateUrlRequest request, UserPrincipal currentUser) {
        UrlValidator.validateOriginalUrl(request.getOriginalUrl());

        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Expiration date must be in the future");
        }

        String customAlias = (request.getCustomAlias() != null && !request.getCustomAlias().trim().isEmpty())
                ? request.getCustomAlias().trim()
                : null;

        if (customAlias != null) {
            if (currentUser == null) {
                throw new BadRequestException("Custom alias is only available for registered users");
            }

            UrlValidator.validateCustomAlias(customAlias);

            if (urlRepository.existsByCustomAlias(customAlias) || urlRepository.existsByShortCode(customAlias)) {
                throw new ConflictException("Custom alias '" + customAlias + "' is already taken");
            }
        }

        // Dùng getReferenceById để tránh SELECT thừa nếu không cần đọc dữ liệu User
        User user = null;
        if (currentUser != null) {
            user = userRepository.getReferenceById(currentUser.getId());
        }

        // Tránh xung đột Unique Constraint khi nhiều request insert cùng lúc
        // Dùng UUID tạm thay cho chuỗi cố định "temp"
        String tempShortCode = (customAlias != null) ? customAlias : UUID.randomUUID().toString();

        Url url = Url.builder()
                .originalUrl(request.getOriginalUrl().trim())
                .customAlias(customAlias)
                .shortCode(tempShortCode)
                .expiresAt(request.getExpiresAt())
                .user(user)
                .isActive(true)
                .build();

        try {
            Url savedUrl = urlRepository.save(url);

            if (customAlias == null) {
                String generatedCode = Base62.encode(savedUrl.getId() + Base62.BASE_OFFSET);
                savedUrl.setShortCode(generatedCode);
                // Hibernate Dirty Checking sẽ tự update khi kết thúc method
            }

            return mapToResponse(savedUrl);
        } catch (DataIntegrityViolationException ex) {
            // Đảm bảo an toàn tuyệt đối khi xảy ra race condition tại DB
            throw new ConflictException("Alias or short code already exists, please try another one.");
        }
    }

    @Transactional(readOnly = true)
    public com.snaplink.dto.response.PageResponse<UrlResponse> getMyUrls(UserPrincipal currentUser, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Url> page = urlRepository.findByUserId(currentUser.getId(), pageable);
        return com.snaplink.dto.response.PageResponse.from(page, this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public UrlResponse getUrlById(Long id, UserPrincipal currentUser) {
        Url url = urlRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new com.snaplink.exception.ResourceNotFoundException("URL not found with id: " + id));

        return mapToResponse(url);
    }

    @Transactional
    public void deleteUrl(Long id, UserPrincipal currentUser) {
        Url url = urlRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new com.snaplink.exception.ResourceNotFoundException("URL not found with id: " + id));

        urlRepository.delete(url);
    }

    @Transactional
    public UrlResponse toggleUrlStatus(Long id, UserPrincipal currentUser) {
        Url url = urlRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new com.snaplink.exception.ResourceNotFoundException("URL not found with id: " + id));

        url.setIsActive(!Boolean.TRUE.equals(url.getIsActive()));
        Url updatedUrl = urlRepository.save(url);

        return mapToResponse(updatedUrl);
    }

    private UrlResponse mapToResponse(Url url) {
        String code = url.getShortCode();
        String fullShortUrl = (baseUrl.endsWith("/") ? baseUrl : baseUrl + "/") + code;

        return UrlResponse.builder()
                .id(url.getId())
                .shortCode(url.getShortCode())
                .shortUrl(fullShortUrl)
                .originalUrl(url.getOriginalUrl())
                .customAlias(url.getCustomAlias())
                .expiresAt(url.getExpiresAt())
                .createdAt(url.getCreatedAt())
                .isActive(url.getIsActive())
                .userId(url.getUser() != null ? url.getUser().getId() : null)
                .build();
    }
}