import styles from './_About.scss';

import React from 'react';

const securityImage = require('../../images/aa.001.jpeg');
const endpointsImage = require('../../images/aa.002.jpeg');
const dataImage = require('../../images/aa.003.jpeg');

export default class About extends React.Component {

  render() {
    return (
      <div className={styles.mod_container_about}>
        <p className={styles.doc_header}>Attribute Aggregation endpoints</p>
        <div className={styles.doc_section}>
          <img src={endpointsImage}/>
        </div>
        <p className={styles.doc_header}>Security protocols</p>
        <div className={styles.doc_section}>
          <img src={securityImage}/>
        </div>
        <p className={styles.doc_header}>Data sources and relations</p>
        <div className={styles.doc_section}>
          <img src={dataImage}/>
        </div>

      </div>
    );
  }
}
