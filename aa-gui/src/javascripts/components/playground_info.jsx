import React from "react";
import PropTypes from "prop-types";

export default function PlayGroundInfo({locale = "en"}) {
    return locale === "en" ?
        <div className="playground_info">
            <p className="primary">The input for Attribute Aggregation is an ARP. Define an ARP and test
                the Attribute Aggregation endpoint.</p>
            <p className="secondary">The ARP attributes are limited to those required by the
                available Attribute authorities and the sources are limited to the authorities that
            require the attribute in order to perform aggregation.</p>
        </div> :
        <div className="playground_info">
        </div>;

}

PlayGroundInfo.propTypes = {
    locale: PropTypes.string,
};

