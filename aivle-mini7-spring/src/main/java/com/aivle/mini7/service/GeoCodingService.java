package com.aivle.mini7.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;

@Service
public class GeoCodingService {

    private final String FASTAPI_BASE_URL = "http://localhost:8000"; // FastAPI 서버 주소

    public double[] getCoordinatesFromAddress(String address) {
        RestTemplate restTemplate = new RestTemplate();

        // URI 생성 시 인코딩 처리
        URI uri = UriComponentsBuilder.fromHttpUrl(FASTAPI_BASE_URL + "/get_coordinates")
                .queryParam("address", address) // 자동 인코딩
                .build()
                .encode() // 여기서 인코딩 보장
                .toUri();

        System.out.println("요청 URI: " + uri); // 디버깅 로그

        try {
            // 요청 보내기
            ResponseEntity<CoordinatesResponse> response = restTemplate.getForEntity(uri, CoordinatesResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                CoordinatesResponse body = response.getBody();

                if (body.getLatitude() != 0 && body.getLongitude() != 0) {
                    return new double[]{body.getLatitude(), body.getLongitude()};
                } else {
                    throw new IllegalArgumentException("주소를 찾을 수 없습니다.");
                }
            } else {
                throw new IllegalArgumentException("FastAPI 응답 오류: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("FastAPI 호출 실패: " + e.getMessage());
        }
    }

    // 내부 클래스 또는 별도 DTO 파일로 작성 가능
    public static class CoordinatesResponse {
        private double latitude;
        private double longitude;

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }
}

