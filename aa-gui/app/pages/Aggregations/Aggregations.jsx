import React from 'react';

//import API from '../util/API';
export default class Aggregations extends React.Component {

  constructor(props, context) {
    super(props, context);
    console.log('constructor APP');
    this.state = {items: []};
    fetch('/aa/api/health')
      .then(res => res.json())
      .then(json => console.log('parsed json', json))
      .catch(ex => console.log('parsing failed', ex));
  }

  handleAboutClick = (e) => {
    e.stopPropagation();
    this.props.history.replace('/about');
  };

  render() {
    return (
      <div className="">
        <p>Aggregations</p>
        <a href="#" onClick={this.handleAboutClick}>about-link</a>
      </div>
    );
  }
}

