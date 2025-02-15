package com.gaebaljip.exceed.member.application.port.in;

import lombok.Builder;

@Builder
public record UpdateWeightCommand(Double weight, Double targetWeight, Long memberId) {
    public static UpdateWeightCommand of(Double weight, Double targetWeight, Long memberId) {
        return UpdateWeightCommand.builder()
                .memberId(memberId)
                .weight(weight)
                .targetWeight(targetWeight)
                .build();
    }
}
