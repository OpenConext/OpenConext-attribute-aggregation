import styles from './_About.scss';

import React from 'react';

const securityImage = require('../../images/aa_security.001.png');

export default class About extends React.Component {

  render() {
    return (
        <div className={styles.mod_container_about}>
          <p>The different security protocols for Attribute Aggregation</p>
          <img src={securityImage}/>
      </div>
    );
  }
}
