package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前登录用户的所有地址信息
     *
     * @return
     */
    @GetMapping("/list")
    public Result<List<AddressBook>> listAddressBook() {
        return addressBookService.listAddressBook();
    }

    /**
     * 新增地址
     *
     * @param addressBook
     * @return
     */
    @PostMapping()
    public Result addAddressBook(@RequestBody AddressBook addressBook) {
        return addressBookService.addAddressBook(addressBook);
    }

    /**
     * 设置默认地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public Result setDefaultAddress(@RequestBody AddressBook addressBook) {
        return addressBookService.setDefaultAddress(addressBook);
    }

    /**
     * 获取默认地址
     * @return
     */
    @GetMapping("/default")
    public Result<AddressBook> getDefaultAddress(){
        return addressBookService.getDefaultAddress();
    }
    /**
     * 根据Id获取地址
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<AddressBook> getAddressById(@PathVariable Long id) {
        return addressBookService.getAddressById(id);
    }

    /**
     * 根据id修改地址
     *
     * @param addressBook
     * @return
     */
    @PutMapping()
    public Result updateAddress(@RequestBody AddressBook addressBook) {
        return addressBookService.updateAddress(addressBook);
    }

    /**
     * 根据Id删除地址
     * @param id
     * @return
     */
    @DeleteMapping()
    public Result deleteAddress(Long id) {
        return addressBookService.deleteAddress(id);
    }
}
