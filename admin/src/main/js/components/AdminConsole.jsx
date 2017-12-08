import React from 'react';
import PropTypes from 'prop-types'

import { Route } from 'react-router-dom';
import { connect } from 'react-redux';

import Topbar from './topbar/Topbar';

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
            <Topbar />
        );
    }
}

const AdminConsole = connect(mapStateToProps, mapDispatchToProps)(AdminConsoleBody);
export default AdminConsole;