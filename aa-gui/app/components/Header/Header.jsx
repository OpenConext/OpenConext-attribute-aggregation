import styles from './_Header.scss';

import i18n from '../../util/I18N';
import API from '../../util/API';

import LanguageSelector from './LanguageSelector';

import React from 'react';

const gitHub = require('../../images/github.png');
require('../../images/logo@2x.png');

export default class Header extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {
      dropDownActive: false
    };
  }

  renderMeta = () => {
    return (
      <header className={styles.meta}>
        <div className={styles.name}>
        Welkom John Doe
        </div>
        <LanguageSelector />
        <a href='https://github.com/oharsta/OpenConext-attribute-aggregation' target='_blank'>
          <img className={styles.logo} src={gitHub}/>
        </a>
      </header>
    );
  };

  render() {
    return (
      <div className={styles.header}>
        <div className={styles.title}><a href='/'>{i18n.t('header.title')}</a></div>
        {this.renderMeta()}
      </div>
    );
  }
}
