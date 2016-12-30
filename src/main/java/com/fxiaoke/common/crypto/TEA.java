package com.fxiaoke.common.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * TEA对称加密算法
 *
 * @author colinli
 * @since 13-8-9 下午3:39
 */
public class TEA {
  private static final Logger LOG = LoggerFactory.getLogger(TEA.class);
  private static Random random = new Random();
  private byte[] plain;
  private byte[] prePlain;
  private byte[] out;
  private int crypt;
  private int preCrypt;
  private int pos;
  private int padding;
  private byte[] key;
  private boolean header;
  private int contextStart;

  /**
   * 构造函数
   *
   * @param k 密钥,密钥长度必须为16
   */
  public TEA(byte[] k) {
    if (k == null || k.length != 16)
      throw new IllegalArgumentException("Key length must be 16!");
    header = true;
    key = k;
  }

  public static long getUnsignedInt(byte[] in, int offset, int len) {
    long ret = 0L;
    int end;
    if (len > 8) {
      end = offset + 8;
    } else {
      end = offset + len;
    }
    for (int i = offset; i < end; i++) {
      ret <<= 8;
      ret |= in[i] & 0xff;
    }

    return ret & 0xffffffffL | ret >>> 32;
  }

  public static void main(String[] args) {
    String str = "123456";
    String key = "FS4e2%Y#X@~g.+F<";
    TEA tea = new TEA(key.getBytes());
    String code = tea.encode2HexStr(str.getBytes());
    LOG.info("code : {}", code);
    LOG.info("{}", new String(tea.decodeFromHexStr(code)));
  }

  private static int rand() {
    return random.nextInt();
  }

  /**
   * 随机加密, 某些字节会随机算一些值，这样每次加密，得到的都是不同的字符串
   *
   * @param in 需要进行加密的字节数组
   * @return byte[] 加密后的字节数组
   */
  public byte[] encode(byte[] in) {
    return encode(in, 0, in.length);
  }

  /**
   * 固定加密, 多次加密得到的都是同样的字符串
   *
   * @param in 需要进行加密的字节数组
   * @return byte[] 加密后的字节数组
   */
  public byte[] encode2(byte[] in) {
    return encode2(in, 0, in.length);
  }

  /**
   * 加密并转为16进制字符串
   *
   * @param in 需要进行加密的字节数组
   * @return String 加密后的16进制字符串
   */
  public String encode2HexStr(byte[] in) {
    return HexUtil.bytes2HexStr(encode(in));
  }

  private byte[] encode(byte[] in, int offset, int len) {
    plain = new byte[8];
    prePlain = new byte[8];
    pos = 1;
    padding = 0;
    crypt = preCrypt = 0;
    header = true;
    pos = (len + 10) % 8;
    if (pos != 0) {
      pos = 8 - pos;
    }
    out = new byte[len + pos + 10];
    plain[0] = (byte) (rand() & 0xf8 | pos);
    int i;
    for (i = 1; i <= pos; i++) {
      plain[i] = (byte) (rand() & 0xff);
    }

    pos++;
    for (i = 0; i < 8; i++) {
      prePlain[i] = 0;
    }

    for (padding = 1; padding <= 2; ) {
      if (pos < 8) {
        plain[pos++] = (byte) (rand() & 0xff);
        padding++;
      }
      if (pos == 8) {
        encrypt8Bytes();
      }
    }

    i = offset;
    int length = len;
    while (length > 0) {
      if (pos < 8) {
        plain[pos++] = in[i++];
        length--;
      }
      if (pos == 8) {
        encrypt8Bytes();
      }
    }
    for (padding = 1; padding <= 7; ) {
      if (pos < 8) {
        plain[pos++] = 0;
        padding++;
      }
      if (pos == 8) {
        encrypt8Bytes();
      }
    }

    return out;
  }

  /* 避免每次编码的内容都不同，去掉rand的调用 */
  private byte[] encode2(byte[] in, int offset, int len) {
    plain = new byte[8];
    prePlain = new byte[8];
    pos = 1;
    padding = 0;
    crypt = preCrypt = 0;
    header = true;
    pos = (len + 10) % 8;
    if (pos != 0) {
      pos = 8 - pos;
    }
    out = new byte[len + pos + 10];
    plain[0] = (byte) (0x17 & 0xf8 | pos);
    int i;
    for (i = 1; i <= pos; i++) {
      plain[i] = (byte) (0x32 & 0xff);
    }

    pos++;
    for (i = 0; i < 8; i++) {
      prePlain[i] = 0;
    }

    for (padding = 1; padding <= 2; ) {
      if (pos < 8) {
        plain[pos++] = (byte) (0xA5 & 0xff);
        padding++;
      }
      if (pos == 8) {
        encrypt8Bytes();
      }
    }

    i = offset;
    int length = len;
    while (length > 0) {
      if (pos < 8) {
        plain[pos++] = in[i++];
        length--;
      }
      if (pos == 8) {
        encrypt8Bytes();
      }
    }
    for (padding = 1; padding <= 7; ) {
      if (pos < 8) {
        plain[pos++] = 0;
        padding++;
      }
      if (pos == 8) {
        encrypt8Bytes();
      }
    }

    return out;
  }

  /**
   * 解密
   *
   * @param code 加密后的字节数组
   * @return byte[] 解密后的字节数组
   */
  public byte[] decode(byte[] code) {
    return decode(code, 0, code.length);
  }

  /**
   * 解密
   *
   * @param code 加密后的hexStr
   * @return byte[] 解密后的字节数组
   */
  public byte[] decodeFromHexStr(String code) {
    return decode(HexUtil.hexStr2Bytes(code));
  }

  private byte[] decode(byte[] in, int offset, int len) {
    crypt = preCrypt = 0;
    byte[] m = new byte[offset + 8];
    if (len % 8 != 0 || len < 16) {
      return new byte[0];
    }
    prePlain = decipher(in, offset);
    pos = prePlain[0] & 7;
    int count = len - pos - 10;
    if (count < 0) {
      return new byte[0];
    }
    int i;
    for (i = offset; i < m.length; i++) {
      m[i] = 0;
    }

    out = new byte[count];
    preCrypt = 0;
    crypt = 8;
    contextStart = 8;
    pos++;
    for (padding = 1; padding <= 2; ) {
      if (pos < 8) {
        pos++;
        padding++;
      }
      if (pos == 8) {
        m = in;
        if (!decrypt8Bytes(in, offset, len)) {
          return new byte[0];
        }
      }
    }

    i = 0;
    while (count != 0) {
      if (pos < 8) {
        out[i] = (byte) (m[offset + preCrypt + pos] ^ prePlain[pos]);
        i++;
        count--;
        pos++;
      }
      if (pos == 8) {
        m = in;
        preCrypt = crypt - 8;
        if (!decrypt8Bytes(in, offset, len)) {
          return new byte[0];
        }
      }
    }
    for (padding = 1; padding < 8; padding++) {
      if (pos < 8) {
        if ((m[offset + preCrypt + pos] ^ prePlain[pos]) != 0) {
          return new byte[0];
        }
        pos++;
      }
      if (pos == 8) {
        m = in;
        preCrypt = crypt;
        if (!decrypt8Bytes(in, offset, len)) {
          return new byte[0];
        }
      }
    }

    return out;
  }

  private byte[] encipher(byte[] in) {
    try {
      int loop = 16;
      long y = getUnsignedInt(in, 0, 4);
      long z = getUnsignedInt(in, 4, 4);
      long a = getUnsignedInt(key, 0, 4);
      long b = getUnsignedInt(key, 4, 4);
      long c = getUnsignedInt(key, 8, 4);
      long d = getUnsignedInt(key, 12, 4);
      long sum = 0L;
      long delta = 0xffffffff9e3779b9L;
      delta &= 0xffffffffL;
      while (loop-- > 0) {
        sum += delta;
        sum &= 0xffffffffL;
        y += (z << 4) + a ^ z + sum ^ (z >>> 5) + b;
        y &= 0xffffffffL;
        z += (y << 4) + c ^ y + sum ^ (y >>> 5) + d;
        z &= 0xffffffffL;
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeInt((int) y);
      dos.writeInt((int) z);
      dos.close();
      return baos.toByteArray();
    } catch (IOException e) {
      LOG.error("encipher failure", e);
      return new byte[0];
    }
  }

  private byte[] decipher(byte[] in, int offset) {
    try {
      int loop = 16;
      long y = getUnsignedInt(in, offset, 4);
      long z = getUnsignedInt(in, offset + 4, 4);
      long a = getUnsignedInt(key, 0, 4);
      long b = getUnsignedInt(key, 4, 4);
      long c = getUnsignedInt(key, 8, 4);
      long d = getUnsignedInt(key, 12, 4);
      long sum = 0xffffffffe3779b90L;
      sum &= 0xffffffffL;
      long delta = 0xffffffff9e3779b9L;
      delta &= 0xffffffffL;
      while (loop-- > 0) {
        z -= (y << 4) + c ^ y + sum ^ (y >>> 5) + d;
        z &= 0xffffffffL;
        y -= (z << 4) + a ^ z + sum ^ (z >>> 5) + b;
        y &= 0xffffffffL;
        sum -= delta;
        sum &= 0xffffffffL;
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream(8);
      DataOutputStream dos = new DataOutputStream(baos);
      dos.writeInt((int) y);
      dos.writeInt((int) z);
      dos.close();
      return baos.toByteArray();
    } catch (IOException e) {
      LOG.error("decipher failure", e);
      return new byte[0];
    }
  }

  private byte[] decipher(byte[] in) {
    return decipher(in, 0);
  }

  private void encrypt8Bytes() {
    for (pos = 0; pos < 8; pos++) {
      if (header) {
        plain[pos] ^= prePlain[pos];
      } else {
        plain[pos] ^= out[preCrypt + pos];
      }
    }

    byte[] crypted = encipher(plain);
    System.arraycopy(crypted, 0, out, crypt, 8);
    for (pos = 0; pos < 8; pos++) {
      out[crypt + pos] ^= prePlain[pos];
    }

    System.arraycopy(plain, 0, prePlain, 0, 8);
    preCrypt = crypt;
    crypt += 8;
    pos = 0;
    header = false;
  }

  private boolean decrypt8Bytes(byte[] in, int offset, int len) {
    for (pos = 0; pos < 8; pos++) {
      if (contextStart + pos >= len) {
        return true;
      }
      prePlain[pos] ^= in[offset + crypt + pos];
    }

    prePlain = decipher(prePlain);
    if (prePlain == null) {
      return false;
    } else {
      contextStart += 8;
      crypt += 8;
      pos = 0;
      return true;
    }
  }
}
