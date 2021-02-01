package edu.dlut.demo.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class CommonResponse implements Serializable {
    private Integer code;
    private Object data;

    public CommonResponse(Integer code, Object data) {
        this.code = code;
        this.data = data;
    }
}
