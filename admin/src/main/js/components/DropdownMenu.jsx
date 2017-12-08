import React from 'react';
import PropTypes from 'prop-types'

import HELPERS from '../utilities/helpers';

const DropdownMenuItem = ({ action, text }) => <li onClick={action}>{text}</li>;

class DropdownMenu extends React.Component {

    constructor(props) {
        super(props);

        this.state = { collapsed: true };

        this.setRef = this.setRef.bind(this);
        this.getItemsClass = this.getItemsClass.bind(this);
        this.toggleMenu = this.toggleMenu.bind(this);
    }

    componentDidMount() {
        this.clickOutsideHandler = HELPERS.registerClickOutsideEvent(this._ref, () => {
            if (!this.state.collapsed) {
                this.toggleMenu();
            }
        })
    }

    componentWillUnmount() {
        HELPERS.unregisterClickOutsideEvent(this.clickOutsideHandler);
    }

    setRef(ref) {
        this._ref = ref;
    }

    getItemsClass() {
        let className = "dropdown-menu-items";
        if (this.state.collapsed) {
            className += " collapsed";
        }

        // If menu is on the right side of the screen, align the items to the right, otherwise align it left
        if (this._ref && this._ref.offsetLeft > (window.innerWidth / 2)) {
            className += " right-align";
        } else {
            className += " left-align";
        }

        return className;
    }

    toggleMenu() {
        this.setState({ collapsed: !this.state.collapsed });
    }

    render() {
        return (
            <div className={"dropdown-menu " + this.props.className} onClick={this.toggleMenu} ref={this.setRef}>
                <div>{"This will be a username"}</div>
                <div className={this.getItemsClass()}>
                    <ul className="dropdown-menu-item-list">
                        {this.props.children}
                    </ul>
                </div>
            </div>
        );
    }
}

DropdownMenu.propTypes = {
    className: PropTypes.string
};

DropdownMenu.defaultProps = {
    className: ""
};

DropdownMenuItem.propTypes = {
    action: PropTypes.func.isRequired,
    text: PropTypes.string.isRequired
};

export { DropdownMenu, DropdownMenuItem };