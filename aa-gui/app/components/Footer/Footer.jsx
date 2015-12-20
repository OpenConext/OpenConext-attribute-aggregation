import React from 'react';

import styles from './_Footer.scss';

import i18n from '../../util/I18N';

export default class Footer extends React.Component {

  render() {
    return (
      <footer className={styles.footer}>
        <ul>
          <li dangerouslySetInnerHTML={{__html: i18n.t("footer.surfnet_html") }}></li>
          <li dangerouslySetInnerHTML={{__html: i18n.t("footer.terms_html") }}></li>
          <li dangerouslySetInnerHTML={{__html: i18n.t("footer.contact_html") }}></li>
        </ul>
      </footer>
    );
  }
}
