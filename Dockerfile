# базовый докер образ
# каждый раз с нуля строить базовый образ (java, mvn, git)
# голый докер образ и устанавлить java, maven
# ЕСЛИ МОЖНО СОЗДАТЬ ОБРАЗ ПОВЕРХ ДРУГОГО ОБРАЗА, ГДЕ ВСЕ УЖЕ УСТАНОВЛЕНО
# МАРКЕТПЛЕЙС ВСЕХ ДОКЕР ОБРАЗОВ - docker hub
# 3.9.9 - maven version, 21 - JDK version
FROM maven:3.9.9-eclipse-temurin-21

# Дефолтные значения аргументов
ARG TEST_PROFILE=api
ARG APIBASEURL=http://172.20.80.1:4111
ARG UIBASEURL=http://localhost:3000
ARG WITH_MOCK=false
ARG DB_URL=jdbc:postgresql://172.20.80.1:5433/nbank
ARG DB_USERNAME=postgres
ARG DB_PASSWORD=postgres

# Переменные окружения для контейнера
ENV TEST_PROFILE=${TEST_PROFILE}
ENV APIBASEURL=${APIBASEURL}
ENV UIBASEURL=${UIBASEURL}
ENV WITH_MOCK=${WITH_MOCK}
ENV DB_URL=${DB_URL}
ENV DB_USERNAME=${DB_USERNAME}
ENV DB_PASSWORD=${DB_PASSWORD}

# устанавливаем рабочую директорию /app
WORKDIR /app

# копируем помник
COPY pom.xml .

# загружаем зависимости и кешируем
RUN mvn dependency:go-offline

# копируем весь проект
COPY . .

RUN mkdir -p /app/logs
# создаем директорию для логов
# mvn test -DwithMock=false -P api
# mvn -DskipTests=true surfire-report:report
# -q лог не выводится в консоль, записывается в файл
# в run.log записываем логи, ошибки (> /app/logs/run.log 2>&1 ) (2>&1 | tee /app/logs/run.log)
# bash file
CMD /bin/bash -c " \
    { \
        echo '>>> Running tests with profile: ${TEST_PROFILE}' ; \
        mvn test -q -DwithMock=${WITH_MOCK} -P ${TEST_PROFILE} ; \
        echo '>>> Running surefire-report:report' ; \
        mvn -DskipTests=true surefire-report:report ; \
    } > /app/logs/run.log 2>&1 "