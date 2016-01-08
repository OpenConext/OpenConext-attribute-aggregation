import styles from './_LanguageSelector.scss';

import React from 'react';

import i18n from 'i18next';

import Utils from '../../util/Utils';

let en = i18n.getFixedT('en');
let nl = i18n.getFixedT('nl');

const SELECTED = {color: 'white', background: '#4db3cf'};

export default class LanguageSelector extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {lang: i18n.options.lng};
  }

  renderLocaleChooser = (fixedT) => {
    return (
      <li key={fixedT('lang')}>
        <a
          href='#'
          style={this.state.lang === fixedT('lang') ? SELECTED : {}}
          onClick={this.handleChooseLocale(fixedT('lang'))}>
          {fixedT('code')}
        </a>
      </li>
    );
  };

  handleChooseLocale = (locale) => (e) => {
    Utils.stop(e);
    i18n.changeLanguage(locale, (err, t) => {
      // resources have been loaded
    });
    this.setState({lang: locale});
  };

  render() {
    return (
      <ul className={styles.language}>
        {[
          this.renderLocaleChooser(en),
          this.renderLocaleChooser(nl)
        ]}
      </ul>
    );
  }
}
