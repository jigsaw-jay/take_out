package com.sky.constant;

public class RedisConstants {
    public static final String SHOP_STATUS_KEY = "shop:status:";

    //菜品
    public static final String CACHE_DISH_KEY = "cache:dish:";
    public static final Long CACHE_DISH_TTL = 30L;

    //套餐
    public static final String CAHCE_SETMEAL_KEY = "cache:setmeal:";
    public static final Long CACHE_SETMEA_TTL = 30L;

    //购物车
    public static final String CAHCE_SHOPINGCART_KEY = "cache:shoppingcart:";
    public static final Long CACHE_SHOPINGCART_TTL = 30L;

    //地址
    public static final String CACHE_ADDRESSBOOK_KEY = "cache:addressbook:";
    public static final Long CACHE_ADDRESSBOOK_TTL = 30L;

    //口味
    public static final String CACHE_FLAVOR_KEY = "cache:flavor:";
    public static final Long CACHE_FLAVOR_TTL = 30L;

    public static final String LOCK_DISH_KEY = "lock:shop:";
    public static final Long LOCK_TTL = 10L;

}
