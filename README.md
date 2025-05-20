# Cloud Storage Service

Облачное хранилище файлов, реализованное на основе Spring Boot.

## Основные возможности

- Авторизация через JWT (JSON Web Token)
- Загрузка, скачивание, удаление и переименование файлов
- Защита API с помощью Spring Security
- Ограничения: максимальный размер файла — 10 МБ
- Поддержка CORS
- Полностью изолированный доступ к файлам пользователей

## Архитектура приложения

Проект построен по принципам многослойной архитектуры.


### Слои приложения:

| Слой           | Классы |
|----------------|--------|
| Controller     | AuthController, FileController |
| Service        | AuthService, FileService, UserDetailsServiceImpl |
| Repository     | UserRepository, FileRepository |
| Entity         | User, File |
| DTO            | LoginRequest, LoginResponse, FileResponse, RenameFileRequest, ErrorResponse |
| Security       | JwtFilter |
| Exception      | GlobalExceptionHandler, CorsException, StorageException, FileProcessingException, InvalidTokenException, UserAlreadyExistsException, UserNotFoundException |
| Config         | MultipartConfig, CorsProperties, SecurityConfig |
| Util           | JwtTokenUtil |

## Хранение файлов

Файлы хранятся в локальной файловой системе. Путь задается в файле настроек `application.yml`.



### Слои тестов приложения:

| Слой           | Классы |
|----------------|--------|
| Service        | AuthServiceTest, FileServiceTest |
| Repository     | UserRepositoryTest |
| Util           | JwtTokenUtilTest |
| Config         | TestContainersConfig |

## Хранение файлов

Файлы хранятся в локальной файловой системе. Путь задается в файле настроек `application-test.yml`.


### Стек технологий
| Категория | Технология |
|----------------|--------|
|  Backend | Spring Boot (Java 17) |
| База данных | PostgreSQL |
| Безопасность | Spring Security + JWT |
| Файловое хранилище | Локальная файловая система |
| Валидация | Hibernate Validator |
| Логгирование | Log4j2 |
| Тестирование | JUnit 5, Mockito, Testcontainers
| Сборка | Maven |
| Формат API | JSON |

### Запуск через Docker Compose
```
docker-compose -f docker-compose.yml up --build
```

