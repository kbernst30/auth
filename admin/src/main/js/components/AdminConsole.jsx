import React from 'react';
import PropTypes from 'prop-types'
import { Route } from 'react-router-dom';
import { connect } from 'react-redux';

const mapStateToProps = (state) => {
    return {

    };
};

const mapDispatchToProps = (dispatch) => {
    return {

    };
};

class AdminConsoleBody extends React.Component {
    render() {
        return (
            <div>Hello JSX</div>
        );
    }
}

const AdminConsole = connect(mapStateToProps, mapDispatchToProps)(AdminConsoleBody);
export default AdminConsole;