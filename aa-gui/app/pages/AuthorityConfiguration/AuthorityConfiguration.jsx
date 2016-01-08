import styles from './_AuthorityConfiguration.scss';

import React from 'react';

import API from '../../util/API';
import Utils from '../../util/Utils';

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
    return selectedAuthority ?
      <div>{selectedAuthority.description}</div> :
      <div></div>
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
