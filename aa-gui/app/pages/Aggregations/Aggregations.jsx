import styles from './_Aggregations.scss';

import React from 'react';

//import API from '../util/API';

export default class Aggregations extends React.Component {

  componentDidMount() {
    console.log('Aggregators componentDidMount');
  }

  render() {
    return (
      <div className={styles.aggregations}>
        <p>Aggregations</p>
      </div>
    );
  }
}
