# TutorHelper (TutorHelp)

TutorHelper — приложение помощник репетитора на Kotlin/Android (Jetpack Compose). Проект реализует расписание, управление учениками, учет оплат и сводную аналитику.

## Возможности
- **Расписание**: три режима отображения (список, таблица, календарь-превью).
- **Ученики**: активные/архивные, поиск, карточки ученика.
- **Финансы**: список занятий с оплатами + график доходов.
- **Сводка**: аналитика по занятиям, оплатам, ученикам и доходам.
- **Настройки**: светлая/темная тема, язык (Русский/English).

## Технологический стек
- Kotlin + Jetpack Compose
- MVVM + Clean Architecture
- Hilt для DI
- Room для локальной БД
- Coroutines + Flow
- Navigation Compose
- DataStore Preferences
- MPAndroidChart для графиков доходов

## Структура проекта (Clean Architecture)
```
app/src/main/java/by/dreb/tutorhelper
├── data
│   ├── db (Room: entities, dao, database)
│   ├── mapper (мэппинг entity ↔ domain)
│   ├── preferences (DataStore)
│   ├── repository (имплементации репозиториев)
│   └── seed (первичные демонстрационные данные)
├── domain
│   ├── model (модели бизнес-логики)
│   ├── repository (интерфейсы)
│   └── usecase (use cases)
├── presentation
│   ├── finance/schedule/students/summary (ViewModel + UI)
│   └── settings (ViewModel + UI state)
└── di (Hilt-модули)
```

## Запуск проекта
1. Откройте проект в Android Studio (Android Studio Koala или новее).
2. Дождитесь синхронизации Gradle.
3. Запустите конфигурацию `app`.

## Заметки
- Данные сохраняются локально в Room.
- При первом запуске добавляются демонстрационные записи.
