import styles from './_About.scss';

import React from 'react';

const securityImage = require('../../images/aa_security.001.png');

export default class About extends React.Component {

  render() {
    return (
      <div className={styles.mod_container_about}>
        <p className={styles.doc_header}>Security protocols in Attribute Aggregation</p>
        <div className={styles.doc_section}>
          <img src={securityImage}/>
        </div>
      </div>
    );
  }
}
