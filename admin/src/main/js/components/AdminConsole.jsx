import React from 'react';
import PropTypes from 'prop-types'

import { Route } from 'react-router-dom';
import { connect } from 'react-redux';

import Sidebar from './sidebar/Sidebar';
import Topbar from './topbar/Topbar';

const mapStateToProps = (state) => {
    return {

    };
};

const mapDispatchToProps = (dispatch) => {
    return {

    };
};

const AdminConsoleBody = () => (
    <div className="admin-console">
        <Topbar />
        <div className="admin-console-inner">
            <Sidebar />
        </div>
    </div>
);

const AdminConsole = connect(mapStateToProps, mapDispatchToProps)(AdminConsoleBody);
export default AdminConsole;