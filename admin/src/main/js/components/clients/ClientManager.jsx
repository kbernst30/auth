import React from 'react';
import PropTypes from 'prop-types'

import { connect } from 'react-redux';

const mapStateToProps = (state) => {
    return {

    };
};

const mapDispatchToProps = (dispatch) => {
    return {

    };
};

const ClientManagerBody = () => (
    <div className="">
        Clients
    </div>
);

const ClientManager = connect(mapStateToProps, mapDispatchToProps)(ClientManagerBody);

export default ClientManager;