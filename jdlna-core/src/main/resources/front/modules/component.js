document.componentRegistry = { };
document.nextId = 0;

class Component {
    constructor() {
        this.id = ++document.nextId;
        document.componentRegistry[this.id] = this;
    }
    addReloadEvent(callback) {
        this.reload = callback;
    }
    update() {
        if (this.reload) {
            this.reload.call();
        }
    }
}

export default Component;