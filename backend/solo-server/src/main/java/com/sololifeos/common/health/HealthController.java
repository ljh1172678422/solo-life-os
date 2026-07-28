package com.sololifeos.common.health;

import com.sololifeos.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simplified liveness endpoint.
 * <p>
 * Returns a lightweight {@code ApiResponse} payload so the frontend and CI can
 * confirm the application is reachable. Spring Boot Actuator's
 * {@code /actuator/health} is still exposed separately for infrastructure
 * probes.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Solo Life OS is running");
    }

}
