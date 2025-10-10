package com.jdt16.agenin.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RestApiResponse<T> {

    @JsonProperty("status")
    private Integer restAPIResponseCode;

    @JsonProperty("results")
    private T restAPIResponseResults;

    @JsonProperty("message")
    private String restAPIResponseMessage;

    @JsonProperty("error")
    private RestApiResponseError restAPIResponseError;
}
