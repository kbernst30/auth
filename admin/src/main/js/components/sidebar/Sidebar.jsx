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

const SidebarBody = () => (
    <div className="sidebar">
        <NavButton icon="sitemap" text="Clients" />
        <NavButton icon="star" text="Scopes" />
        <NavButton icon="users" text="Users" />
        <NavButton icon="cogs" text="Settings" />
    </div>
);

const Sidebar = connect(mapStateToProps, mapDispatchToProps)(SidebarBody);

export default Sidebar;