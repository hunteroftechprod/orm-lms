# ORM LMS

Учебная платформа для курса по ORM и Hibernate. Преподаватель формирует программу из модулей и уроков, выдаёт задания и выставляет оценки. Студент записывается на курс, отправляет решения и проходит тесты. PostgreSQL хранит расписание, учебные материалы и результаты.

## Запуск

Для запуска в контейнерах нужны Docker и Docker Compose. Для сборки и тестов из терминала дополнительно нужны JDK 17+ и Maven 3.9+.

```bash
cp .env.example .env
docker compose up --build -d
docker compose logs -f app
```

После сообщения `Started LmsApplication` откройте [Swagger UI](http://localhost:8080/swagger-ui/index.html) или выполните `curl -fsS http://localhost:8080/api/courses`. Docker загружает необходимые образы, Maven устанавливает Java-зависимости из `pom.xml`. Если порт 8080 занят, измените `LMS_PORT` в `.env`. Для остановки используйте `docker compose down`. PostgreSQL хранит данные в именованном томе.

Для запуска Maven с подготовленной локальной PostgreSQL укажите настройки подключения и пароль демонстрационной учётной записи в окружении.

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lms
export SPRING_DATASOURCE_USERNAME=lms
export SPRING_DATASOURCE_PASSWORD='local-postgres-password'
export LMS_ADMIN_PASSWORD='local-admin-password'
mvn -ntp spring-boot:run -Dspring-boot.run.profiles=dev
```

Профиль `dev` добавляет демонстрационный курс «Основы Spring» с преподавателем, студентом, модулями, уроками, заданиями и тестами. Повторный запуск пропускает загрузку, если демо-курс уже существует. Единственный демонстрационный логин `student` связан с пользователем `student1@example.com`. Для него можно задать другой email через `LMS_STUDENT_EMAIL`. Операции записи в каталог и учебную программу доступны ролям TEACHER и ADMIN. Пароли логинов `admin`, `teacher` и `student` задаются в окружении. Для Compose учебные значения приведены в `.env.example`. HTTP Basic здесь используется для учебной демонстрации. Запросы за пределами учебного окружения потребуют отдельной политики доступа и HTTPS.

## API

| Назначение | Маршрут |
|---|---|
| Пользователи | CRUD `/api/users` с ролью ADMIN |
| Категории и теги | GET, POST `/api/categories`, `/api/tags` |
| Курсы | CRUD `/api/courses`, GET `/api/courses/{id}/program` |
| Модули | POST `/api/courses/{id}/modules`, PUT и DELETE `/api/courses/{id}/modules/{moduleId}` |
| Уроки и расписание | CRUD `/api/lessons`. Поле `scheduledAt` принимает ISO 8601, например `2026-12-01T15:00:00Z` |
| Запись и отписка | CRUD `/api/enrollments`, фильтры `?courseId=` и `?studentId=` |
| Учебные задания | CRUD `/api/assignments` |
| Сдача и оценивание | POST `/api/submissions`, PATCH `/api/submissions/{id}/grade`, GET по ID или с фильтром `?assignmentId=` / `?studentId=`, DELETE по ID |
| Тесты и вопросы | CRUD `/api/quizzes`, `/api/questions`, POST `/api/questions/{id}/options`, DELETE `/api/questions/options/{optionId}` |
| Прохождение теста и результаты | POST `/api/quiz-submissions`, GET по ID или с фильтром `?quizId=` / `?studentId=`, DELETE по ID |
| Отзывы | CRUD `/api/reviews` |

Для примера возьмите реальные ID из Swagger или ответов API. После создания либо выбора курса откройте его программу, найдите урок и задание, проверьте запись студента. Для теста найдите ID теста, вопроса и вариантов ответа. Ключ в `answers` — ID вопроса, значение — массив ID выбранных вариантов. Сервер вычисляет процент и сохраняет попытку.

```bash
curl -fsS http://localhost:8080/api/courses
curl -fsS http://localhost:8080/api/courses/1/program
curl -fsS 'http://localhost:8080/api/enrollments?courseId=1'

curl -fsS -u "student:student-local-demo" \
  -H 'Content-Type: application/json' \
  -d '{"quizId":1,"studentId":2,"answers":{"1":[1]}}' \
  http://localhost:8080/api/quiz-submissions
```

Оценка проходит проверку диапазона, дубли записей и домашних работ блокируются в базе. Общий обработчик возвращает 400 для некорректного запроса, 404 для отсутствующей сущности и 409 при конфликте ограничения PostgreSQL. Удаление курса каскадно удаляет его учебную структуру и связанные результаты.

## Архитектура и тесты

[Описание модели и модулей](docs/architecture.md).

```bash
mvn -ntp clean test-compile             # Проверка компиляции
mvn -ntp -Dtest=QuizScoringTest test     # Юнит-тесты без Docker
mvn -ntp clean verify                   # Все тесты, требуется запущенный Docker
```

Интеграционные тесты используют временную PostgreSQL 16. Проверяются создание и удаление графа сущностей, CRUD, каскады, ленивая загрузка, REST и учебный сценарий от записи до оценки. В `LazyLoadingTest` связь намеренно читается после закрытия сессии. Рабочий REST-сценарий формирует DTO внутри сервисной транзакции. GitHub Actions выполняет проверку изменений, собирает JAR и Docker-образ.
