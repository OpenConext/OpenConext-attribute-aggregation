import styles from './_App.scss';

import React from 'react';

import API from '../../util/API';

import Header from '../Header/Header';
import Body from '../Body/Body';
import Footer from '../Footer/Footer';

export default class App extends React.Component {

  constructor(props) {
    super(props);
    console.log('constructor APP');
    this.state = {items: []};
  }

  componentDidMount() {
    console.log('componentDidMount APP');
    API.getItems().then((items) => { this.setState({items: items}); });
  }

  render() {
    return (
      <div className={styles.app}>
        <Header />
        <Body items={this.state.items} />
        <Footer />
      </div>
    );
  }
}
