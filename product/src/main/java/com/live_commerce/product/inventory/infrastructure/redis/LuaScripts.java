package com.live_commerce.product.inventory.infrastructure.redis;

public class LuaScripts {
    public static final String STOCK_DECREASE_SCRIPT =
        "local stock = redis.call('get', KEYS[1]) " +
        "if not stock then return -1 end " +
        "if tonumber(stock) < tonumber(ARGV[1]) then return -2 end " +
        "return redis.call('decrby', KEYS[1], ARGV[1])";
}
