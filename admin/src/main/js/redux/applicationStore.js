import { createStore, applyMiddleware, combineReducers } from 'redux';
import thunkMiddleware from 'redux-thunk';
import { syncHistoryWithStore, routerReducer } from 'react-router-redux';
import { browserHistory } from 'react-router';

const reducers = combineReducers({
    routing: routerReducer
});

const middleware = applyMiddleware(thunkMiddleware);

export const applicationStore = createStore(reducers, middleware);