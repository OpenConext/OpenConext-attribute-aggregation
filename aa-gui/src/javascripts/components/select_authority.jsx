import React from "react";
import PropTypes from "prop-types";
import Select from "react-select";
import "react-select/dist/react-select.css";

export default class SelectAuthority extends React.PureComponent {

    render() {
        const {onChange, source, sources, disabled} = this.props;
        return <Select className="select-authority"
                       onChange={onChange}
                       optionRenderer={option => <span>{option.value}</span>}
                       options={sources.map(s => {
                           return {value: s, label: s};
                       })}
                       clearable={false}
                       disabled={disabled}
                       value={source}
                       searchable={false}
                       valueRenderer={this.renderOption}/>;
    }
}

SelectAuthority.propTypes = {
    onChange: PropTypes.func.isRequired,
    source: PropTypes.string.isRequired,
    sources: PropTypes.array.isRequired,
    disabled: PropTypes.bool.isRequired
};


