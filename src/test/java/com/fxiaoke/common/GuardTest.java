package com.fxiaoke.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * 测试加解密
 * Created by lirui on 2016-12-30 18:20.
 */
public class GuardTest {
  @Test
  public void testEncode() throws Exception {
    String str = "123456";
    String key = "FS4e2%Y#X@~g.+F<";
    Guard guard = new Guard(key);
    // 加解密功能
    assertEquals(str, guard.decode(guard.encode(str)));
    assertEquals(str, guard.decode(guard.encode2(str)));

    // 每次加密串都不同
    assertNotEquals(guard.encode(str), guard.encode(str));
    // 每次加密串都相同
    assertEquals(guard.encode2(str), guard.encode2(str));

  }
}
