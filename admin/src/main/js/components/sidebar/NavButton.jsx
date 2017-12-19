import React from 'react';
import PropTypes from 'prop-types'

const NavButton = ({ text, icon }) => (
    <div className="navbutton">
        <i className={"fas fa-" + icon} />&nbsp; {text}
    </div>
);

NavButton.propTypes = {
    icon: PropTypes.string.isRequired,
    text: PropTypes.string.isRequired
};

NavButton.defaultProps = {
    icon: "square"
};

export default NavButton;