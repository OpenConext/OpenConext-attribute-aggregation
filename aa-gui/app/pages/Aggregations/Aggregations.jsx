import React from 'react';

import API from '../../util/API';

export default class Aggregations extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {aggregations: []};
    API.getAggregations((json) => this.setState({aggregations: json}));
  }

  handleAboutClick = (e) => {
    e.stopPropagation();
    e.preventDefault();
    this.props.history.replace('/about');
  };

  render() {
    return (
      <div className="">
        <a href="#" onClick={this.handleAboutClick}>about-link</a>
        <p>Aggregations</p>
        <div>{this.state.aggregations.map((aggregation) => {
          return (<p key={aggregation.id}>{aggregation.name}</p>);
        })}</div>
      </div>
    );
  }
}

