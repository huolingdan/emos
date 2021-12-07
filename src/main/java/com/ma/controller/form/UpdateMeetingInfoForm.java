package com.ma.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel
@Data
public class UpdateMeetingInfoForm {
    private String title;

    private String date;

    private String place;

    private String start;

    private String end;

    private Byte type;

    private String members;

    private String desc;

    private Integer id;

    private String instanceId;
}
