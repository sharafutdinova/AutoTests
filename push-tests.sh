#!/bin/bash

# Установите режим строгой обработки ошибок
set -euo pipefail

# -------------------- Конфигурация --------------------
# Имя локального образа (источник)
LOCAL_IMAGE_NAME="nbank-autotests"

# Docker Hub параметры (измените под себя)
DOCKERHUB_USERNAME="alsushaz"   # <-- поменяйте на свой
IMAGE_NAME="nbank-autotests-alsu"  # имя образа в Docker Hub
TAG="${1:-latest}"  # тег: первый аргумент или 'latest'

# Итоговое имя: username/image-name:tag
REMOTE_IMAGE="${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${TAG}"

# -------------------- Проверка токена --------------------
if [ -z "${DOCKERHUB_TOKEN:-}" ]; then
    echo "❌ Ошибка: переменная окружения DOCKERHUB_TOKEN не установлена"
    echo "   Экспортируйте её: export DOCKERHUB_TOKEN='your-token'"
    exit 1
fi

# -------------------- Проверка существования локального образа --------------------
if ! docker image inspect "$LOCAL_IMAGE_NAME" >/dev/null 2>&1; then
    echo "❌ Ошибка: образ '$LOCAL_IMAGE_NAME' не найден локально"
    echo "   Сначала соберите его: docker build -t $LOCAL_IMAGE_NAME ."
    exit 1
fi

# -------------------- Логин в Docker Hub --------------------
echo "🔑 Логин в Docker Hub как $DOCKERHUB_USERNAME..."
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin
echo "✅ Логин успешен"

# -------------------- Тегирование --------------------
echo "🏷️ Тегирование образа: $LOCAL_IMAGE_NAME -> $REMOTE_IMAGE"
docker tag "$LOCAL_IMAGE_NAME" "$REMOTE_IMAGE"

# -------------------- Пуш образа --------------------
echo "📤 Отправка образа в Docker Hub..."
docker push "$REMOTE_IMAGE"
echo "✅ Образ отправлен"

# -------------------- Финальное сообщение --------------------
echo "==============================================="
echo "🎉 Готово! Скачать образ можно командой:"
echo "   docker pull $REMOTE_IMAGE"
echo "==============================================="