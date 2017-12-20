import React from 'react';
import PropTypes from 'prop-types'

import { Route } from 'react-router-dom';
import { connect } from 'react-redux';

import ClientManager from './clients/ClientManager';
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

const AdminConsoleBody = ({ location }) => (
    <div className="admin-console">
        <Topbar />
        <div className="admin-console-inner">
            <Sidebar currentLocation={location.pathname} />
            <div className="admin-console-main">
                <Route exact path="/clients" component={ClientManager} />
            </div>
        </div>
    </div>
);

const AdminConsole = connect(mapStateToProps, mapDispatchToProps)(AdminConsoleBody);
export default AdminConsole;