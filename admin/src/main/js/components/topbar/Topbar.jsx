import React from 'react';
import PropTypes from 'prop-types'

import { connect } from 'react-redux';

import { DropdownMenu, DropdownMenuItem } from "../DropdownMenu";

const mapStateToProps = (state) => {
    return {

    };
};

const mapDispatchToProps = (dispatch) => {
    return {

    };
};

class TopbarBody extends React.Component {
    render() {
        return (
            <div className="topbar">
                <DropdownMenu className="topbar-right-item">
                    <DropdownMenuItem action={() => {}} text="Logout" />
                </DropdownMenu>
            </div>
        );
    }
}

const Topbar = connect(mapStateToProps, mapDispatchToProps)(TopbarBody);
export default Topbar;