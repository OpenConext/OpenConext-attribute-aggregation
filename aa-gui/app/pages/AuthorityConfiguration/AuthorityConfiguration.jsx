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
      selectedAuthority: undefined
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
    let style = this.state.selectedAuthority && this.state.selectedAuthority.id === authority.id ?
      styles.authority_selected : styles.authority;
    return (
      <div key={authority.id} className={style}>
        <a href="#" className={styles.authority_link}
           onClick={this.handleShowAuthority(authority)}>{authority.id}</a>
        <em>{authority.description}</em>
      </div>)
  }

  renderAuthority() {
    var selectedAuthority = this.state.selectedAuthority;
    return selectedAuthority ? this.renderAuthorityDetails(selectedAuthority) : <div></div>
  }

  renderAuthorityDetails(authority) {
    return (
      <table>
        <tbody>
        <tr>
          <td>{i18n.t('authority.description')}</td>
          <td>{authority.description}</td>
          <td></td>
        </tr>
        <tr>
          <td>{i18n.t('authority.endpoint')}</td>
          <td>{authority.endpoint}</td>
          <td></td>
        </tr>
        <tr>
          <td>{i18n.t('authority.userName')}</td>
          <td>{authority.user}</td>
          <td></td>
        </tr>
        {authority.attributes.map(this.renderAttribute)}
        </tbody>
      </table>
    );
  }

  renderAttribute(attribute) {
    var valueToString = (val) => val !== undefined && val !== null ? val.toString() : '';
    return attributeKeys.map((key) =>
      <tr key={attribute.id}>
        <td></td>
        <td>{i18n.t('authority.' + key)}</td>
        <td>{valueToString(attribute[key])}</td>
      </tr>
    )
  }


  render() {
    return (
      <div className={styles.mod_container}>
        <div className={styles.mod_left}>
          {this.state.authorities.map((authority) => this.renderAuthorityLink(authority))}
        </div>
        <div className={styles.mod_right}>
          {this.renderAuthority()}
        </div>

      </div>
    );
  }
}
