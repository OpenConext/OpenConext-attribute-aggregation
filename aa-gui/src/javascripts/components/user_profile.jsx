import React from "react";
import I18n from "i18n-js";
import PropTypes from "prop-types";

export default function UserProfile({currentUser}) {
    return (
        <ul className="user-profile">
            <li>
                <span>{`${I18n.t("profile.name")}:`}</span>
                <span className="value">{currentUser.displayName}</span>
            </li>
        </ul>);

}

UserProfile.propTypes = {
    currentUser: PropTypes.object.isRequired
};


