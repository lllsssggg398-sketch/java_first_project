package com.atguigu.lease.common.exception;

import com.atguigu.lease.common.result.ResultCodeEnum;
import lombok.Data;

@Data
public class LeaseException extends RuntimeException{
    private int code;
    public LeaseException(String message,int code){
        super(message);
        this.code=code;
    }

    @Override
    public String toString() {
        return "LeaseException{" +
                "code=" + code +
                '}';
    }

    public LeaseException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code= resultCodeEnum.getCode();
    }




}
