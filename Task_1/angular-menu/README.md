# Лабораторная №2. Часть 3

Из каталога `Task_1/angular-menu`:

1. В Firebase Console создайте проект и зарегистрируйте веб-приложение. Заполните `public/firebase-config.json` по образцу `firebase-config.example.json`.
2. Создайте базу Cloud Firestore и коллекцию `list-menu`. Добавьте документы с произвольными ID, полем `description` типа string и полем `price` типа number. Примеры: Грибной суп — 9.5, Драники со сметаной — 12, Яблочный пирог — 7.5.
3. Включите Email/Password в Firebase Authentication и создайте пользователя для редактирования. Правила дают всем посетителям право чтения, а каждому пользователю проекта с активным входом — право редактирования.
4. Выполните команды под своей учётной записью Firebase.

```sh
npm ci
npm run build
npx firebase-tools login
npx firebase-tools deploy --project YOUR_PROJECT --only hosting,firestore:rules
```

Откройте адрес Hosting из вывода команды. Проверьте список и карточку, затем войдите через «Редактировать меню», добавьте блюдо, измените цену и удалите его. Обновите страницу после каждого действия, чтобы проверить сохранение в Cloud Firestore.
