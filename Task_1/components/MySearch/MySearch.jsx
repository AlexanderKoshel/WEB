import React, { useState } from "react";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import "../../styles/main.css";

function MySearch() {
  const [substr, setSubstr] = useState("");
  const query = substr.toLocaleLowerCase("ru");
  const dishes = window.lab1models.myDataModel()
    .filter((name) => name.toLocaleLowerCase("ru").includes(query))
    .sort((left, right) => left.localeCompare(right, "ru"));

  return (
    <main className="container MySearch">
      <nav aria-label="Навигация">
        <a href="/">Главная</a>
        <a href="getting-started.html">Пример React</a>
        <a href="p2.html">Области Беларуси</a>
      </nav>
      <h1>Меню ресторана</h1>
      <p>Найдите блюдо по части его названия.</p>
      <label htmlFor="dishSearch">Введите подстроку для поиска</label>
      <input
        id="dishSearch"
        type="search"
        value={substr}
        onChange={(event) => setSubstr(event.target.value)}
      />
      <p>Искомая подстрока: {substr}</p>
      <section aria-label="Результаты поиска" aria-live="polite">
        {dishes.length > 0 ? (
          <List aria-label="Найденные блюда">
            {dishes.map((name) => <ListItem key={name}>{name}</ListItem>)}
          </List>
        ) : <p>Совпадений не найдено</p>}
      </section>
    </main>
  );
}

export default MySearch;
