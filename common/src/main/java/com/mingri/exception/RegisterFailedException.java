package com.mingri.exception;

/**
 * 注册失败
 */
public class RegisterFailedException extends BaseException{
    public RegisterFailedException(String msg){
        super(msg);
    }
}
