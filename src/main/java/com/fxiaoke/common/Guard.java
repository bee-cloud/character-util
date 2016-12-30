package com.fxiaoke.common;

import com.fxiaoke.common.crypto.HexUtil;
import com.fxiaoke.common.crypto.TEA;
import com.google.common.base.Charsets;

/**
 * 验证token
 * Created by lirui on 2016-04-29 17:20.
 */
public final class Guard {
  private final String key;

  public Guard(String key) {
    this.key = key;
  }

  /**
   * 多次加密得到不同的串
   *
   * @param raw
   * @return
   * @throws Exception
   */
  public String encode(String raw) throws Exception {
    return new TEA(key.getBytes(Charsets.UTF_8)).encode2HexStr(raw.getBytes(Charsets.UTF_8));
  }

  public String decode(String token) throws Exception {
    return new String(new TEA(key.getBytes(Charsets.UTF_8)).decodeFromHexStr(token), Charsets.UTF_8);
  }

  /**
   * 多次加密得到同样的串
   *
   * @param raw
   * @return
   * @throws Exception
   */
  public String encode2(String raw) throws Exception {
    return HexUtil.bytes2HexStr(new TEA(key.getBytes(Charsets.UTF_8)).encode2(raw.getBytes(Charsets.UTF_8)));
  }
}
