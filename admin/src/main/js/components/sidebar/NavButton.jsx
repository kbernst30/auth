import React from 'react';
import PropTypes from 'prop-types'
import { Link } from 'react-router-dom';

const NavButton = ({ active, icon, path, text }) => (
    <Link to={path} style={{"textDecoration": "none"}}>
        <div className={"navbutton " + (active && "active")}>
            <i className={"fas fa-" + icon} />&nbsp; {text}
        </div>
    </Link>
);

NavButton.propTypes = {
    active: PropTypes.bool.isRequired,
    icon: PropTypes.string.isRequired,
    path: PropTypes.string.isRequired,
    text: PropTypes.string.isRequired
};

NavButton.defaultProps = {
    active: false,
    icon: "square"
};

export default NavButton;