import React from "react";
import ReactDOM from "react-dom";
import { HashRouter, Route, Switch, NavLink, Redirect } from "react-router-dom";
import AppBar from "@mui/material/AppBar";
import Toolbar from "@mui/material/Toolbar";
import Typography from "@mui/material/Typography";
import Grid from "@mui/material/Grid";
import List from "@mui/material/List";
import ListItemButton from "@mui/material/ListItemButton";
import Example from "./components/Example";
import Regions from "./components/Regions";
import MySearch from "./components/MySearch/MySearch";
import "./styles/main.css";

function App() {
  return (
    <HashRouter>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="h1">
            Лабораторная №1 — React SPA
          </Typography>
        </Toolbar>
      </AppBar>
      <Grid container className="spa-layout">
        <Grid item xs={12} sm={3} component="aside" className="spa-sidebar">
          <nav aria-label="Компоненты приложения">
            <List>
              <ListItemButton component={NavLink} to="/example" activeClassName="spa-active">
                Example
              </ListItemButton>
              <ListItemButton component={NavLink} to="/regions" activeClassName="spa-active">
                Regions
              </ListItemButton>
              <ListItemButton component={NavLink} to="/mysearch" activeClassName="spa-active">
                MySearch
              </ListItemButton>
            </List>
          </nav>
        </Grid>
        <Grid item xs={12} sm={9} component="main" className="spa-content">
          <Switch>
            <Route exact path="/example"><Example embedded /></Route>
            <Route exact path="/regions"><Regions embedded /></Route>
            <Route exact path="/mysearch"><MySearch embedded /></Route>
            <Redirect to="/example" />
          </Switch>
        </Grid>
      </Grid>
    </HashRouter>
  );
}

ReactDOM.render(<App />, document.getElementById("reactapp"));
