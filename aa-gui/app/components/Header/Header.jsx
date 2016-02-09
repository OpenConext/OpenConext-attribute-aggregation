import styles from './_Header.scss';

import i18n from '../../util/I18N';
import API from '../../util/API';

import LanguageSelector from './LanguageSelector';
import Spinner from './Spinner';

import React from 'react';

const gitHub = require('../../images/github.png');
const pivotal = require('../../images/pivotaltracker.png');
require('../../images/logo@2x.png');

export default class Header extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {
      user: {
        displayNmae : ''
      }
    };
    API.getUser((json) => this.setState({user: json}));
  }

  renderMeta = () => {
    return (
      <header className={styles.meta}>
        <div className={styles.name}>
          {i18n.t("header.welcome",{name: this.state.user.displayName})}
        </div>
        <LanguageSelector />
        <a href='https://github.com/OpenConext/OpenConext-attribute-aggregation' target='_blank'>
          <img className={styles.logo} src={gitHub}/>
        </a>
        <a href='https://www.pivotaltracker.com/n/projects/1501602' target='_blank'>
          <img className={styles.logo} src={pivotal}/>
        </a>
      </header>
    );
  };

  render() {
    return (
      <div className={styles.header}>
        <div className={styles.title}><a href='/'>{i18n.t('header.title')}</a></div>
        <Spinner />
        {this.renderMeta()}
      </div>
    );
  }
}
