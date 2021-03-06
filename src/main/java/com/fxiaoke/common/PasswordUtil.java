package com.fxiaoke.common;

import lombok.experimental.UtilityClass;

/**
 * 验证token
 * Created by lirui on 2016-04-29 17:20.
 */
@UtilityClass
public final class PasswordUtil {
  private static final String key = "FS4e2%Y#X@~g.+F<";
  private static final Guard encrypt = new Guard(key);

  public String encode(String raw) throws Exception {
    return encrypt.encode(raw);
  }

  public String decode(String token) throws Exception {
    return encrypt.decode(token);
  }
}
