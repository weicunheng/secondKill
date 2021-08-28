package com.weiheng.secondkill.kill.model.request;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;

@Data
@ToString
public class KillParams implements Serializable {
    @NotNull(message = "商品id必传")
    @Min(1)
    private Integer killId;
}
