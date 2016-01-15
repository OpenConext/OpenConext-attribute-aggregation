import styles from './_Spinner.scss';

import React from 'react';

import PubSub from 'pubsub-js'

const spinner = require('../../images/ajax-loader.gif');

export default class Spinner extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {visible: false};
    let subscriber = (msg, data) => {
      this.setState({visible: data.started})
    };
    PubSub.subscribe('API', subscriber);
  }

  render() {
    let style = this.state.visible ? {} : {display: 'none'};
    return (
      <img className={styles.spinner} style={style} src={spinner}/>
    );
  }

}
