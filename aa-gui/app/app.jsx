import './favicon.ico';
import './index.html';
import 'babel-core/polyfill';
import 'normalize.css/normalize.css';
import './scss/app.scss';

import React from 'react';
import { render } from 'react-dom';
import { Router, Route } from 'react-router';

import App from './components/App/App';

import Aggregations from './pages/Aggregations/Aggregations';
import Aggregators from './pages/Aggregators/Aggregators';
import Aggregator from './pages/Aggregator/Aggregator';


render((
         <Router>
           <Route component={App} path="/" >
             <Route component={Aggregations} path="/aggregations" />
             <Route component={Aggregators} path="/aggregators" >
               <Route component={Aggregator} path=":id" />
             </Route>
           </Route>
         </Router>
       ), document.getElementById('app'));
