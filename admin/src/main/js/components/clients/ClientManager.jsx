import React from 'react';
import PropTypes from 'prop-types'

import { connect } from 'react-redux';

import PageHeader from'../PageHeader';
import Column from '../table/Column';
import Table from '../table/Table';

const mapStateToProps = (state) => {
    return {

    };
};

const mapDispatchToProps = (dispatch) => {
    return {

    };
};

const clients = [
    {
        clientId: "my-client",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-2",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-3",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-4",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-5",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-6",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-7",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-8",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-9",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-10",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-11",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-12",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    },
    {
        clientId: "my-client-13",
        created: "2017-08-01",
        lastAccessed: "2017-12-20"
    }
];

const ClientManagerBody = () => (
    <div className="client-manager">
        <PageHeader>Clients</PageHeader>
        <Table data={clients}
               height={400}>

            <Column headerText="Client ID" dataKey="clientId" />
            <Column headerText="Created" dataKey="created" />
            <Column headerText="Last Accessed" dataKey="lastAccessed" />
        </Table>
    </div>
);

const ClientManager = connect(mapStateToProps, mapDispatchToProps)(ClientManagerBody);

export default ClientManager;