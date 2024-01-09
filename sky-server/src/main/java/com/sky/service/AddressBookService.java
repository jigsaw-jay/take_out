package com.sky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sky.entity.AddressBook;
import com.sky.result.Result;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {
    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    Result<List<AddressBook>> listAddressBook();

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    Result addAddressBook(AddressBook addressBook);

    /**
     *
     * @param addressBook
     * @return
     */
    Result setDefaultAddress(AddressBook addressBook);

    /**
     * 根据Id获取地址
     * @param id
     * @return
     */
    Result<AddressBook> getAddressById(Long id);

    /**
     * 根据id修改地址
     * @param addressBook
     * @return
     */
    Result updateAddress(AddressBook addressBook);

    /**
     * 根据Id删除地址
     * @param id
     * @return
     */
    Result deleteAddress(Long id);

    /**
     * 获取默认地址
     * @return
     */
    Result<AddressBook> getDefaultAddress();
}
