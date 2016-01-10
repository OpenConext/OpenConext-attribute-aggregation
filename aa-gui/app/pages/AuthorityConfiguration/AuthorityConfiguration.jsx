import styles from './_AuthorityConfiguration.scss';

import React from 'react';

import i18n from 'i18next';

import API from '../../util/API';
import Utils from '../../util/Utils';

const attributeKeys = ['name', 'caseExact', 'description', 'multiValued', 'mutability', 'required', 'returned', 'type', 'uniqueness']

export default class AuthorityConfiguration extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {
      authorities: [],
      selectedAuthority: {attributes: []}
    };
    API.getAuthorityConfiguration((json) => this.setState(
      {authorities: json.authorities, selectedAuthority: json.authorities[0]}
    ));
  }

  handleShowAuthority = (authority) => (e) => {
    Utils.stop(e);
    this.setState({selectedAuthority: authority})
  };

  renderAuthorityLink(authority) {
    var currentAuthority = this.state.selectedAuthority && this.state.selectedAuthority.id === authority.id;
    let style = currentAuthority ? styles.authority_selected : styles.authority_link;
    return currentAuthority ?
      <p key={authority.id} className={style}>{authority.id}</p> :
      <a key={authority.id} href="#" className={style}
         onClick={this.handleShowAuthority(authority)}>{authority.id}</a>;
  }

  renderAuthority() {
    var authority = this.state.selectedAuthority;
    return (
      <div>
        <p className={styles.title}>{authority.id}</p>
        <div className={styles.authority_details}>
          <span>{i18n.t('authority.description')}</span>
          <p>{authority.description}</p>
          <span>{i18n.t('authority.endpoint')}</span>
          <p>{authority.endpoint}</p>
          <span>{i18n.t('authority.userName')}</span>
          <p>{authority.user}</p>
        </div>
        {this.renderAttributes(authority.attributes)}

      </div>
    );
  }

  renderAttributes(attributes) {
    return (
      <div>
        <p>{i18n.t('authority.attributes')}</p>
        {attributes.map(this.renderAttribute)}
      </div>
    )
  }

  renderAttribute(attribute) {
    const valueToString = (val) => val !== undefined && val !== null ? val.toString() : '';
    return attributeKeys.map((key) =>
      <div>
        <p key={attribute.id}>{i18n.t('authority.' + key)}</p>
        <p>{valueToString(attribute[key])}</p>
      </div>)
  }


  render() {
    return (
      <div className={styles.mod_container}>
        <div className={styles.mod_left_authorities}>
          <p className={styles.title}>{i18n.t('authority.authorities')}</p>
          {this.state.authorities.map((authority) => this.renderAuthorityLink(authority))}
        </div>
        <div className={styles.mod_right_authorities}>
          {this.renderAuthority()}
        </div>

      </div>
    );
  }
}
