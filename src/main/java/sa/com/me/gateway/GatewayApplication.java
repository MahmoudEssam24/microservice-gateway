package sa.com.me.gateway;

import java.io.IOException;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitKeyGenerator;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.RateLimitUtils;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.config.properties.RateLimitProperties;
import com.marcosbarbero.cloud.autoconfigure.zuul.ratelimit.support.DefaultRateLimitKeyGenerator;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import sa.com.me.core.CoreApplication;
import sa.com.me.core.model.User;

@SpringBootApplication
@EnableEurekaClient
@EnableZuulProxy
@EnableFeignClients
@ComponentScan
public class GatewayApplication extends CoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public RateLimitKeyGenerator ratelimitKeyGenerator(RateLimitProperties properties, RateLimitUtils rateLimitUtils) {
		return new DefaultRateLimitKeyGenerator(properties, rateLimitUtils) {
			@Override
			public String key(HttpServletRequest request, Route route, RateLimitProperties.Policy policy) {
				final StringJoiner joiner = new StringJoiner(":");
				joiner.add(properties.getKeyPrefix());
				if (route != null) {
					joiner.add(route.getId());
				}
				policy.getType().forEach(matchType -> {
					String key = matchType.key(request, route, rateLimitUtils);
					if (key.equals("anonymous") && "POST".equalsIgnoreCase(request.getMethod())) {
						try {
							if (request.getRequestURI().contains("auth")
									&& request.getRequestURI().contains("public")) {
								User userLogin = new ObjectMapper().readValue(
										request.getReader().lines().collect(Collectors.joining(System.lineSeparator())),
										User.class);
								key = userLogin.getEmail();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (StringUtils.isNotEmpty(key)) {
						joiner.add(key);
					}
				});
				return joiner.toString();
			}
		};
	}

}
