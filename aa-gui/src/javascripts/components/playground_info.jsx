import React from "react";
import PropTypes from "prop-types";

export default function PlayGroundInfo({locale = "en"}) {
    return locale === "en" ?
        <div className="playground_info">
            <h2>ABOUT THE PLAYGROUND</h2>
            <p className="primary">The input for Attribute Aggregation are the ARP of a SP and the user
                attributes received from de IdP. Define an ARP and test
                the Attribute Aggregation endpoint.</p>
            <p className="secondary">The ARP attributes are limited to those possible returned by the
                available Attribute authorities and the sources are limited to the authorities that
            can return the attribute.</p>
            <p className="secondary">The user attributes attributes are limited to those required by the
                available Attribute authorities to perform their queries.</p>
        </div> :
        <div className="playground_info">
        </div>;

}

PlayGroundInfo.propTypes = {
    locale: PropTypes.string,
};

