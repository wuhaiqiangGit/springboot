package com.whq.mvc.redis;

import com.whq.mvc.utils.RedisUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisTemplateTest {

    @Autowired
    public RedisUtils redisUtils;

    @Test
    public void testRedis() {
        Object money = redisUtils.get("money");
        System.out.printf("money: %s\n", money);

        Map<Object, Object> myHash = redisUtils.hmget("myHash");
        Set<Map.Entry<Object, Object>> entries = myHash.entrySet();
        Iterator<Map.Entry<Object, Object>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Object, Object> entry = iterator.next();
            System.out.printf("key: %s value: %s\n", entry.getKey(), entry.getValue());
        }
    }
}
