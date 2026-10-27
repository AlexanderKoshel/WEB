import React  from "react"; 
import ReactDOM from "react-dom";
import { useState } from "react"; 
import { HashRouter, BrowserRouter as Router, Route, Switch, Link } from "react-router-dom";
import Example from "./components/Example";
import States from "./components/Regions";

function App() {
	return (
    <HashRouter> 
      <div>
        <ul>
          <li>
            <Link to="/example">Example</Link>
          </li>
          <li>
            <Link to="/regions">Regions</Link>
          </li>
          
        </ul>

        <hr />

        {/*
          A <Switch> looks through all its children <Route>
          elements and renders the first one whose path
          matches the current URL. Use a <Switch> any time
          you have multiple routes, but you want only one
          of them to render at a time
        */}
        <Switch>
          <Route exact path="/example">
            <Example />
          </Route>
          <Route path="/regions">
            <Regions />
          </Route>
          <Route path="/dashboard">
            <Regions />
          </Route>
        </Switch>
      </div>
    </HashRouter>
  );
	
	}
ReactDOM.render(
	
		<App />, document.getElementById("reactapp")
	
	);
