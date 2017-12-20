import React from 'react';
import PropTypes from 'prop-types';

class Row extends React.Component {

    constructor(props) {
        super(props);

        this.getRowClass = this.getRowClass.bind(this);
    }

    getRowClass() {
        return this.props.header ? "data-table-header" : "data-table-row";
    }

    render() {
        return (
            <div className={this.getRowClass()} style={{"height": this.props.height}}>
                {this.props.children}
            </div>
        )
    }
}

Row.propTypes = {
    children: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.element), PropTypes.element]).isRequired,
    header: PropTypes.bool.isRequired,
    height: PropTypes.number.isRequired
};

Row.defaultProps = {
    header: false
};

export default Row;