package com.crochess.backend.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class WsMessage<T> {
    public String event;
    public T payload;
}
