package com.weiheng.secondkill.kill.service;

import java.util.Map;

public interface IKillService {
    Boolean killItem(Integer killId, Integer userId) throws Exception;
    Boolean killItemV2(Integer killId, Integer userId) throws Exception;
    Boolean killItemV3(Integer killId, Integer userId) throws Exception;
    Boolean killItemV4(Integer killId, Integer userId) throws Exception;
    Boolean killItemV5(Integer killId, Integer userId) throws Exception;
    Map<String, Object> checkUserKillResult(Integer killId, Integer userId);
}
