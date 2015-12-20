import React from 'react';

import { Link } from 'react-router'

import styles from './_Navigation.scss';

import i18n from '../../util/I18N';

const ACTIVE = {color: 'white', background: 'black'};

export default class Navigation extends React.Component {

  render() {
    return (
      <nav className={styles.navigation}>
        <ul>
          <li><Link to="/aggregations" activeStyle={ACTIVE}>aggregations</Link></li>
          <li><Link to="/authorities" activeStyle={ACTIVE}>authorities</Link></li>
          <li><Link to="/about" activeStyle={ACTIVE}>about</Link></li>
        </ul>
      </nav>
    );
  }
}
