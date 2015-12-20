import React from 'react'
import { render } from 'react-dom'
import { Link } from 'react-router'

import API from '../../util/API';

import Header from '../Header/Header';
import Navigation from '../Navigation/Navigation';
import Footer from '../Footer/Footer';

export default class App extends React.Component {

  constructor(props, context) {
    super(props, context);
  }

  render() {
    return (
      <div className="">
        <Header />
        <Navigation />
        {this.props.children}
        <Footer />
      </div>
    );
  }
}
