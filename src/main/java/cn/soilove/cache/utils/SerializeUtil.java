package cn.soilove.cache.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * 序列化处理
 *
 * @author: Chen GuoLin
 * @create: 2020-04-14 18:17
 **/
@Slf4j
public class SerializeUtil {
    /**
     * 序列化
     */
    public static byte[] serialize(Object object) {
        ObjectOutputStream objectOutputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("etcRedis serialize Object error: ", e);
        } finally {
            try {
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
            } catch (IOException e) {
                log.error("etcRedis outputStream close error: ", e);
            }
        }
        return null;
    }

    /**
     * 反序列化
     *
     * @param bytes
     * @return
     */
    public static Object unSerialize(byte[] bytes) {
        ObjectInputStream objectInputStream = null;
        ByteArrayInputStream byteArrayInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return objectInputStream.readObject();
        } catch (Exception e) {
            log.error("etcRedis unSerialize Object error: ", e);
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (byteArrayInputStream != null) {
                    byteArrayInputStream.close();
                }
            } catch (IOException e) {
                log.error("etcRedis inputStream close error: ", e);
            }
        }
        return null;
    }

    /**
     * 获取具体的堆栈信息
     * @param e
     * @return
     */
    public static String getStackTraceAsString(Exception e) {
        try {
            // StringWriter将包含堆栈信息
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            // 获取堆栈信息
            e.printStackTrace(printWriter);
            // 转换成String，并返回该String
            StringBuffer error = stringWriter.getBuffer();
            return error.toString();
        } catch (Exception e2) {
            return "获取堆栈信息异常";
        }
    }
}
