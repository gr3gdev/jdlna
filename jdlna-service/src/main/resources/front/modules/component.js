document.componentRegistry = { };
document.nextId = 0;

class Component {
    constructor() {
        this._id = ++document.nextId;
        document.componentRegistry[this._id] = this;
    }
    update() {
        document.querySelector('body').innerHTML = render();
    }
}

export default Component;