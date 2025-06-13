package io.jungmini.messaging;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RedisListenerProcessor implements BeanPostProcessor, ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(RedisListenerProcessor.class);
	private final ObjectMapper objectMapper;
	private RedisMessageListenerContainer listenerContainer;

	public RedisListenerProcessor(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		Method[] methods = bean.getClass().getDeclaredMethods();

		for (Method method : methods) {
			if (method.isAnnotationPresent(RedisListener.class)) {
				RedisListener annotation = method.getAnnotation(RedisListener.class);
				registerListener(bean, method, annotation);
			}
		}
		return bean;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.listenerContainer = applicationContext.getBean(RedisMessageListenerContainer.class);
	}

	private void registerListener(Object bean, Method method, RedisListener annotation) {
		validateMethodSignature(method);

		MessageListener listener = (message, pattern) -> {
			try {
				String messageBody = new String(message.getBody());
				String actualChannel = new String(message.getChannel());
				String channel = new String(pattern);

				Parameter[] parameters = method.getParameters();
				log.debug("Processing message: [{}] from channel: [{}] for method: [{}]",
					messageBody, channel, method.getName());

				if (parameters.length == 0) {
					method.invoke(bean);
					return;
				}

				if (parameters.length == 1) {
					Class<?> parameterType = parameters[0].getType();
					if (parameterType.equals(String.class)) {
						method.invoke(bean, messageBody);
					} else {
						Object parsedMessage = objectMapper.readValue(messageBody, parameterType);
						method.invoke(bean, parsedMessage);
					}
					return;
				}

				if (parameters.length == 2) {
					Class<?> parameterType1 = parameters[0].getType();
					Class<?> parameterType2 = parameters[1].getType();

					if (parameterType1.equals(String.class)) {
						// String, String
						method.invoke(bean, messageBody, actualChannel);
					} else {
						// Object, String
						Object parsedMessage = objectMapper.readValue(messageBody, parameterType1);
						method.invoke(bean, parsedMessage, actualChannel);
					}
					return;
				}

			} catch (Exception e) {
				log.error("Error processing Redis message for method: {} - {}", method.getName(), e.getMessage());
				throw new RuntimeException(e);
			}
		};

		for (String channel : annotation.channels()) {
			listenerContainer.addMessageListener(listener, new ChannelTopic(channel));
			log.info("Redis Listener Registered: [{}] -> [{}]", channel, method.getName());
		}

		for (String patternStr : annotation.patterns()) {
			listenerContainer.addMessageListener(listener, new PatternTopic(patternStr));
			log.info("Redis Pattern Listener Registered: [{}] -> [{}]", patternStr, method.getName());
		}
	}

	/**
	 * @RedisListener 메서드 시그니처 검증
	 * 허용되는 시그니처:
	 * 1. () - 파라미터 없음
	 * 2. (Object) - 메시지만
	 * 3. (Object, String) - 메시지 + 채널
	 */
	private void validateMethodSignature(Method method) {
		Parameter[] parameters = method.getParameters();
		String methodName = method.getDeclaringClass().getSimpleName() + "." + method.getName();

		if (parameters.length > 2) {
			throw new IllegalArgumentException(
				String.format("@RedisListener method '%s' can have at most 2 parameters", methodName));
		}

		if (parameters.length == 0) {
			return;
		}

		if (parameters.length == 1) {
			return;
		}

		Class<?> secondParamType = parameters[1].getType();
		if (!secondParamType.equals(String.class)) {
			throw new IllegalArgumentException(
				String.format("@RedisListener method '%s' second parameter must be String (channel), but was %s",
					methodName, secondParamType.getSimpleName()));
		}
	}
}