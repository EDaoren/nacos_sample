package com.edaoren.event.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 测试消息
 *
 * @author EDaoren
 */
@Data
public class TestMessage implements Serializable {

    private String content;

    public TestMessage() {
    }

    @Builder
    public TestMessage(String content) {
        this.content = content;
    }
}
