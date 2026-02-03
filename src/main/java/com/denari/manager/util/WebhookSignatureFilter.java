//package com.denari.manager.util;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//@Component
//public class WebhookSignatureFilter extends OncePerRequestFilter {
//    private static final String HMAC_SHA256 = "HmacSHA256";
//    private static final Logger log = LoggerFactory.getLogger(WebhookSignatureFilter.class);
//
//    private final String webhookSecret;
//
//    public WebhookSignatureFilter(@Value("${moderntreasury.webhookSecret}") String webhookSecret) {
//        this.webhookSecret = webhookSecret;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        // Only apply filter to the webhook endpoint
//        if (request.getRequestURI().equals("/api/webhook") && request.getMethod().equals("POST")) {
//
//            String signature = request.getHeader("X-Signature");
//
//            if (signature == null) {
//                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                response.getWriter().write("{\"success\": false, \"message\": \"Missing signature\"}");
//                return;
//            }
//
//            // Use a CachedBodyHttpServletRequest to allow reading the body multiple times
//            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
//            byte[] body = cachedRequest.getContentAsByteArray();
//
//            try {
//                String rawBody = new String(body, StandardCharsets.UTF_8);
//
//                Mac mac = Mac.getInstance(HMAC_SHA256);
//                SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
//                mac.init(secretKeySpec);
//                byte[] hmacBytes = mac.doFinal(body);
//
//                StringBuilder sb = new StringBuilder();
//                for (byte b : hmacBytes) {
//                    sb.append(String.format("%02x", b));
//                }
//                String computedSignature = sb.toString();
//
//                log.info("Received signature: {}", signature);
//                log.info("Computed signature: {}", computedSignature);
//
//                if (!computedSignature.equals(signature)) {
//                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                    response.getWriter().write("{\"success\": false, \"message\": \"Invalid signature\"}");
//                    return;
//                }
//
//                // Signature is valid, continue
//                filterChain.doFilter(cachedRequest, response);
//            } catch (Exception e) {
//                log.error("Error validating signature", e);
//                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//                response.getWriter().write("{\"success\": false, \"message\": \"Error validating signature\"}");
//            }
//        } else {
//            // Not our endpoint, continue
//            filterChain.doFilter(request, response);
//        }
//    }
//}
