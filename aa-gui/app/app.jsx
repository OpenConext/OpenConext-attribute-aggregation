import './favicon.ico';
import './index.html';
import 'babel-core/polyfill';
import 'normalize.css/normalize.css';

import './scss/app.scss';

require('font-awesome-webpack');

import React from 'react';
import { render } from 'react-dom';
import { Router, browserHistory, Route, DefaultRoute, Redirect } from 'react-router';

import App from './components/App/App';

import About from './pages/About/About';
import Aggregation from './pages/Aggregation/Aggregation';
import Aggregations from './pages/Aggregations/Aggregations';
import AuthorityConfiguration from './pages/AuthorityConfiguration/AuthorityConfiguration';
import Error from './pages/Error/Error';
import Playground from './pages/Playground/Playground';

render((
         <Router>
           <Redirect from="/" to="/aggregations"/>
           <Route component={App} path="/" >
             <Route component={About} path="/about" />
             <Route component={Aggregation} path="/aggregation/:id" />
             <Route component={Aggregations} path="/aggregations" />
             <Route component={AuthorityConfiguration} path="/authority-configuration" />
             <Route component={Error} path="/error" />
             <Route component={Playground} path="/playground" />
           </Route>
         </Router>
       ), document.getElementById('app'));
