package com.weiheng.secondkill.kill.constants;

public interface OrderStatus {
    /*
    Invalid(-1,"无效"),
        SuccessNotPayed(0,"成功-未付款"),
        HasPayed(1,"已付款"),
        Cancel(2,"已取消")
    */
    Integer INVALID = -1; // 无效
    Integer WAIT_PAY = 0; // 已下单未付款
    Integer PAID = 1; // 已付款
    Integer CANCEL = 2; // 已取消
}
