import React from 'react'
import { render } from 'react-dom'
import { Link } from 'react-router'

import PubSub from 'pubsub-js'

import API from '../../util/API';

import Header from '../Header/Header';
import Navigation from '../Navigation/Navigation';
import Footer from '../Footer/Footer';

export default class App extends React.Component {

  constructor(props, context) {
    super(props, context);
    PubSub.subscribe('DEAD_SESSION', (msg, data) => {
      window.location.reload()
    });
    PubSub.subscribe('ERROR', (msg, data) => {
      this.props.history.replace('/error');
      //need to publish on different topic as Errors might not be constructed yet
      PubSub.publish('ERROR_RECEIVED', data);
    });
  }

  render() {
    return (
      <div>
        <Header />
        <Navigation />
        {this.props.children}
        <Footer />
      </div>
    );
  }
}
