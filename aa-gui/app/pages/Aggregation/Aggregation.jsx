import React from 'react';

import API from '../../util/API';

export default class Aggregation extends React.Component {

  constructor(props, context) {
    super(props, context);
    let id = this.props.params.id;
    if (id !== 'new') {
      API.getAggregation((id, json) => this.state = json);
    } else {
      this.state = {};
    }

  }

  handleAboutClick = (e) => {
    e.stopPropagation();
    this.props.history.replace('/about');
  };

  render() {
    return (
      <div className="">
        <a href="#" onClick={this.handleAboutClick}>about-link</a>
        <p>Aggregation</p>
        <p>{this.state}</p>
      </div>
    );
  }
}

