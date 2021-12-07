package com.ma.controller.form;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Objects;

@Data
@ApiModel
public class CheckinForm {

    private String address;
    private String country;
    private String province;
    private String city;
    private String district;



}
