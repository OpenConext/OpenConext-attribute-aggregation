import React from 'react';

import API from '../../util/API';

export default class Playground extends React.Component {

  constructor(props, context) {
    super(props, context);
    API.getServiceProviders((json) => this.state = json);
  }

  render() {
    return (
      <div className="">
        <p>Playground</p><i className="fa fa-edit"></i>
        <p>{this.state}</p>
      </div>
    );
  }
}
