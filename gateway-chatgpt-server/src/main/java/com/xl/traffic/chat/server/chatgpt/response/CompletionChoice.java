package com.xl.traffic.chat.server.chatgpt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * CompletionChoice: ignore logprobs, finish_reason definition
 *
 * @author chenx
 */
@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompletionChoice {

    @JsonProperty("text")
    private String text;

    @JsonProperty("index")
    private int index;
}
