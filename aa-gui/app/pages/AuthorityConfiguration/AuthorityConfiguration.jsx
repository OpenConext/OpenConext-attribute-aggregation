import React from 'react';

import API from '../../util/API';

export default class AuthorityConfiguration extends React.Component {

  constructor(props, context) {
    super(props, context);
    API.getAuthorityConfiguration((json) => this.state = json);
  }

  render() {
    return (
      <div className="">
        <p>Authorities</p><i className="fa fa-edit"></i>
        <p>{this.state}</p>
      </div>
    );
  }
}
