FROM eclipse-temurin:17-jre-alpine
ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV spring_profiles_active=production

COPY target/dependency dependency/
ADD target/aelaort-telegram-pack.jar .

ENTRYPOINT ["java", "-jar", "aelaort-telegram-pack.jar"]