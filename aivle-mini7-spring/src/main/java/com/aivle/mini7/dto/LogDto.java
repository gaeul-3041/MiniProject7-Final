package com.aivle.mini7.dto;

import com.aivle.mini7.model.Log;
import lombok.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogDto {




    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResponseList {
        //        Log의 항목들을 적어야함
        private String inputText;
        private String datetime;
        private Double inputLatitude;
        private Double inputLongitude;
        private String address;
        private Integer emClass;
        private String hospital1;
        private String addr1;
        private String tel1;
        private String hospital1ArrivalTime;
        private String hospital1Duration;
        private String hospital2;
        private String addr2;
        private String tel2;
        private String hospital2ArrivalTime;
        private String hospital2Duration;
        private String hospital3;
        private String addr3;
        private String tel3;
        private String hospital3ArrivalTime;
        private String hospital3Duration;


        public static ResponseList of(Log log) {
            return ResponseList.builder()
                    .inputText(log.getInputText())
                    .datetime(log.getDatetime())
                    .inputLatitude(log.getInputLatitude())
                    .inputLongitude(log.getInputLongitude())
                    .emClass(log.getEmClass())
                    .hospital1(log.getHospital1())
                    .inputText(log.getInputText())
                    .datetime(log.getDatetime())
                    .inputLatitude(log.getInputLatitude())
                    .inputLongitude(log.getInputLongitude())
                    .emClass(log.getEmClass())
                    .hospital1(log.getHospital1())
                    .addr1(log.getAddr1())
                    .tel1(log.getTel1())
                    .hospital1ArrivalTime(log.getHospital1ArrivalTime())
                    .hospital1Duration(log.getHospital1Duration())
                    .hospital2(log.getHospital2())
                    .addr2(log.getAddr2())
                    .tel2(log.getTel2())
                    .hospital2ArrivalTime(log.getHospital2ArrivalTime())
                    .hospital2Duration(log.getHospital2Duration())
                    .hospital3(log.getHospital3())
                    .addr3(log.getAddr3())
                    .tel3(log.getTel3())
                    .hospital3ArrivalTime(log.getHospital3ArrivalTime())
                    .hospital3Duration(log.getHospital3Duration())
                    .build();
        }
    }
}
