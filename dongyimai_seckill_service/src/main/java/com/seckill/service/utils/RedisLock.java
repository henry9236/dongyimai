package com.seckill.service.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RedisLock {

    @Autowired
    private RedisTemplate <String,String> redisTemplate;

    /**
     * 加锁,
     * 锁超时，当前时间大于redis中的时间，则锁超时
     * @param key 键
     * @param value 当前时间，超时时间
     * @return 是否获得锁
     */
    public boolean lock(String key,String value){
        //如果键不存在则新增,存在则不改变已经有的值，键不存在则返回true,存在则返回false
        if(redisTemplate.opsForValue().setIfAbsent(key,value)){
            return true;
        }
        //获取值，上一个线程传入的时间
        String currentValue = redisTemplate.opsForValue().get(key);
        //如果锁过期（老值小于当前时间），并且老值不为空
        if(!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()){
            //设置新值，返回旧值，设置当前线程传入的值
            String oldValue = redisTemplate.opsForValue().getAndSet(key,value);
            //锁是否已被别人抢占 比对currentValue 和oldValue 是否一致确保未被其他人抢占
            return !StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue);
        }
        return false;
    }


    /**
     * 解锁
     *
     * @param key   键
     * @param value 当前时间 ，超时时间
     */
    public void unlock(String key, String value) {
        try {

            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            System.out.println("redis解锁异常");
        }
    }
}
