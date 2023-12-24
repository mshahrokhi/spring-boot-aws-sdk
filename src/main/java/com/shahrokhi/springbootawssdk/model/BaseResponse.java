package com.shahrokhi.springbootawssdk.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponse {
    private int status;
    private String message;
    private String messageFa;
    private String stack;

    public void commonSetSuccess() {
        this.setStatus(200);
        this.setMessage("The request has done successfully");
        this.setMessageFa("درخواست با موفقیت انجام شد");
    }

    public void commonSetInternalError(String message, String messageFa) {
        this.setStatus(500);
        this.setMessage(message);
        this.setMessageFa(messageFa);
        this.setStack("");
    }

    public void commonSetNotFound(String message, String messageFa) {
        this.setStatus(404);
        this.setMessage(message);
        this.setMessageFa(messageFa);
    }

}

