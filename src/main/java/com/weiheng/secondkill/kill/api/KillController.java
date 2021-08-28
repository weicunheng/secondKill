package com.weiheng.secondkill.kill.api;

import com.weiheng.secondkill.enums.StatusCode;
import com.weiheng.secondkill.kill.dto.KillInfoDto;
import com.weiheng.secondkill.kill.model.request.KillParams;
import com.weiheng.secondkill.kill.service.IKillService;
import com.weiheng.secondkill.response.BaseResponse;
import com.weiheng.secondkill.service.RabbitPublisherService;
import com.weiheng.secondkill.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/kill")
public class KillController {

    @Autowired
    private IKillService IKillService;

    @Autowired
    private RabbitPublisherService publisherService;

    @RequestMapping(value = "/execute/", method = RequestMethod.POST)
    public BaseResponse<String> killExecute(@RequestBody(required = false) @Valid KillParams killParams,
                                            BindingResult result, HttpServletRequest request) {
        BaseResponse<String> response;
        /*
         * 1. 获取用户和商品id
         * 2. 判断用户是否已抢购，如果已抢购 ->秒杀失败
         * 3. 判断商品是否有库存，如果无库存 -> 秒杀失败
         * 4. 扣减库存
         * 5. 更新商品库存
         * 6. 库存是否更新成功，如果更新失败 -> 秒杀失败
         * 7. 创建秒杀单
         * 8. 异步通知秒杀成功
         * */

        Integer userId = UserUtils.getCurrentUser().getId();
        if (userId == null) {
            response = new BaseResponse<String>(StatusCode.FAIL);
            response.setData("请登录!");
            return response;
        }

        if (result.hasErrors()) {
            response = new BaseResponse<>(StatusCode.FAIL);
            FieldError fieldError = result.getFieldError();
            assert fieldError != null;
            response.setMsg(fieldError.getDefaultMessage());
            return response;
        }

        // 执行秒杀业务
        try {
            Boolean isSuccess = IKillService.killItem(killParams.getKillId(), userId);
            if (!isSuccess) {
                return new BaseResponse<String>(StatusCode.FAIL, "库存不足，秒杀失败！");
            }
        } catch (Exception e) {
            System.out.println("执行秒杀异常，异常信息:" + e.getMessage());
            return new BaseResponse<String>(StatusCode.FAIL, e.getMessage());
        }

        return new BaseResponse<>(StatusCode.SUCCESS);
    }


    @RequestMapping(value = "async/execute/", method = RequestMethod.POST)
    public BaseResponse<String> asyncKillExecute(@RequestBody(required = false) @Valid KillParams killParams,
                                                 BindingResult result, HttpServletRequest request) {
        /*
         * 不直接处理秒杀业务逻辑， 而是直接返回
         * */
        BaseResponse<String> response = new BaseResponse<String>(StatusCode.SUCCESS);

        Integer userId = UserUtils.getCurrentUser().getId();
        if (userId == null) {
            response = new BaseResponse<String>(StatusCode.UserNotLogin.getCode(), "请先登录");
        }

        if (result.hasErrors()) {
            response = new BaseResponse<>(StatusCode.FAIL);
            FieldError fieldError = result.getFieldError();
            assert fieldError != null;
            response.setMsg(fieldError.getDefaultMessage());
        }
        // 将请求信息法治MQ
        KillInfoDto killInfoDto = new KillInfoDto();
        killInfoDto.setKillItemId(killParams.getKillId());
        killInfoDto.setUserId(userId);
        try {
            publisherService.sendKillExecuteMqMsg(killInfoDto);
        } catch (Exception e) {
            response = new BaseResponse<String>(StatusCode.MqPublishError.getCode(), e.getMessage());
        }
        return response;
    }

    @RequestMapping(value = "/check/order/", method = RequestMethod.GET)
    public BaseResponse checkOrderStatuc(@RequestParam(required = true) @Valid Integer killId,
                                         BindingResult result,
                                         HttpServletRequest request) {
        BaseResponse response = new BaseResponse(StatusCode.SUCCESS);

        Integer userId = UserUtils.getCurrentUser().getId();

        try {
            Map<String, Object> resMap = IKillService.checkUserKillResult(killId, userId);
            response.setData(resMap);
        } catch (Exception e) {
            response = new BaseResponse(StatusCode.FAIL.getCode(), e.getMessage());
        }
        return response;
    }
}
