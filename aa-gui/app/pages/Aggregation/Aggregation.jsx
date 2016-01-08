import React from 'react';

import API from '../../util/API';
import Utils from '../../util/Utils';

export default class Aggregation extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {aggregation: {}, serviceProviders: []};
    API.getServiceProviders((json) => this.setState({serviceProviders: json}));
    this.componentWillReceiveProps(props);
  }

  componentWillReceiveProps(nextProps) {
    let id = nextProps.params.id;
    if (id !== 'new') {
      API.getAggregation(id, (json) => this.setState({aggregation: json}));
    } else {
      this.setState({aggregation: {}});
    }
  }

  handleAboutClick = (e) => {
    Utils.stop(e);
    this.props.history.replace('/about');
  };

  render() {
    var name = this.state.aggregation.name;
      return (
      <div className="">
        <a href="#" onClick={this.handleAboutClick}>about-link</a>
        <p>Aggregation</p>
        <p>{name ? name : 'new'}</p>
        <p>{this.state.serviceProviders.length}</p>
      </div>
    );
  }
}

