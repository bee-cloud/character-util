package com.fxiaoke.common;

import org.junit.Test;

import java.net.InetAddress;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 测试获取本地IP
 * Created by lirui on 2016-08-31 13:25.
 */
public class IpUtilTest {

  @Test
  public void testGetLocalAddress() throws Exception {
    String ip = IpUtil.getSiteLocalIp();
    assertFalse("127.0.0.1".equals(ip));
    assertTrue(InetAddress.getByName(ip).isSiteLocalAddress());
  }
}
