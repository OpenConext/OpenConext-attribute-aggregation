import React from "react";
import PropTypes from "prop-types";
import Select from "react-select";
import "react-select/dist/react-select.css";

import nlFlag from "../../images/dutch_flag.png";
import enFlag from "../../images/british_flag.png";

const languageOptions = [{value: "DUTCH", label: "Nederlands"}, {value: "ENGLISH", label: "English"}];

export default class SelectLanguage extends React.PureComponent {


    renderOption = option => {
        return (
            <span className="select-option">
                {option.value === "ENGLISH" ? <img src={enFlag}/> : <img src={nlFlag}/>}
                <span className="select-label">{option.label}</span>
            </span>
        );
    };

    render() {
        const {onChange, language, disabled} = this.props;
        return <Select className="select-language"
                       onChange={onChange}
                       optionRenderer={this.renderOption}
                       options={languageOptions}
                       value={language}
                       searchable={false}
                       valueRenderer={this.renderOption}
                       disabled={disabled}/>;
    }


}

SelectLanguage.propTypes = {
    onChange: PropTypes.func.isRequired,
    language: PropTypes.string.isRequired,
    disabled: PropTypes.bool
};


