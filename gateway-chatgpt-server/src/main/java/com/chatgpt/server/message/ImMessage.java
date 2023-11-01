package com.chatgpt.server.message;

import com.chatgpt.server.enums.MsgType;
import lombok.*;

import javax.validation.constraints.NotEmpty;

/**
 * ImMessage
 * Reference: https://doc.rongcloud.cn/imserver/server/v1/message/sync
 *
 * @author chenx
 */
@ToString
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImMessage {

    @NotEmpty(message = "fromUserId mustn't be empty or null!")
    private String fromUserId;

    @NotEmpty(message = "toUserId mustn't be empty or null!")
    private String toUserId;

    @NotEmpty(message = "objectName mustn't be empty or null!")
    private String objectName;

    @NotEmpty(message = "content mustn't be empty or null!")
    private String content;

    private String channelType;

    private Long msgTimestamp;

    private String msgUID;

    private String[] groupUserIds;
    private MsgType msgType;
}
