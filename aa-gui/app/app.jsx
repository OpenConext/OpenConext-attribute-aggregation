import './favicon.ico';
import './index.html';
import 'babel-core/polyfill';
import 'normalize.css/normalize.css';
import './scss/app.scss';

require('font-awesome-webpack');

import React from 'react';
import { render } from 'react-dom';
import { Router, Route, DefaultRoute, Redirect } from 'react-router';

import App from './components/App/App';

import About from './pages/About/About';

import Aggregations from './pages/Aggregations/Aggregations';
import Aggregators from './pages/Aggregators/Aggregators';
import Aggregator from './pages/Aggregator/Aggregator';

import Error from './pages/Error/Error';

render((
         <Router>
           <Redirect from="/" to="/about"/>
           <Route component={App} path="/" >
             <Route component={About} path="/about" />
             <Route component={Aggregations} path="/aggregations" />
             <Route component={Aggregators} path="/aggregators" />
             <Route component={Aggregator} path="/aggregators/:id" />
             <Route component={Error} path="/error" />
           </Route>
         </Router>
       ), document.getElementById('app'));
