package com.ma.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;

@ApiModel
@Data
public class UpdateUnreadMessageForm {

    @NotNull
    private String id;
}
