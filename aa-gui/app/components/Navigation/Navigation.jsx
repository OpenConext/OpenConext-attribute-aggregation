import React from 'react';

import { Link } from 'react-router'

import styles from './_Navigation.scss';

import i18n from '../../util/I18N';

const ACTIVE = {fontWeight: 'bold'};

export default class Navigation extends React.Component {

  //<li><Link to="/about" activeStyle={ACTIVE}>{i18n.t('navigation.about')}</Link></li>

  render() {
    return (
      <nav className={styles.navigation}>
        <ul>
          <li><Link to="/aggregations" activeStyle={ACTIVE}>{i18n.t('navigation.aggregations')}</Link></li>
          <li><Link to="/authority-configuration" activeStyle={ACTIVE}>{i18n.t('navigation.authorities')}</Link></li>
          <li><Link to="/playground" activeStyle={ACTIVE}>{i18n.t('navigation.playground')}</Link></li>
          <li><Link to="/aggregation/new" activeStyle={ACTIVE}>{i18n.t('navigation.aggregation')}</Link></li>
        </ul>
      </nav>
    );
  }
}
