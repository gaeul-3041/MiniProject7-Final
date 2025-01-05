package com.aivle.mini7.client.api;


import com.aivle.mini7.client.dto.HospitalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * FastApiClient
 * @app.get("/hospital/{request}/{latitude}/{longitude}") 를 호출한다.
 */
@FeignClient(name = "fastApiClient", url = "${hospital.api.host}")
public interface FastApiClient {

     @GetMapping("/hospital_by_module")
     public List<HospitalResponse> getHospital(@RequestParam("request") String request, @RequestParam("latitude") double latitude, @RequestParam("longitude") double longitude, @RequestParam("address") String address, @RequestParam("number_of_hospitals") int number_of_hospitals);

     @PostMapping(value = "/audio_to_text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
     String audioToText(@RequestPart("audio") MultipartFile audio);
}
