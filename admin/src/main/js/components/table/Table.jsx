import React from 'react';
import PropTypes from 'prop-types';

import Cell from './Cell';
import Row from './Row';

class Table extends React.Component {

    constructor(props) {
        super(props);

        this.getTableHeader = this.getTableHeader.bind(this);
        this.getTableRows = this.getTableRows.bind(this);
        this.getTableStyle = this.getTableStyle.bind(this);
    }

    getTableHeader() {
        return (
            <Row height={this.props.headerHeight} header={true}>
                {React.Children.map(this.props.children, (column, idx) => (
                    <Cell width={column.props.width}>
                        {column.props.headerText}
                    </Cell>
                ))}
            </Row>
        );
    }

    getTableRows() {
        // TODO row key should probably be itemId not itemIdx or updates might not work
        return this.props.data.map((item, itemIdx) => (
            <Row key={itemIdx} height={this.props.rowHeight}>
                {React.Children.map(this.props.children, (column, columnIdx) => (
                    <Cell>
                        {item[column.props.dataKey]}
                    </Cell>
                ))}
            </Row>
        ));
    }

    getTableStyle() {
        let style = {
            "width": this.props.width || "100%"
        };

        if (this.props.height) {
            style["height"] = this.props.height;
        }

        if (this.props.maxHeight) {
            style["maxHeight"] = this.props.maxHeight;
        }

        return style;
    }

    render() {
        return (
            <div className={"data-table " + this.props.className}
                 style={this.getTableStyle()}>

                {this.getTableHeader()}

                <div className="data-table-body">
                    {this.getTableRows()}
                </div>
            </div>
        );
    }

}

Table.propTypes = {
    children: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.element), PropTypes.element]).isRequired,
    className: PropTypes.string,
    data: PropTypes.arrayOf(PropTypes.object).isRequired,
    headerHeight: PropTypes.number,
    height: PropTypes.number,
    maxHeight: PropTypes.number,
    rowHeight: PropTypes.number.isRequired,
    width: PropTypes.number
};

Table.defaultProps = {
    className: "",
    headerHeight: 40,
    rowHeight: 30
};

export default Table;