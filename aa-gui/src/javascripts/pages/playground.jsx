import React from "react";
// import Select from "react-select";
import I18n from "i18n-js";
import {prettyPrintJson} from "../utils/utils";
import PlayGroundInfo from "../components/playground_info";
import CheckBox from "../components/checkbox";

import {authorityConfiguration} from "../api";

export default class Playground extends React.Component {

    constructor(props, context) {
        super(props, context);
        this.state = {
            requiredAttributesAndAuthorityIds: {},
            arp: {},
            result: undefined,
            duration: 0
        };
    }

    componentWillMount = () => authorityConfiguration().then(configuration => {
        const requiredAttributes = new Set([].concat(
            ...configuration.authorities.map(authority => authority.requiredInputAttributes.map(attribute => attribute.name)))
        );
        const requiredAttributesAndAuthorityIds = Array.from(requiredAttributes).reduce((acc, attribute) => {
            acc[attribute] = configuration.authorities
                .filter(authority => authority.requiredInputAttributes.map(attr => attr.name).includes(attribute))
                .map(authority => authority.id);
            return acc;
        }, {});
        const arp = Array.from(requiredAttributes).reduce((acc, attribute) => {
            acc[attribute] = [{id: 0, value: "", source: ""}];
            return acc;
        }, {});
        this.setState({
            requiredAttributesAndAuthorityIds: requiredAttributesAndAuthorityIds,
            arp: arp
        });
    });

    renderRight = (result, duration) => result ? this.renderResult(result, duration) :
        <PlayGroundInfo locale={I18n.locale}/>;

    removeArpItem = (name, id) => {
        const arp = this.state.arp;
        const items = arp[name];
        const filterItems = items.filter(item => item.id !== id);
        arp[name] = filterItems;
        this.setState({arp: arp});
    };

    addArpItem = (name, sources) => () => {
        const arp = this.state.arp;
        const items = arp[name] || [];
        items.push({id: items.length + 1, value: "", source: sources[0]});
        arp[name] = items;
        this.setState({arp: arp});
    };

    renderCheckBox = (name, item, index) => <div key={`${name}_${index}`}>
        <CheckBox name={`${name}_${index}`} value={true} onChange={this.addArpItem(name)}/>
    </div>;

    renderArpAttribute = (name, authorities, arp) => <tr key={name}>
        <td>{name}</td>
        <td>{arp[name].map((item, index) => this.renderCheckBox(name, item, index))}</td>
        <td></td>
        <td><i className="fa fa-plus"></i></td>
    </tr>;

    renderLeft = (requiredAttributesAndAuthorityIds, arp) => {
        return (
            <section className="arp">
                <h2>{I18n.t("playground.arp")}</h2>
                <table>
                    <thead>
                    <tr>
                        <th>{I18n.t("playground.attribute_name")}</th>
                        <th>{I18n.t("playground.enabled")}</th>
                        <th>{I18n.t("playground.matching_rule")}</th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    {Object.keys(requiredAttributesAndAuthorityIds).map(name =>
                        this.renderArpAttribute(name, requiredAttributesAndAuthorityIds[name], arp))}
                    </tbody>
                </table>
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
        const {requiredAttributesAndAuthorityIds, arp, result, duration} = this.state;
        return (
            <div className="playground">
                <div className="left">
                    {this.renderLeft(requiredAttributesAndAuthorityIds, arp)}
                </div>
                <div className="right">
                    {this.renderRight(result, duration)}
                </div>

            </div>
        );
    }
}
