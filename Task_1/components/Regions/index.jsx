import React, { useState } from "react";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import "../../styles/main.css";
import "./styles.css";

function Regions() {
  const [substr, setSubstr] = useState("");
  const query = substr.toLocaleLowerCase("ru");
  const regions = window.lab1models.regionsModel()
    .filter((name) => name.toLocaleLowerCase("ru").includes(query))
    .sort((left, right) => left.localeCompare(right, "ru"));

  return (
    <main className="container Regions">
      <nav aria-label="Навигация">
        <a href="/">Главная</a>
        <a href="getting-started.html">Пример React</a>
        <a href="MySearch.html">Меню ресторана</a>
      </nav>
      <h1>Области Беларуси</h1>
      <label htmlFor="substrId">Введите подстроку для поиска</label>
      <input
        id="substrId"
        type="search"
        value={substr}
        onChange={(event) => setSubstr(event.target.value)}
      />
      <p className="state-search">Искомая подстрока: {substr}</p>
      <section aria-label="Результаты поиска" aria-live="polite">
        {regions.length > 0 ? (
          <List aria-label="Найденные области">
            {regions.map((name) => <ListItem key={name}>{name}</ListItem>)}
          </List>
        ) : <p>Совпадений не найдено</p>}
      </section>
    </main>
  );
}

export default Regions;
