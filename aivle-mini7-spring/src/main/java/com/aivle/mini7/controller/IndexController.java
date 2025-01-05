package com.aivle.mini7.controller;

import com.aivle.mini7.client.api.FastApiClient;
import com.aivle.mini7.client.dto.HospitalResponse;
import com.aivle.mini7.service.LogService;
import com.aivle.mini7.service.GeoCodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class IndexController {

    private final FastApiClient fastApiClient;
    private final LogService logService;
    private final GeoCodingService geoCodingService;

    @GetMapping("/") // 새로운 main 페이지 매핑
    public String mainPage() {
        return "main"; // main.html 반환
    }

    @GetMapping("/recommend_hospital")
    public String recommendHospital(
            @RequestParam("request") String request,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam("number_of_hospitals") int number_of_hospitals,
            Model model) {

        // GeoCodingService를 사용하여 주소를 위도/경도로 변환
        if (latitude == null || longitude == null) {
            if (address != null && !address.isEmpty()) {
                try {
                    double[] coordinates = geoCodingService.getCoordinatesFromAddress(address);
                    latitude = coordinates[0];
                    longitude = coordinates[1];
                    log.info("주소를 위도/경도로 변환: {}, {}", latitude, longitude);
                } catch (Exception e) {
                    log.error("주소 변환 중 오류 발생: {}", e.getMessage());
                    throw new IllegalArgumentException("유효하지 않은 주소입니다.");
                }
            } else {
                throw new IllegalArgumentException("위도/경도나 주소 중 하나를 입력해야 합니다.");
            }
        }

        // FastApiClient를 호출할 때 위도와 경도만 전달
        List<HospitalResponse> hospitalList = fastApiClient.getHospital(
                request,
                latitude,
                longitude,
                null, // 주소는 null로 전달
                number_of_hospitals
        );
        log.info("FastApiClient에서 받은 병원 리스트 크기: {}", hospitalList.size());

        // 병원 리스트 제한
        List<HospitalResponse> limitedHospitals = hospitalList.stream()
                .limit(number_of_hospitals)
                .collect(Collectors.toList());

        model.addAttribute("hospitalList", limitedHospitals);

        // 로그 저장
        logService.saveLog(hospitalList, request, latitude, longitude, 2);

        return "main";
    }
}
