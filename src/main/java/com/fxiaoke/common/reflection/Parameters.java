package com.fxiaoke.common.reflection;

import com.github.trace.annotation.RpcParameterToString;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 处理请求参数
 * Created by lirui on 2017-02-28 14:25.
 */
@UtilityClass
@Slf4j
public class Parameters {
  private static final Set<String> NAMES = ImmutableSet.of("Boolean", "Character", "Byte", "Short", "Long", "Integer", "Byte", "Float", "Double", "Void", "String");
  private static final TimeZone CHINA_ZONE = TimeZone.getTimeZone("GMT+08:00");
  private static final Locale CHINA_LOCALE = Locale.CHINA;
  private static final Object NULL = new Object();
  private static final Map<Class, Object> PARAM_CACHES = Maps.newConcurrentMap();
  private static final int OBJECT_TO_STRING_LIMIT = 1024;
  private static final int PRIMITIVE_TO_STRING_LIMIT = 256;

  /**
   * 函数参数信息
   *
   * @param args 参数列表
   * @return 格式化输出
   */
  public String toString(Object[] args) {
    if (args == null) {
      return "";
    }
    java.lang.StringBuilder sbd = new StringBuilder();
    if (args.length > 0) {
      for (Object i : args) {
        if (i == null) {
          sbd.append("null");
        } else {
          Class<?> clz = i.getClass();
          if (isPrimitive(clz)) {
            sbd.append(evalPrimitive(i));
          } else if (clz.isArray()) {
            sbd.append("arr[").append(Array.getLength(i)).append(']');
          } else if (Collection.class.isAssignableFrom(clz)) {
            sbd.append("col[").append(((Collection<?>) i).size()).append(']');
          } else if (i instanceof Date) {
            sbd.append('"').append(formatYmdHis(((Date) i))).append('"');
          } else {
            sbd.append(getObjectToString(clz, i));
          }
        }
        sbd.append(',');
      }
      sbd.setLength(sbd.length() - 1);
    }
    return sbd.toString();
  }


  /**
   * 获取一个类的字符串标识
   *
   * @param clazz  类对象
   * @param target 目标对象
   * @return
   */
  private String getObjectToString(Class<?> clazz, Object target) {
    Object m = PARAM_CACHES.get(clazz);
    if (null == m) {
      RpcParameterToString annotation = clazz.getAnnotation(RpcParameterToString.class);
      m = annotation != null ? tryFindTargetMethod(clazz, annotation.value()) : NULL;
      PARAM_CACHES.putIfAbsent(clazz, m);
    }
    if (m != NULL) {
      try {
        String s = (String) ((Method) m).invoke(target);
        if (s.length() > OBJECT_TO_STRING_LIMIT) {
          s = s.substring(0, OBJECT_TO_STRING_LIMIT) + "...";
        }
        return s;
      } catch (Exception ignored) {
        log.error("invoke error", ignored);
      }
    }
    return clazz.getSimpleName() + ":OBJ";
  }

  private Object tryFindTargetMethod(Class<?> clazz, String name) {
    for (Class<?> clz = clazz; clz != Object.class; clz = clz.getSuperclass()) {
      try {
        Method m = clz.getDeclaredMethod(name);
        m.setAccessible(true);
        return m;
      } catch (NoSuchMethodException ignored) {
        log.error("{} cannot found method: {}", clazz, name);
      }
    }
    log.error("{} cannot found method: {}", clazz, name);
    return NULL;
  }

  private boolean isPrimitive(Class clz) {
    return clz.isPrimitive() || NAMES.contains(clz.getSimpleName());
  }

  private String evalPrimitive(Object obj) {
    String s = String.valueOf(obj);
    if (s.length() > PRIMITIVE_TO_STRING_LIMIT) {
      return s.substring(0, PRIMITIVE_TO_STRING_LIMIT) + "...";
    }
    return s;
  }

  /**
   * 构造时间的显示，带上时分秒的信息，如 2013-06-11 03:14:25
   *
   * @param date 时间
   * @return 字符串表示
   */
  private String formatYmdHis(Date date) {
    Calendar ca = Calendar.getInstance(CHINA_ZONE, CHINA_LOCALE);
    ca.setTimeInMillis(date.getTime());
    StringBuilder sbd = new StringBuilder();
    sbd.append(ca.get(Calendar.YEAR)).append('-');
    int month = 1 + ca.get(Calendar.MONTH);
    if (month < 10) {
      sbd.append('0');
    }
    sbd.append(month).append('-');
    int day = ca.get(Calendar.DAY_OF_MONTH);
    if (day < 10) {
      sbd.append('0');
    }
    sbd.append(day).append(' ');
    int hour = ca.get(Calendar.HOUR_OF_DAY);
    if (hour < 10) {
      sbd.append('0');
    }
    sbd.append(hour).append(':');
    int minute = ca.get(Calendar.MINUTE);
    if (minute < 10) {
      sbd.append('0');
    }
    sbd.append(minute).append(':');
    int second = ca.get(Calendar.SECOND);
    if (second < 10) {
      sbd.append('0');
    }
    sbd.append(second);
    return sbd.toString();
  }
}
