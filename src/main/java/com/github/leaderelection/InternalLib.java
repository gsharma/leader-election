package com.github.leaderelection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Internal libs with various reusable functions.
 * 
 * @author gaurav
 */
public final class InternalLib {
  private static final transient Logger logger =
      LogManager.getLogger(InternalLib.class.getSimpleName());
  private static final ObjectMapper objectMapper = new ObjectMapper();

  {
    objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.ANY));
    objectMapper.enableDefaultTyping();
  }

  /**
   * Get ObjectMapper for json ser-de
   */
  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /**
   * Flatten the passed object to a byte array. Note that the passed toFlatten object needs to be
   * Serializable.
   */
  public static byte[] serialize(final Serializable toFlatten) {
    byte[] serialized = null;
    ObjectOutputStream objectOutputStream = null;
    try {
      final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
      objectOutputStream.writeObject(toFlatten);
      serialized = byteArrayOutputStream.toByteArray();
    } catch (Exception problem) {
      logger.error("Unable to serialize the provided object to a byte array", problem);
    } finally {
      try {
        objectOutputStream.close();
      } catch (Exception problem) {
        logger.error("Unable to serialize the provided object to a byte array", problem);
      }
    }
    return serialized;
  }

  /**
   * Deserialize the provided byte array to a Serializable object.
   */
  public static Object deserialize(final byte[] flattened) {
    Object deserialized = null;
    if (flattened == null || flattened.length == 0) {
      logger.warn("Cannot deserialize an empty or null byte array");
      return deserialized;
    }
    ObjectInputStream objectInputStream = null;
    try {
      objectInputStream = new ObjectInputStream(new ByteArrayInputStream(flattened));
      deserialized = objectInputStream.readObject();
    } catch (Exception problem) {
      logger.error("Unable to deserialize the provided byte array to an object", problem);
    } finally {
      if (objectInputStream != null)
        try {
          objectInputStream.close();
        } catch (Exception problem) {
          logger.error("Unable to deserialize the provided byte array to an object", problem);
        }
    }
    return deserialized;
  }

}
