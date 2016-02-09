import styles from './_Spinner.scss';

import React from 'react';

import PubSub from 'pubsub-js'

const spinner = require('../../images/ajax-loader.gif');

export default class Spinner extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {visible: false};
    PubSub.subscribe('API', (msg, data) => this.setState({visible: data.started}));
  }

  render() {
    let style = this.state.visible ? {} : {display: 'none'};
    return (
      <img className={styles.spinner} style={style} src={spinner}/>
    );
  }

}
