package com.github.leaderelection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Internal libs with various reusable functions.
 * 
 * @author gaurav
 */
final class InternalLib {
  private static final transient Logger logger =
      LogManager.getLogger(InternalLib.class.getSimpleName());
  private static final ObjectMapper objectMapper = new ObjectMapper();

  {
    objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
  }

  /**
   * Get ObjectMapper for json ser-de
   */
  public static ObjectMapper getObjectMapper() {
    return objectMapper;
  }

}
