package com.itheima.service;

import com.itheima.domain.Account;

import java.util.List;

/**
 * 账户的业务层接口
 */
public interface IAccountService {

    public void transfer(String sourceName, String targetName, Float money);

    Account findAccountById(Integer accountId);
}
