import React from "react";

import {authorityConfiguration} from "../api";

export default class Playground extends React.Component {

    constructor(props, context) {
        super(props, context);
        this.state = {};
    }

    componentWillMount = () => authorityConfiguration().then(configuration => this.setState(
        {authorities: configuration.authorities.sort((a1, a2) => a1.id.localeCompare(a2.id)), selectedAuthority: configuration.authorities[0]}
    ));


    render() {
        return (
            <div className="playground">
                <div className="left">
                    {"left"}
                </div>
                <div className="right">
                    {"right"}
                </div>

            </div>
        );
    }
}
