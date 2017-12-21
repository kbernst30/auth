import React from 'react';
import PropTypes from 'prop-types';

class Column extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        console.error("Columns should not be rendered directly");
        return null;
    }
}

Column.propTypes = {
    dataKey: PropTypes.string.isRequired,
    headerText: PropTypes.string.isRequired,
    renderer: PropTypes.func,
    width: PropTypes.number
};

export default Column;