import React from "react";
import I18n from "i18n-js";
import {prettyPrintJson} from "../utils/utils";
import PlayGroundInfo from "../components/playground_info";
import CheckBox from "../components/checkbox";
import SelectAuthority from "../components/select_authority";

import {attributeAggregation, authorityConfiguration} from "../api";

export default class Playground extends React.Component {

    constructor(props, context) {
        super(props, context);
        this.state = {
            attributesAndAuthorityIds: {},
            arp: {},
            userAttributes: {},
            result: undefined,
            duration: 0
        };
    }

    componentWillMount = () => authorityConfiguration().then(configuration => {
        const attributesFromAuthorities = new Set([].concat(
            ...configuration.authorities.map(authority => authority.attributes.map(attribute => attribute.name)))
        );
        const attributesAndAuthorityIds = Array.from(attributesFromAuthorities).reduce((acc, attribute) => {
            acc[attribute] = configuration.authorities
                .filter(authority => authority.attributes.map(attr => attr.name).includes(attribute))
                .map(authority => authority.id);
            acc[attribute].push("IdP");
            return acc;
        }, {});
        const arp = Array.from(attributesFromAuthorities).reduce((acc, attribute) => {
            acc[attribute] = [{id: 0, value: "", selected: false, source: ""}];
            return acc;
        }, {});
        const requiredAttributes = new Set([].concat(
            ...configuration.authorities.map(authority => authority.requiredInputAttributes.map(attribute => attribute.name)))
        );
        const userAttributes = Array.from(requiredAttributes).reduce((acc, attribute) => {
            acc[attribute] = "";
            return acc;
        }, {});
        this.setState({
            attributesAndAuthorityIds: attributesAndAuthorityIds,
            arp: arp,
            userAttributes: userAttributes,
            result: undefined,
            duration: 0
        });
    });

    doAttributeAggregation = () => {
        this.startDate = new Date();
        const {userAttributes, arp} = this.state;
        const arpAggregationRequest = {
            userAttributes: Object.keys(userAttributes).map(name => {
                return {
                    name: name,
                    values: [userAttributes[name]]
                };
            }),
            arpAttributes: arp
        };
        attributeAggregation(arpAggregationRequest).then(result => {
            const duration = new Date() - this.startDate;
            this.setState({result: result, duration: duration});
            document.body.scrollTop = document.documentElement.scrollTop = 0;
        });
    };

    renderRight = (result, duration) => result ? this.renderResult(result, duration) :
        <PlayGroundInfo locale={I18n.locale}/>;

    changeArpItem = (name, id) => e => {
        const value = e.target.checked;
        if (value) {
            const arp = {...this.state.arp};
            const item = arp[name].filter(item => item.id === id)[0];
            item.selected = true;
            this.setState({arp: arp});
        } else {
            this.removeArpItem(name, id);
        }
    };

    removeArpItem = (name, id) => {
        const arp = {...this.state.arp};
        const items = arp[name];
        const newItems = items.filter(item => item.id !== id);
        arp[name] = newItems.length === 0 ? [{id: 0, value: "", selected: false, source: ""}] : newItems;
        this.setState({arp: arp});
    };

    addArpItem = (name, sources) => () => {
        const arp = {...this.state.arp};
        const items = arp[name];
        items.push({id: items.length, selected: true, value: "*", source: sources[0]});
        this.setState({arp: arp});
    };

    changeArpValue = (name, id) => e => {
        const newValue = e.target.value;
        const arp = {...this.state.arp};
        const item = arp[name].filter(item => item.id === id)[0];
        item.value = newValue;
        this.setState({arp: arp});
    };

    changeArpSource = (name, id) => e => {
        const newValue = e.value;
        const arp = {...this.state.arp};
        const item = arp[name].filter(item => item.id === id)[0];
        item.source = newValue;
        this.setState({arp: arp});
    };

    changeUserAttribute = name => e => {
        const userAttributes = {...this.state.userAttributes};
        const newValue = e.target.value;
        userAttributes[name] = newValue;
        this.setState({userAttributes: userAttributes});
    };

    renderArpValue = (name, sources, index, value, last) =>
        <tr key={`${name}_${index}`}>
            <td className="enabled">
                <CheckBox name={`${name}_${index}`} value={value.selected} onChange={this.changeArpItem(name, index)}/>
            </td>
            <td className="rule">
                <input className={value.selected ? "" : "disabled"} type="text" value={value.value}
                       onChange={this.changeArpValue(name, index)}
                       disabled={!value.selected}/>
            </td>
            <td>
                <SelectAuthority onChange={this.changeArpSource(name, index)} source={value.source} sources={sources}
                                 disabled={!value.selected}/>
            </td>
            <td onClick={last ? this.addArpItem(name, sources) : () => this}>
                {last && <i className="fa fa-plus"></i>}
            </td>
        </tr>;

    renderArpAttribute = (name, authorities, arp) =>
        <section key={`${name}_arp`} className="arp_attribute">
            <span className="label">{I18n.t("playground.attribute_name")}</span>
            <p className="value">{name}</p>
            <table>
                <thead>
                <tr>
                    <th className="enabled">{I18n.t("playground.enabled")}</th>
                    <th className="rule">{I18n.t("playground.matching_rule")}</th>
                    <th className="source">{I18n.t("playground.source")}</th>
                    <th className="add_value"></th>
                </tr>
                </thead>
                <tbody>
                {arp[name].map((arpValue, index) => this.renderArpValue(name, authorities, index, arpValue, (arp[name].length - 1) === index))}
                </tbody>
            </table>
        </section>;

    renderUserAttributes = (name, userAttributes) =>
        <section key={`${name}_user`} className="user_attributes">
            <span className="label">{I18n.t("playground.attribute_name")}</span>
            <p className="value">{name}</p>
            <input type="text" value={userAttributes[name]} onChange={this.changeUserAttribute(name)}/>
        </section>;

    renderButtons = () =>
        <section className="buttons">
            <a href="#" onClick={this.doAttributeAggregation} className="button blue">
                <i className="fa fa-refresh"></i>{I18n.t("playground.do_attribute_aggregation")}
            </a>
            <a href="#" onClick={this.componentWillMount} className="button grey">
                {I18n.t("playground.clear")}
            </a>
        </section>;


    renderLeft = (attributesAndAuthorityIds, arp, userAttributes) => {
        return (
            <section className="arp">
                <h2>{I18n.t("playground.arp")}</h2>
                {Object.keys(attributesAndAuthorityIds).map(name =>
                    this.renderArpAttribute(name, attributesAndAuthorityIds[name], arp))}
                <h2 className="header_user_attributes">{I18n.t("playground.user_attributes")}</h2>
                {Object.keys(userAttributes).map(name =>
                    this.renderUserAttributes(name, userAttributes))}
                {this.renderButtons()}
            </section>
        );
    };

    renderResult = (result, duration) =>
        <div className="playground_result">
            <section>
                <i className="fa fa-check"></i>
                <article>
                    <h1>{I18n.t("playground.result_status_ok")}</h1>
                    <em></em>
                </article>
                <p><i className="fa fa-file-o"></i>result.json - {duration} ms</p>
            </section>
            <pre>
          <code dangerouslySetInnerHTML={{__html: prettyPrintJson(result)}}></code>
        </pre>
        </div>;

    render() {
        const {attributesAndAuthorityIds, arp, result, duration, userAttributes} = this.state;
        return (
            <div className="playground">
                <div className="left">
                    {this.renderLeft(attributesAndAuthorityIds, arp, userAttributes)}
                </div>
                <div className="right">
                    {this.renderRight(result, duration)}
                </div>

            </div>
        );
    }
}
