import styles from './_Aggregator.scss';

import React from 'react';

//import API from '../util/API';

export default class Aggregator extends React.Component {

  componentDidMount() {
    //let id = this.props.params.id;
    //this.setState({
    //                // route components are rendered with useful information, like URL params
    //                aAggregator: API.findAggregatorById(id)
    //              })
  }

  render() {
    return (
      <div className={styles.aggregator}>
        <p>Aggregator {this.props.params.id}</p>
      </div>
    );
  }
}
