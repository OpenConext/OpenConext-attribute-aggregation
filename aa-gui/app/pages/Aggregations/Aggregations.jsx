import styles from './_Aggregations.scss';

import React from 'react';
import ReactTooltip from 'react-tooltip';
import i18n from 'i18next';
import API from '../../util/API';
import Flash from '../../components/Flash/Flash';

let { DataTable } = require('react-data-components');

import Utils from '../../util/Utils';

export default class Aggregations extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {
      aggregations: []
    };

    API.getAggregations((json) => {
      this.setState({aggregations: json});
    });
  }

  handleShowAggregation = (aggregation) => (e) => {
    Utils.stop(e);
    this.props.history.replace('/aggregation/' + aggregation.id);
  };

  handleDeleteAggregation = (aggregation) => (e) => {
    stop(e);
    if (confirm(i18n.t("aggregations.confirmation", {name: aggregation.name}))) {
      API.deleteAggregation(aggregation.id, () =>
        API.getAggregations((json) => {
          this.setState({aggregations: json, flash: i18n.t('aggregations.deleted', {name: aggregation.name})});
        })
      )
    }
  };

  renderServiceProviders = (val, row) => <ul className={styles.attributes}>{row.serviceProviders.map((sp) =>
    <li key={sp.name}>{sp.name !== undefined && sp.name !== null ? sp.name : sp.entityId}</li>)}</ul>;

  renderAttributes = (val, row) => <ul className={styles.attributes}>{row.attributes.map((attr) =>
    <li key={attr.name}>{attr.attributeAuthorityId} <i className="fa fa-arrow-right"></i> {attr.name}</li>)}</ul>;

  renderActions = (val, row) => (<div>
    <a href="#" onClick={this.handleShowAggregation(row)}
       data-tip={i18n.t("aggregations.edit")}><ReactTooltip /> <i className="fa fa-edit"></i>
    </a>
    <a href="#" data-tip={i18n.t("aggregations.delete")} onClick={this.handleDeleteAggregation(row)}>
      <ReactTooltip /> <i className="fa fa-remove"></i>
    </a>
  </div>);

  render() {
    let columns = [
      {title: i18n.t('aggregations.name'), prop: 'name', width: '15%'},
      {title: i18n.t('aggregations.serviceProviders'), render: this.renderServiceProviders, width: '35%'},
      {title: i18n.t('aggregations.attributes'), render: this.renderAttributes, width: '35%'},
      {title: i18n.t('aggregations.actions'), render: this.renderActions, width: '15%'}
    ];

    //migrate to https://github.com/facebook/fixed-data-table/blob/master/examples/SortExample.js ???
    return (
      <div>
        <Flash message={this.state.flash}/>
        <div className={styles.mod_container}>
          <div className={styles.mod_center}>
            <DataTable
              className={styles.container}
              keys={['id']}
              columns={columns}
              initialData={this.state.aggregations}
              initialPageLength={20}
              initialSortBy={{ prop: 'name', order: 'descending' }}
              pageLengthOptions={[ 5, 20, 50 ]}
            />
          </div>
        </div>
      </div>
    );
  }
}

