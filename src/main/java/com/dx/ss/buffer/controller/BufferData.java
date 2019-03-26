package com.dx.ss.buffer.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 测试用的实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BufferData implements Serializable {

    private String header;

    private String body;
}
