#!/bin/bash
set -euo pipefail

# -------------------- Конфигурация --------------------
# Путь к docker-compose.yml
COMPOSE_FILE="${COMPOSE_FILE:-docker-compose.yml}"
# Если файл не найден в текущей папке, ищем в infra/docker_compose/
if [ ! -f "$COMPOSE_FILE" ]; then
    if [ -f "./infra/docker_compose/docker-compose.yml" ]; then
        COMPOSE_FILE="./infra/docker_compose/docker-compose.yml"
    else
        echo "❌ Не найден docker-compose.yml. Укажите переменную COMPOSE_FILE или разместите файл в текущей папке."
        exit 1
    fi
fi

# Имя образа с тестами
TEST_IMAGE="alsushaz/nbank-autotests-alsu:latest"

# -------------------- Функция остановки окружения --------------------
cleanup() {
    echo "🛑 Останавливаем Docker Compose окружение..."
    docker compose -f "$COMPOSE_FILE" down
    echo "✅ Окружение остановлено."
}

# Гарантированно останавливаем окружение при любом завершении скрипта
trap cleanup EXIT

# -------------------- Проверка/сборка тестового образа --------------------
if ! docker image inspect "$TEST_IMAGE" &>/dev/null; then
    echo "🔨 Образ $TEST_IMAGE не найден. Загружаем..."
    docker pull "$TEST_IMAGE"
fi

# -------------------- Запуск тестового окружения --------------------
echo "⬆️ Поднимаем тестовое окружение через docker compose..."
docker compose -f "$COMPOSE_FILE" up -d
echo "✅ Окружение запущено."

# Ожидаем инициализации сервисов
echo "⏳ Ожидание готовности сервисов (10 секунд)..."
sleep 10

# -------------------- Запуск контейнера с тестами --------------------
echo "🧪 Запускаем контейнер с тестами ($TEST_IMAGE)..."

# Настройка логов
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP
mkdir -p "$TEST_OUTPUT_DIR"/{logs,results,report,screenshots}

# Общие параметры для docker run
COMMON_ARGS=(
    --rm
    --network=host
    -v "$TEST_OUTPUT_DIR/logs://app/logs"
    -v "$TEST_OUTPUT_DIR/results://app/target/surefire-reports"
    -v "$TEST_OUTPUT_DIR/report://app/target/site"
    -v "$TEST_OUTPUT_DIR/screenshots://app/build/reports/tests"
    -e APIBASEURL="http://localhost:4111"
    -e DB_URL="jdbc:postgresql://localhost:5433/nbank"
    -e DB_USERNAME="postgres"
    -e DB_PASSWORD="postgres"
    -e UIBASEURL="http://nginx:80"
    -e SELENOID_URL="http://selenoid:4444"
    -e SELENOID_UI_URL="http://selenoid-ui:8080"
    -e BROWSER="chrome"
    -e WITH_MOCK=false
)

# Запуск API и UI тестов
for PROFILE in api ui; do
    echo "Запускаем $PROFILE тесты"
    docker run "${COMMON_ARGS[@]}" -e TEST_PROFILE="$PROFILE" "$TEST_IMAGE"
done

# Сохраняем код возврата тестов
TEST_EXIT_CODE=$?

# Вывод результата
if [ $TEST_EXIT_CODE -eq 0 ]; then
    echo "✅ Тесты успешно завершены."
else
    echo "❌ Тесты завершились с ошибкой (код $TEST_EXIT_CODE)."
fi

# Скрипт завершится, trap автоматически вызовет cleanup
exit $TEST_EXIT_CODE