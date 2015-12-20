import React from 'react';
import ReactDOM from 'react-dom';
import TestUtils from 'react-addons-test-utils';

import Header from '../Header.jsx';
import { expect } from 'chai';

describe('Header', () => {
  it('Should have the correct header element', () => {
    let header = TestUtils.renderIntoDocument(
      <Header />
    );
    let headerElem = ReactDOM.findDOMNode(header);
    expect(headerElem.tagName.toLowerCase()).to.equal('header');
  });
});
