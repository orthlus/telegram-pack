FROM openjdk:17-alpine
ENV TZ=Europe/Moscow
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
COPY target/dependency dependency/
ADD target/aelaort-telegram-pack.jar .
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "aelaort-telegram-pack.jar", "-D", "exec.mainClass=main.Main"]