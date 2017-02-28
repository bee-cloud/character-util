package com.fxiaoke.common;

import lombok.experimental.UtilityClass;

/**
 * 关闭资源
 * Created by lirui on 2016-12-03 21:54.
 */
@UtilityClass
public final class CloseUtil {
  public void closeQuietly(AutoCloseable c) {
    if (c != null) {
      try {
        c.close();
      } catch (Exception ignored) {
      }
    }
  }
}
