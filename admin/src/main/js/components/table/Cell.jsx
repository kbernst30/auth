import React from 'react';
import PropTypes from 'prop-types';

class Cell extends React.Component {

    constructor(props) {
        super(props);

        this.getCellClass = this.getCellClass.bind(this);
        this.getCellStyle = this.getCellStyle.bind(this);
    }

    getCellClass() {
        let cellClass = "data-table-cell";

        if (this.props.width) {
            cellClass += " no-flex";
        }

        return cellClass;
    }

    getCellStyle() {
        let style = {};

        if (this.props.width) {
            style["width"] = this.props.width;
        }

        return style;
    }

    render() {
        return (
            <div className={this.getCellClass()} style={this.getCellStyle()}>
                <div style={{"padding": this.props.padding}}>
                    {this.props.children}
                </div>
            </div>
        )
    }
}

Cell.propTypes = {
    width: PropTypes.number,
    padding: PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired
};

Cell.defaultProps = {
    padding: 5
};

export default Cell;