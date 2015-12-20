import styles from './_Header.scss';
import React from 'react';

export default class Header extends React.Component {
  render() {
    var year = (new Date()).getFullYear();
    return (
      <header className={styles.header}>
        &copy; Header TODO &nbsp;{year}
      </header>
    );
  }
}
