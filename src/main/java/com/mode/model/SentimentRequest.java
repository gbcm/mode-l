package com.mode.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SentimentRequest {
    private Tweet[] data;
}
