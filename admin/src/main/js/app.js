import React from 'react';
import ReactDOM from 'react-dom';

import { BrowserRouter, Route } from 'react-router-dom'
import { Provider } from "react-redux";

import { applicationStore } from './redux/applicationStore.js';

import AdminConsole from "./components/AdminConsole";
import AdminConsoleError from "./components/AdminConsoleError";
import keystash from "./utilities/keystash.js";

import './styles/main.scss';

const isAuthorizing = window.location.search && window.location.search.substring(1).toLowerCase().indexOf("authorizing=true") > -1;
const isError = window.location.search && window.location.search.substring(1).toLowerCase().indexOf("error=") > -1;

if (isError) {
    ReactDOM.render(<AdminConsoleError error="Oops" />, document.getElementById('app'));

} else if (isAuthorizing) {
    keystash.processAuth();

} else if (!keystash.isAuthenticated() || !keystash.isAuthorized() || keystash.authenticationIsExpired() || keystash.authorizationIsExpired()) {
    keystash.login();

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