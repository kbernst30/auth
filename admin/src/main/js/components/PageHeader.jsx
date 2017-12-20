import React from 'react';
import PropTypes from 'prop-types';

const PageHeader = ({ children }) => (
    <div className="page-header">{children}</div>
);

PageHeader.propTypes = {
    children: PropTypes.string.isRequired
};

export default PageHeader;