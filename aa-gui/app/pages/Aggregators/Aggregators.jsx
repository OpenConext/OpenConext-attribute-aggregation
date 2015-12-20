import styles from './_Aggregators.scss';

import React from 'react';

//import API from '../util/API';

export default class Aggregators extends React.Component {

  componentDidMount() {
    console.log('Aggregators componentDidMount');
  }

  render() {
    return (
      <div className={styles.aggregators}>
        <p>Aggregators</p>
      </div>
    );
  }
}
