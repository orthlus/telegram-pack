package main;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.time.ZoneId;

@SpringBootApplication
@EnableCaching
public class Main {
	public static final ZoneId zone = ZoneId.of("Europe/Moscow");
	public static void main(String[] args) {
		new SpringApplicationBuilder(Main.class)
				.web(WebApplicationType.NONE)
				.beanNameGenerator(new CustomGenerator())
				.run(args);
	}

	public static class CustomGenerator extends AnnotationBeanNameGenerator {
		@Override
		public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
			String beanClassName = null;

			if (definition instanceof AnnotatedBeanDefinition) {
				String beanName = determineBeanNameFromAnnotation((AnnotatedBeanDefinition) definition);
				if (StringUtils.hasText(beanName)) {
					beanClassName = beanName;
				}
			}
			if (beanClassName == null){
				beanClassName = definition.getBeanClassName();
			}

			return ClassUtils.getPackageName(beanClassName) + "." + super.generateBeanName(definition, registry);
		}
	}
}
