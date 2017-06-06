import React from 'react';

import styles from './_Flash.scss';

import Utils from '../../util/Utils';

export default class Flash extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {display: props.message !== undefined};
  }

  close = (e) => {
    Utils.stop(e);
    this.setState({display: false})
  };

  componentWillReceiveProps(nextProps) {
    this.setState({display: nextProps.message !== undefined});
  }

  render() {
    return this.state.display ? (
      <section className={styles.flash}><p>{this.props.message}</p><a href="#" onClick={this.close}>
        <i className="fa fa-remove"></i></a>
      </section>
    ) : <div/>;
  }

}
