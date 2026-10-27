import React, { useEffect, useState } from "react";
import Prism from "prismjs";
import "prismjs/components/prism-jsx.js";
import "prismjs/themes/prism.css";
import "./styles.css";

function Example({ embedded = false }) {
  const [name] = useState(() => window.lab1models.exampleModel().name);
  const [motto, setMotto] = useState("Мой первый React-компонент");
  const [counter, setCounter] = useState(0);

  useEffect(() => {
    Prism.highlightAll();
    const timerID = setInterval(() => {
      setCounter((value) => value + 1);
    }, 2000);

    return () => clearInterval(timerID);
  }, []);

  return (
    <section className="container Example">
      {!embedded && <nav aria-label="Навигация">
        <a href="/">Главная</a>
        <a href="p2.html">Области Беларуси</a>
        <a href="MySearch.html">Меню ресторана</a>
      </nav>}
      <h1>Первый React-компонент</h1>
      <p className="lab1-example-output">Меня зовут {name}.</p>

      <h2>Изменение состояния</h2>
      <p className="motto-update">{motto}</p>
      <label htmlFor="mottoId">Изменить девиз</label>
      <input
        id="mottoId"
        type="text"
        value={motto}
        onChange={(event) => setMotto(event.target.value)}
      />
      <p>Счётчик обновлений каждые 2 секунды: {counter}</p>

      <h2>JSX и данные модели</h2>
      <p>
        Имя загружается из modelData/example.js. Хук useState хранит
        состояние, а его изменение обновляет отображение компонента.
      </p>
      <pre className="lab1-example-code">
        <code className="language-jsx">{'<p>Меня зовут {name}.</p>'}</code>
      </pre>
      <p>
        useEffect запускает таймер при открытии страницы и очищает его
        при удалении компонента.
      </p>
    </section>
  );
}

export default Example;
