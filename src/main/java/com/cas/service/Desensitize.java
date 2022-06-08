package com.cas.service;

/**
 * @author xiang_long
 * @version 1.0
 * @date 2022/6/8 11:04 上午
 * @desc
 */
public interface Desensitize {

    /**
     * 加密数据
     * @param data
     * @return
     */
    String encryptData(String data);

    /**
     * 解密数据
     * @param data
     * @return
     */
    String decryptData(String data);

}
