package com.aivle.mini7.controller;

import com.aivle.mini7.client.api.FastApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AudioController {

    private final FastApiClient fastApiClient;

    @PostMapping("/audio_to_text")
    public String audioToText(@RequestParam("audio") MultipartFile audio, Model model) throws IOException {
        try {
            // FastApiClient를 통해 API 호출
            String resultText = fastApiClient.audioToText(audio);
            model.addAttribute("resultText", resultText); // 결과를 모델에 추가
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "파일 처리 실패: " + e.getMessage()); // 오류 메시지를 모델에 추가
        }
        return "main"; // main.html로 이동
    }

}
