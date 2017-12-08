const HELPERS = {

    registerClickOutsideEvent: (component, handler) => {
        const handleClickOutside = e => {
            if (component && handler && !component.contains(e.target)) {
                handler();
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return handleClickOutside;
    },

    unregisterClickOutsideEvent: handler => document.removeEventListener('mousedown', handler)

};

export default HELPERS;