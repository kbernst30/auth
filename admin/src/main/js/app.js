import React from 'react';
import ReactDOM from 'react-dom';

import { BrowserRouter, Route } from 'react-router-dom'
import { Provider } from "react-redux";

import { applicationStore } from './redux/applicationStore.js';

import AdminConsole from "./components/AdminConsole";
import keystash from "./utilities/keystash.js";

import './styles/main.scss';

if (!keystash.isAuthenticated() || !keystash.isAuthorized()) {
    keystash.login();

} else if (keystash.authenticationIsExpired() || keystash.authorizationIsExpired()) {
    // TODO refresh if expired
    keystash.refreshAuth();

} else {
    ReactDOM.render(
        <Provider store={applicationStore}>
            <BrowserRouter>
                <Route path="/" component={AdminConsole}/>
            </BrowserRouter>
        </Provider>,

        document.getElementById('app')
    );
}