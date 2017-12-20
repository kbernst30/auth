import React from 'react';
import PropTypes from 'prop-types'

import { connect } from 'react-redux';

import NavButton from './NavButton';

const mapStateToProps = (state) => {
    return {

    };
};

const mapDispatchToProps = (dispatch) => {
    return {

    };
};

const SidebarBody = ({ currentLocation }) => (
    <div className="sidebar">
        <NavButton active={currentLocation === "/clients"} icon="sitemap" path="/clients" text="Clients" />
        <NavButton active={currentLocation === "/scopes"} icon="star" path="/scopes" text="Scopes" />
        <NavButton active={currentLocation === "/users"} icon="users" path="/users" text="Users" />
        <NavButton active={currentLocation === "/settings"} icon="cogs" path="/settings" text="Settings" />
    </div>
);

SidebarBody.propTypes = {
    currentLocation: PropTypes.string.isRequired
};

const Sidebar = connect(mapStateToProps, mapDispatchToProps)(SidebarBody);

export default Sidebar;